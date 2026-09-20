package com.example.util

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.ExtractedMatchData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object OcrMatchExtractor {

    private val ignoredHeaders = setOf(
        "premier league", "la liga", "laliga", "serie a", "bundesliga", "ligue 1",
        "bajnokok ligája", "champions league", "europa league", "konferencia liga",
        "nb i", "nb 1", "nb ii", "nb 2", "fa cup", "copa del rey", "dfb pokal", "eredivisie",
        "összefoglaló", "statisztika", "statisztikák", "felállások", "felállás",
        "tabella", "odds", "h2h", "eredmények", "flashscore", "sofascore",
        "fotmob", "bet365", "tippmix", "live", "élő", "vége", "félidő", "ht", "ft",
        "hazai", "vendég", "döntetlen", "home", "away", "draw", "1x2", "összesen",
        "mérkőzés", "meccs", "infó", "hírek", "kommentár", "értékelés", "további",
        "foci", "labdarúgás", "félidő/végeredmény", "hendikep", "gólok száma"
    )

    suspend fun extractFromBitmaps(bitmaps: List<Bitmap>, isLiveMode: Boolean): ExtractedMatchData =
        withContext(Dispatchers.Default) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val extractedTexts = mutableListOf<String>()

            for (bitmap in bitmaps) {
                try {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val visionText = suspendCancellableCoroutine<String> { continuation ->
                        recognizer.process(inputImage)
                            .addOnSuccessListener { result ->
                                continuation.resume(result.text)
                            }
                            .addOnFailureListener { e ->
                                Log.e("OcrMatchExtractor", "OCR failure on image", e)
                                continuation.resume("")
                            }
                    }
                    if (visionText.isNotBlank()) {
                        extractedTexts.add(visionText)
                    }
                } catch (e: Throwable) {
                    Log.e("OcrMatchExtractor", "Bitmap process error", e)
                }
            }

            if (extractedTexts.isEmpty()) {
                return@withContext ExtractedMatchData(
                    homeTeam = null,
                    awayTeam = null,
                    context = "Nem sikerült szöveget kiolvasni a képről."
                )
            }

            parseOcrResults(extractedTexts, isLiveMode)
        }

    fun parseOcrResults(texts: List<String>, isLiveMode: Boolean): ExtractedMatchData {
        val combinedText = texts.joinToString("\n---\n")
        val lines = texts.flatMap { it.lines() }.map { it.trim() }.filter { it.isNotBlank() }

        var homeTeam: String? = null
        var awayTeam: String? = null
        var score: String? = null
        var minute: Int? = null

        var shotsHome: Int? = null
        var shotsAway: Int? = null
        var shotsHomeOnTarget: Int? = null
        var shotsAwayOnTarget: Int? = null
        var dangerousAttacksHome: Int? = null
        var dangerousAttacksAway: Int? = null
        var cornersHome: Int? = null
        var cornersAway: Int? = null
        var possessionHome: Int? = null
        var possessionAway: Int? = null
        var homeBaseXg: Double? = null
        var awayBaseXg: Double? = null

        // 1. Check for "TeamA vs TeamB" or "TeamA - TeamB" patterns in lines
        for (line in lines) {
            if (homeTeam != null && awayTeam != null) break
            val vsMatch = Regex("([A-Za-z0-9ÁÉÍÓÖŐÚÜŰáéíóöőúüű\\s.'-]+)\\s+(?:vs|v|–|-)\\s+([A-Za-z0-9ÁÉÍÓÖŐÚÜŰáéíóöőúüű\\s.'-]+)", RegexOption.IGNORE_CASE).find(line)
            if (vsMatch != null) {
                val candidateH = cleanCandidateTeam(vsMatch.groupValues[1])
                val candidateA = cleanCandidateTeam(vsMatch.groupValues[2])
                if (isValidTeam(candidateH) && isValidTeam(candidateA)) {
                    homeTeam = candidateH
                    awayTeam = candidateA
                }
            }
        }

        // 2. Scoreboard detection (e.g. Home team on line i, Away on line i+2 with score in between, or consecutive lines)
        if (homeTeam == null || awayTeam == null) {
            val potentialTeams = mutableListOf<String>()
            for (line in lines) {
                val cleaned = cleanCandidateTeam(line)
                if (isValidTeam(cleaned)) {
                    potentialTeams.add(cleaned!!)
                }
            }
            if (potentialTeams.size >= 2) {
                homeTeam = potentialTeams[0]
                awayTeam = potentialTeams[1]
            }
        }

        // 3. Extract Score
        val scoreRegex = Regex("(\\d+)\\s*[-–:]\\s*(\\d+)")
        val scoreMatch = scoreRegex.find(combinedText)
        if (scoreMatch != null) {
            score = "${scoreMatch.groupValues[1]}-${scoreMatch.groupValues[2]}"
        }

        // 4. Extract Minute (e.g. 15', 38 min, 45+2', 15. perc)
        val minuteRegex = Regex("(\\d{1,2})\\s*(?:'|’|\\.|min|perc)", RegexOption.IGNORE_CASE)
        val minuteMatch = minuteRegex.find(combinedText)
        if (minuteMatch != null) {
            minute = minuteMatch.groupValues[1].toIntOrNull()
        } else if (combinedText.contains("Félidő", ignoreCase = true) || combinedText.contains("HT", ignoreCase = true)) {
            minute = 45
        }

        // 5. Extract Possession (%)
        val possRegex = Regex("(\\d{1,2})%\\s*(?:[-–]|\\s+)?\\s*(\\d{1,2})%")
        val possMatch = possRegex.find(combinedText)
        if (possMatch != null) {
            possessionHome = possMatch.groupValues[1].toIntOrNull()
            possessionAway = possMatch.groupValues[2].toIntOrNull()
        }

        // 6. Extract Statistics from lines
        for (i in lines.indices) {
            val line = lines[i].lowercase()

            // Dangerous attacks
            if (line.contains("veszélyes támadás") || line.contains("dangerous attack")) {
                extractTwoNumbersAround(lines, i)?.let {
                    dangerousAttacksHome = it.first
                    dangerousAttacksAway = it.second
                }
            }
            // Shots / Lövések
            else if ((line.contains("lövések") || line.contains("kapura lövés") || line.contains("total shots")) && !line.contains("kaput eltaláló")) {
                extractTwoNumbersAround(lines, i)?.let {
                    shotsHome = it.first
                    shotsAway = it.second
                }
            }
            // Shots on target / Kaput eltaláló
            else if (line.contains("kaput eltaláló") || line.contains("shots on target") || line.contains("kapura tartó")) {
                extractTwoNumbersAround(lines, i)?.let {
                    shotsHomeOnTarget = it.first
                    shotsAwayOnTarget = it.second
                }
            }
            // Corners / Szögletek
            else if (line.contains("szöglet") || line.contains("corner")) {
                extractTwoNumbersAround(lines, i)?.let {
                    cornersHome = it.first
                    cornersAway = it.second
                }
            }
            // xG / Várható gólok
            else if (line.contains("xg") || line.contains("várható gól")) {
                extractTwoDoublesAround(lines, i)?.let {
                    homeBaseXg = it.first
                    awayBaseXg = it.second
                }
            }
        }

        val impression = if (isLiveMode) {
            "Képernyőkép OCR alapján beolvasva (${minute ?: 15}. perc, állás: ${score ?: "0-0"})."
        } else {
            "Képernyőkép OCR alapján beolvasva: ${homeTeam ?: "Hazai"} vs ${awayTeam ?: "Vendég"}."
        }

        return ExtractedMatchData(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            score = score ?: (if (isLiveMode) "0-0" else null),
            minute = minute ?: (if (isLiveMode) 15 else null),
            shotsHome = shotsHome,
            shotsAway = shotsAway,
            shotsHomeOnTarget = shotsHomeOnTarget,
            shotsAwayOnTarget = shotsAwayOnTarget,
            dangerousAttacksHome = dangerousAttacksHome,
            dangerousAttacksAway = dangerousAttacksAway,
            cornersHome = cornersHome,
            cornersAway = cornersAway,
            possessionHome = possessionHome,
            possessionAway = possessionAway,
            homeBaseXg = homeBaseXg,
            awayBaseXg = awayBaseXg,
            context = "Képernyőképről sikeresen kiolvasva.",
            tacticalImpression = impression
        )
    }

    private fun extractTwoNumbersAround(lines: List<String>, index: Int): Pair<Int, Int>? {
        // Check current line for "X - Y" or "X Y"
        val currLine = lines[index]
        val numRegex = Regex("(\\d+)\\s*(?:[-–:]|\\s+)\\s*(\\d+)")
        val match = numRegex.find(currLine)
        if (match != null) {
            val n1 = match.groupValues[1].toIntOrNull()
            val n2 = match.groupValues[2].toIntOrNull()
            if (n1 != null && n2 != null) return Pair(n1, n2)
        }

        // Check previous and next lines
        val prev = if (index > 0) lines[index - 1].toIntOrNull() else null
        val next = if (index < lines.size - 1) lines[index + 1].toIntOrNull() else null
        if (prev != null && next != null) {
            return Pair(prev, next)
        }
        return null
    }

    private fun extractTwoDoublesAround(lines: List<String>, index: Int): Pair<Double, Double>? {
        val currLine = lines[index]
        val doubleRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:[-–:]|\\s+)\\s*(\\d+(?:\\.\\d+)?)")
        val match = doubleRegex.find(currLine)
        if (match != null) {
            val d1 = match.groupValues[1].toDoubleOrNull()
            val d2 = match.groupValues[2].toDoubleOrNull()
            if (d1 != null && d2 != null) return Pair(d1, d2)
        }
        return null
    }

    private fun cleanCandidateTeam(str: String?): String? {
        if (str == null) return null
        var cleaned = str.trim()
            .replace(Regex("^(Hazai|Vendég|Home|Away|1|2|H|V)\\s*[:–-]?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\((H|A|Hazai|Vendég|1|2)\\)$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(vs|–|-|:)\\s*"), "")
            .replace(Regex("\\s*(vs|–|-|:)$"), "")
            .replace(Regex("^[0-9.]+\\s*"), "") // strip leading numbers like "1. "
            .replace(Regex("\\s*[0-9.]+$"), "") // strip trailing odds like " 1.85"
            .trim()

        if (cleaned.length < 2) return null
        if (cleaned.all { it.isDigit() || it == '-' || it == ':' || it == '.' || it == ' ' }) return null

        return cleaned
    }

    private fun isValidTeam(candidate: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        if (candidate.length < 3) return false
        if (candidate.length > 35) return false
        val lower = candidate.lowercase().trim()

        if (ignoredHeaders.any { lower == it || lower.startsWith("$it ") || lower.endsWith(" $it") }) {
            return false
        }
        if (lower.contains("http") || lower.contains(".com") || lower.contains(".hu")) {
            return false
        }
        return true
    }
}
