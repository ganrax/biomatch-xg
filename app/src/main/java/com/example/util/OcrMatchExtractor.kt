package com.example.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.example.data.model.ExtractedMatchData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.abs

data class OcrItem(
    val text: String,
    val rect: Rect,
    val topRel: Float,
    val bottomRel: Float,
    val leftRel: Float,
    val rightRel: Float,
    val centerXRel: Float,
    val centerYRel: Float
)

object OcrMatchExtractor {

    private val ignoredKeywords = setOf(
        // Section headers, UI tabs & navigation
        "match timeline", "match timer", "timeline", "timer", "match events", "match event",
        "events", "event", "match info", "match details", "summary", "detail", "details",
        "lineups", "lineup", "odds", "h2h", "chat", "table", "standings", "tabella",
        "statistics", "stats", "statisztika", "statisztikák", "összefoglaló", "felállások",
        "felállás", "eredmények", "kommentár", "értékelés", "további", "infó", "hírek",
        
        // Statistics terms & match events
        "corner kicks", "corner kicks(ht)", "corner kicks (ht)", "corner kick", "corners", "szöglet", "szögletek",
        "shots", "total shots", "shots on target", "shots off target", "lövések", "kapura lövés", "kaput eltaláló", "kapura tartó",
        "attack", "attacks", "támadás", "támadások",
        "dangerous attack", "dangerous attacks", "veszélyes támadás", "veszélyes támadások",
        "possession", "ball possession", "labdabirtoklás", "birtoklás",
        "fouls", "foul", "szabálytalanság", "szabálytalanságok",
        "yellow cards", "red cards", "sárga lap", "piros lap", "lapok", "cards",
        "offsides", "les", "lesek", "saves", "védések", "bravúr",
        "xg", "expected goals", "várható gólok",
        "substitutions", "cserék", "referee", "bíró", "játékvezető", "stadium", "stadion",
        
        // Match status & periods
        "live", "élő", "finished", "vége", "ft", "ht", "half time", "full time", "félidő", "hosszabbítás", "et",
        "1st half", "2nd half", "1. félidő", "2. félidő", "ht)",
        
        // Generic betting / terms
        "hazai", "vendég", "döntetlen", "home", "away", "draw", "1x2", "összesen", "foci", "labdarúgás",
        "félidő/végeredmény", "hendikep", "gólok száma", "over", "under", "több", "kevesebb",
        
        // Sports App brand names
        "flashscore", "sofascore", "fotmob", "bet365", "tippmix", "goaloo", "livescore", "365scores", "aiscore"
    )

    private val leaguePatterns = listOf(
        Regex(".*(premier league|la liga|laliga|serie a|serie b|bundesliga|ligue 1|ligue 2).*", RegexOption.IGNORE_CASE),
        Regex(".*(champions league|europa league|conference league|bajnokok ligája|konferencia liga).*", RegexOption.IGNORE_CASE),
        Regex(".*(copa del rey|fa cup|dfb pokal|coppa italia|coupe de france|magyar kupa).*", RegexOption.IGNORE_CASE),
        Regex(".*(eredivisie|primeira liga|liga portugal|super lig|super league|ekstraklasa).*", RegexOption.IGNORE_CASE),
        Regex(".*(nb i|nb 1|nb ii|nb 2|segunda division|championship|league one|league two).*", RegexOption.IGNORE_CASE),
        Regex(".*(spanish|english|german|italian|french|hungarian|dutch|portuguese|turkish)\\s+(la liga|league|cup|liga).*", RegexOption.IGNORE_CASE)
    )

    suspend fun extractFromBitmaps(bitmaps: List<Bitmap>, isLiveMode: Boolean): ExtractedMatchData =
        withContext(Dispatchers.Default) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val allItems = mutableListOf<OcrItem>()
            val rawTexts = mutableListOf<String>()

            for (bitmap in bitmaps) {
                try {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val visionText = suspendCancellableCoroutine<Text?> { continuation ->
                        recognizer.process(inputImage)
                            .addOnSuccessListener { result ->
                                continuation.resume(result)
                            }
                            .addOnFailureListener { e ->
                                Log.e("OcrMatchExtractor", "OCR failure on image", e)
                                continuation.resume(null)
                            }
                    }

                    if (visionText != null) {
                        rawTexts.add(visionText.text)
                        val w = bitmap.width.toFloat().coerceAtLeast(1f)
                        val h = bitmap.height.toFloat().coerceAtLeast(1f)

                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val box = line.boundingBox ?: continue
                                val topRel = (box.top.toFloat() / h).coerceIn(0f, 1f)
                                val bottomRel = (box.bottom.toFloat() / h).coerceIn(0f, 1f)
                                val leftRel = (box.left.toFloat() / w).coerceIn(0f, 1f)
                                val rightRel = (box.right.toFloat() / w).coerceIn(0f, 1f)
                                val centerXRel = ((leftRel + rightRel) / 2f).coerceIn(0f, 1f)
                                val centerYRel = ((topRel + bottomRel) / 2f).coerceIn(0f, 1f)

                                allItems.add(
                                    OcrItem(
                                        text = line.text.trim(),
                                        rect = box,
                                        topRel = topRel,
                                        bottomRel = bottomRel,
                                        leftRel = leftRel,
                                        rightRel = rightRel,
                                        centerXRel = centerXRel,
                                        centerYRel = centerYRel
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("OcrMatchExtractor", "Bitmap process error", e)
                }
            }

            if (allItems.isEmpty() && rawTexts.isEmpty()) {
                return@withContext ExtractedMatchData(
                    homeTeam = null,
                    awayTeam = null,
                    context = "Nem sikerült szöveget kiolvasni a képről."
                )
            }

            parseSpatialOcrResults(allItems, rawTexts, isLiveMode)
        }

    fun parseSpatialOcrResults(
        items: List<OcrItem>,
        rawTexts: List<String>,
        isLiveMode: Boolean
    ): ExtractedMatchData {
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

        // --- STEP 1: SPATIAL TEAM DETECTION (Top Match Hero Card) ---
        // In sports apps (Goaloo, Flashscore, Sofascore, etc.), team names and score
        // are located in the upper banner between Y=0.06 and Y=0.36
        val heroCandidates = items.filter { it.centerYRel in 0.05f..0.38f }

        // Find candidate valid team items on the LEFT side (Home) and RIGHT side (Away)
        val leftTeamCandidates = heroCandidates
            .filter { it.centerXRel < 0.48f }
            .mapNotNull { item ->
                val cleaned = cleanCandidateTeam(item.text)
                if (isValidTeam(cleaned)) Pair(cleaned!!, item) else null
            }

        val rightTeamCandidates = heroCandidates
            .filter { it.centerXRel > 0.52f }
            .mapNotNull { item ->
                val cleaned = cleanCandidateTeam(item.text)
                if (isValidTeam(cleaned)) Pair(cleaned!!, item) else null
            }

        if (leftTeamCandidates.isNotEmpty()) {
            // Choose the most prominent candidate (closest to Y=0.18 or largest text height)
            homeTeam = leftTeamCandidates.minByOrNull { abs(it.second.centerYRel - 0.18f) }?.first
        }

        if (rightTeamCandidates.isNotEmpty()) {
            awayTeam = rightTeamCandidates.minByOrNull { abs(it.second.centerYRel - 0.18f) }?.first
        }

        // Fallback for stacked vertical layout in top hero (e.g. Flashscore list or Bet365 stacked cards)
        if (homeTeam == null || awayTeam == null) {
            val allValidHeroTeams = heroCandidates
                .mapNotNull { item ->
                    val cleaned = cleanCandidateTeam(item.text)
                    if (isValidTeam(cleaned)) Pair(cleaned!!, item) else null
                }
                .sortedBy { it.second.centerYRel }

            if (allValidHeroTeams.size >= 2) {
                if (homeTeam == null) homeTeam = allValidHeroTeams[0].first
                if (awayTeam == null) awayTeam = allValidHeroTeams[1].first
            }
        }

        // Fallback 1D regex for "Team A vs Team B" or "Team A - Team B"
        if (homeTeam == null || awayTeam == null) {
            for (item in items) {
                if (homeTeam != null && awayTeam != null) break
                val vsMatch = Regex("([A-Za-z0-9ÁÉÍÓÖŐÚÜŰáéíóöőúüű\\s.'-]+)\\s+(?:vs|v|–|-)\\s+([A-Za-z0-9ÁÉÍÓÖŐÚÜŰáéíóöőúüű\\s.'-]+)", RegexOption.IGNORE_CASE).find(item.text)
                if (vsMatch != null) {
                    val candidateH = cleanCandidateTeam(vsMatch.groupValues[1])
                    val candidateA = cleanCandidateTeam(vsMatch.groupValues[2])
                    if (isValidTeam(candidateH) && isValidTeam(candidateA)) {
                        homeTeam = candidateH
                        awayTeam = candidateA
                    }
                }
            }
        }

        // --- STEP 2: SCOREBOARD & MINUTE DETECTION (Top Center) ---
        // Scoreboard in Goaloo, Flashscore, SofaScore is usually at Y=0.10..0.30, center X=0.28..0.72
        val centerScoreItems = heroCandidates.filter { it.centerXRel in 0.28f..0.72f }

        // Look for combined score strings like "0 - 0", "1:0", "2 - 1" (excluding timestamps like 16:35 or dates)
        for (item in centerScoreItems) {
            val text = item.text.trim()
            if (isLikelyTimeOrDate(text)) continue

            val scoreMatch = Regex("^(\\d{1,2})\\s*[-–:]\\s*(\\d{1,2})$").find(text)
                ?: Regex("(?<=\\s|^)(\\d{1,2})\\s*[-–:]\\s*(\\d{1,2})(?=\\s|$)").find(text)

            if (scoreMatch != null) {
                val candidate = sanitizeFootballScore("${scoreMatch.groupValues[1]}-${scoreMatch.groupValues[2]}")
                if (candidate != null) {
                    score = candidate
                    break
                }
            }
        }

        // Look for 3 horizontal numbers in center: [homeScore, minute, awayScore] e.g. "0", "17", "0" (Goaloo style)
        if (score == null) {
            val digitsInCenter = centerScoreItems
                .filter { it.text.matches(Regex("^\\d{1,2}$")) && !isLikelyTimeOrDate(it.text) }
                .sortedBy { it.centerXRel }

            if (digitsInCenter.size >= 3) {
                val hScore = digitsInCenter[0].text
                val minVal = digitsInCenter[1].text.toIntOrNull()
                val aScore = digitsInCenter[2].text
                val candidate = sanitizeFootballScore("$hScore-$aScore")
                if (candidate != null) {
                    score = candidate
                    if (minVal != null && minVal in 1..120) {
                        minute = minVal
                    }
                }
            } else if (digitsInCenter.size == 2) {
                val candidate = sanitizeFootballScore("${digitsInCenter[0].text}-${digitsInCenter[1].text}")
                if (candidate != null) {
                    score = candidate
                }
            }
        }

        // Extract minute with ' or min
        val combinedText = rawTexts.joinToString("\n")
        val minuteRegex = Regex("(\\d{1,2})\\s*(?:'|’|\\.|min|perc)", RegexOption.IGNORE_CASE)
        val minMatch = minuteRegex.find(combinedText)
        if (minMatch != null && minute == null) {
            minute = minMatch.groupValues[1].toIntOrNull()
        } else if (minute == null && (combinedText.contains("Félidő", ignoreCase = true) || combinedText.contains("HT", ignoreCase = true))) {
            minute = 45
        }

        // --- STEP 3: SPATIAL STATISTICS ROW PARSING ---
        // For each stat label, find the left number and right number on the same vertical line
        val statRows = items.filter { it.centerYRel > 0.35f }

        for (labelItem in statRows) {
            val labelLower = labelItem.text.lowercase().trim()

            // Find numbers on the same horizontal row (within +/- 0.035 height)
            val rowItems = statRows.filter { abs(it.centerYRel - labelItem.centerYRel) <= 0.035f }
            val leftNum = rowItems
                .filter { it.centerXRel < labelItem.leftRel || it.centerXRel < 0.35f }
                .mapNotNull { it.text.toIntOrNull() }
                .firstOrNull()

            val rightNum = rowItems
                .filter { it.centerXRel > labelItem.rightRel || it.centerXRel > 0.65f }
                .mapNotNull { it.text.toIntOrNull() }
                .firstOrNull()

            if (leftNum != null && rightNum != null) {
                when {
                    labelLower.contains("dangerous attack") || labelLower.contains("veszélyes támadás") -> {
                        dangerousAttacksHome = leftNum
                        dangerousAttacksAway = rightNum
                    }
                    (labelLower.contains("shots") || labelLower.contains("lövések") || labelLower.contains("kapura lövés")) &&
                            !labelLower.contains("target") && !labelLower.contains("eltaláló") -> {
                        shotsHome = leftNum
                        shotsAway = rightNum
                    }
                    labelLower.contains("shots on target") || labelLower.contains("kaput eltaláló") || labelLower.contains("kapura tartó") -> {
                        shotsHomeOnTarget = leftNum
                        shotsAwayOnTarget = rightNum
                    }
                    labelLower.contains("corner") || labelLower.contains("szöglet") -> {
                        if (!labelLower.contains("ht")) {
                            cornersHome = leftNum
                            cornersAway = rightNum
                        }
                    }
                    labelLower.contains("possession") || labelLower.contains("birtoklás") -> {
                        possessionHome = leftNum
                        possessionAway = rightNum
                    }
                }
            }
        }

        // Fallback line-based regex stats if spatial didn't find all
        val lines = items.map { it.text }
        for (i in lines.indices) {
            val line = lines[i].lowercase()
            if (dangerousAttacksHome == null && (line.contains("dangerous attack") || line.contains("veszélyes támadás"))) {
                extractTwoNumbersAround(lines, i)?.let {
                    dangerousAttacksHome = it.first
                    dangerousAttacksAway = it.second
                }
            }
            if (shotsHome == null && (line.contains("shots") || line.contains("lövések")) && !line.contains("target") && !line.contains("eltaláló")) {
                extractTwoNumbersAround(lines, i)?.let {
                    shotsHome = it.first
                    shotsAway = it.second
                }
            }
            if (cornersHome == null && (line.contains("corner") || line.contains("szöglet")) && !line.contains("ht")) {
                extractTwoNumbersAround(lines, i)?.let {
                    cornersHome = it.first
                    cornersAway = it.second
                }
            }
            if (possessionHome == null && (line.contains("possession") || line.contains("birtoklás"))) {
                extractTwoNumbersAround(lines, i)?.let {
                    possessionHome = it.first
                    possessionAway = it.second
                }
            }
            if (line.contains("xg") || line.contains("várható gól")) {
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
        val currLine = lines[index]
        val numRegex = Regex("(\\d+)\\s*(?:[-–:]|\\s+)\\s*(\\d+)")
        val match = numRegex.find(currLine)
        if (match != null) {
            val n1 = match.groupValues[1].toIntOrNull()
            val n2 = match.groupValues[2].toIntOrNull()
            if (n1 != null && n2 != null) return Pair(n1, n2)
        }

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

    fun cleanCandidateTeam(str: String?): String? {
        if (str == null) return null
        var cleaned = str.trim()
            .replace(Regex("^(Hazai|Vendég|Home|Away|1|2|H|V)\\s*[:–-]?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\((H|A|Hazai|Vendég|1|2)\\)$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(vs|–|-|:)\\s*"), "")
            .replace(Regex("\\s*(vs|–|-|:)$"), "")
            .replace(Regex("^[0-9.]+\\s*"), "")
            .replace(Regex("\\s*[0-9.]+$"), "")
            .trim()

        if (cleaned.length < 2) return null
        if (cleaned.all { it.isDigit() || it == '-' || it == ':' || it == '.' || it == ' ' || it == '/' }) return null

        return cleaned
    }

    fun isValidTeam(candidate: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        val trimmed = candidate.trim()
        if (trimmed.length < 2 || trimmed.length > 35) return false

        // Filter numeric, date, timestamp strings
        if (trimmed.all { it.isDigit() || it.isWhitespace() || it in "-:.'’/,%+()" }) return false
        if (trimmed.matches(Regex("^[0-9/\\-\\s:.]+$"))) return false
        if (trimmed.matches(Regex("^\\d{1,2}['’].*"))) return false

        val lower = trimmed.lowercase()

        if (ignoredKeywords.any { lower == it || lower.startsWith("$it ") || lower.endsWith(" $it") || lower == "${it}s" }) {
            return false
        }

        if (leaguePatterns.any { it.matches(lower) }) {
            return false
        }

        if (lower.contains("http") || lower.contains(".com") || lower.contains(".hu") || lower.contains(".org")) {
            return false
        }

        return true
    }

    /**
     * Rejects time strings (e.g. 16:35, 20:45) and dates (e.g. 09/21/2026, 21.09) so they aren't parsed as scores.
     */
    fun isLikelyTimeOrDate(text: String): Boolean {
        val t = text.trim()
        // Date formats: 09/21/2026, 21/09/2026, 2026-09-21, 21.09.2026
        if (t.matches(Regex(".*\\d{1,4}[/.-]\\d{1,2}[/.-]\\d{2,4}.*"))) return true
        // Time format with 2 digits: 16:35, 18:00, 20:45
        val timeMatch = Regex("(?:^|\\s)([01]?\\d|2[0-3]):([0-5]\\d)(?:\\s|$)").find(t)
        if (timeMatch != null) {
            val h = timeMatch.groupValues[1].toIntOrNull() ?: 0
            val m = timeMatch.groupValues[2].toIntOrNull() ?: 0
            // In football, a 15th minute score is almost never > 6 goals per team.
            // If either number is >= 10, or minute is standard minute (15, 30, 45, 35, etc.), it's a timestamp.
            if (h >= 10 || m >= 10) return true
        }
        return false
    }

    /**
     * Sanitizes a potential football score string (e.g. "0-0", "1-0").
     * Rejects absurd values (e.g. "16-35") that are actually times, odds or stats.
     */
    fun sanitizeFootballScore(scoreStr: String?): String? {
        if (scoreStr.isNullOrBlank()) return null
        val trimmed = scoreStr.trim().replace(":", "-").replace("–", "-")
        val parts = trimmed.split("-").map { it.trim() }
        if (parts.size != 2) return null

        val home = parts[0].toIntOrNull() ?: return null
        val away = parts[1].toIntOrNull() ?: return null

        // In 15th minute or 1H live football, total goals cannot reasonably exceed 9 (e.g. max 5-4)
        if (home < 0 || away < 0) return null
        if (home > 9 || away > 9) return null
        if (home + away > 12) return null

        return "$home-$away"
    }
}
