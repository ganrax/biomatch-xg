package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiPredictiveService
import com.example.data.db.AnalysisRepository
import com.example.data.db.AppDatabase
import com.example.data.db.SavedAnalysisEntity
import com.example.data.engine.BioKineticsEngine
import com.example.data.model.ChatMessage
import com.example.data.model.ExtractedMatchData
import com.example.data.model.LiveHalfAnalysis
import com.example.data.model.LiveHalfInput
import com.example.data.model.PreMatchAnalysis
import com.example.data.model.PreMatchInput
import com.example.data.storage.LocalTipStorageManager
import com.example.data.update.AppUpdateManager
import com.example.data.update.UpdateInfo
import com.example.util.AnalysisDossierBuilder
import com.example.util.ImageUtils
import com.example.util.OcrMatchExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiService = GeminiPredictiveService(application)
    private val repository = AnalysisRepository(AppDatabase.getInstance(application).savedAnalysisDao())

    private val prefs = application.getSharedPreferences("biomatch_prefs", Context.MODE_PRIVATE)

    fun getCustomApiKey(): String {
        return prefs.getString("custom_gemini_api_key", "") ?: ""
    }

    fun saveCustomApiKey(key: String) {
        val trimmed = key.trim()
        prefs.edit().putString("custom_gemini_api_key", trimmed).apply()
        geminiService.customApiKey = trimmed
        _statusMessage.value = if (trimmed.isNotBlank()) "Gemini API kulcs sikeresen mentve!" else "API kulcs törölve."
    }

    fun isApiKeyConfigured(): Boolean {
        return geminiService.isKeyValid
    }

    val savedAnalyses: StateFlow<List<SavedAnalysisEntity>> = repository.allSaved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isExtractingData = MutableStateFlow(false)
    val isExtractingData: StateFlow<Boolean> = _isExtractingData.asStateFlow()

    private val _isResolvingTip = MutableStateFlow(false)
    val isResolvingTip: StateFlow<Boolean> = _isResolvingTip.asStateFlow()

    val localTipsFolderPath: String = LocalTipStorageManager.getLocalFolderPath(application)

    // --- In-App Updater & GitHub Sync ---
    private val updateManager = AppUpdateManager(application)
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    var githubRepoSlug: String
        get() = updateManager.githubRepo
        set(value) {
            updateManager.githubRepo = value
        }

    // --- Pre-Match State ---
    private val _preMatchInput = MutableStateFlow(PreMatchInput())
    val preMatchInput: StateFlow<PreMatchInput> = _preMatchInput.asStateFlow()

    private val _preMatchAnalysis = MutableStateFlow<PreMatchAnalysis?>(null)
    val preMatchAnalysis: StateFlow<PreMatchAnalysis?> = _preMatchAnalysis.asStateFlow()

    private val _isPreMatchLoading = MutableStateFlow(false)
    val isPreMatchLoading: StateFlow<Boolean> = _isPreMatchLoading.asStateFlow()

    // --- Live 1H State ---
    private val _liveHalfInput = MutableStateFlow(LiveHalfInput())
    val liveHalfInput: StateFlow<LiveHalfInput> = _liveHalfInput.asStateFlow()

    private val _liveHalfAnalysis = MutableStateFlow<LiveHalfAnalysis?>(null)
    val liveHalfAnalysis: StateFlow<LiveHalfAnalysis?> = _liveHalfAnalysis.asStateFlow()

    private val _isLiveHalfLoading = MutableStateFlow(false)
    val isLiveHalfLoading: StateFlow<Boolean> = _isLiveHalfLoading.asStateFlow()

    // --- Chatbot State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "model",
                message = "Üdvözlöm! A bioinformatikai és komplex dinamikai rendszerek módszertanára épülő prediktív AI-rendszer készen áll a futball-mérkőzések és élő félidők modellezésére. Kérdezzen bármilyen taktikai receptor-illeszkedésről, entrópia-szintről vagy fekete hattyú kockázatról!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatThinking = MutableStateFlow(false)
    val isChatThinking: StateFlow<Boolean> = _isChatThinking.asStateFlow()

    // --- UI Status Messages ---
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        // Compute initial default predictions locally so user sees instant high-fidelity visuals
        _preMatchAnalysis.value = BioKineticsEngine.generateLocalPreMatchAnalysis(_preMatchInput.value)
        _liveHalfAnalysis.value = BioKineticsEngine.generateLocalLiveHalfAnalysis(_liveHalfInput.value)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun showStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    // --- Pre-Match Updates & Analysis ---
    fun updatePreMatchInput(transform: (PreMatchInput) -> PreMatchInput) {
        _preMatchInput.value = transform(_preMatchInput.value)
        // Keep math synchronized in real-time
        val current = _preMatchAnalysis.value
        if (current != null) {
            val quickMath = BioKineticsEngine.computePreMatchMath(_preMatchInput.value)
            _preMatchAnalysis.value = current.copy(
                calculatedXg = quickMath.totalXg,
                mostLikelyInterval = quickMath.interval,
                over25Prob = quickMath.under25Prob.let { 1.0 - it },
                under25Prob = quickMath.under25Prob,
                bindingAffinityIndex = quickMath.bindingAffinity,
                scoreProbabilities = quickMath.topScores
            )
        }
    }

    fun runPreMatchAiAnalysis() {
        viewModelScope.launch {
            _isPreMatchLoading.value = true
            try {
                val result = geminiService.analyzePreMatch(_preMatchInput.value)
                _preMatchAnalysis.value = result
                _statusMessage.value = "A 4 fázisú pre-match modellezés sikeresen lefutott!"
            } catch (e: Exception) {
                _statusMessage.value = "Hiba történt: ${e.message}"
            } finally {
                _isPreMatchLoading.value = false
            }
        }
    }

    fun loadPreMatchPreset(name: String) {
        when (name) {
            "mancity_realmadrid" -> {
                _preMatchInput.value = PreMatchInput(
                    homeTeam = "Manchester City",
                    awayTeam = "Real Madrid",
                    context = "Bajnokok Ligája negyeddöntő, enyhe eső, nyitott taktikai felállás, vendégeknél kulcsfontosságú belső védő sérülés.",
                    homeBaseXg = 1.85,
                    awayBaseXg = 1.45,
                    tsszHome = 1.20,
                    tsszAway = 1.15,
                    dynamicBias = 0.15
                )
            }
            "inter_juventus" -> {
                _preMatchInput.value = PreMatchInput(
                    homeTeam = "Inter",
                    awayTeam = "Juventus",
                    context = "Serie A rangadó, bajnoki döntő tét, feszes mélyblokk, mindkét fél a védekezési struktúra megtartására fókuszál.",
                    homeBaseXg = 1.15,
                    awayBaseXg = 0.95,
                    tsszHome = 0.85,
                    tsszAway = 0.85,
                    dynamicBias = -0.20
                )
            }
            "arsenal_liverpool" -> {
                _preMatchInput.value = PreMatchInput(
                    homeTeam = "Arsenal",
                    awayTeam = "Liverpool",
                    context = "Premier League címmeccs, intenzív gegenpressing a középpályán, magas kinetikai tempó.",
                    homeBaseXg = 1.60,
                    awayBaseXg = 1.50,
                    tsszHome = 1.10,
                    tsszAway = 1.10,
                    dynamicBias = 0.05
                )
            }
        }
        _preMatchAnalysis.value = BioKineticsEngine.generateLocalPreMatchAnalysis(_preMatchInput.value)
    }

    // --- Live 1H Updates & Analysis ---
    fun updateLiveHalfInput(transform: (LiveHalfInput) -> LiveHalfInput) {
        _liveHalfInput.value = transform(_liveHalfInput.value)
        val current = _liveHalfAnalysis.value
        if (current != null) {
            val quickMath = BioKineticsEngine.computeLiveHalfMath(_liveHalfInput.value)
            _liveHalfAnalysis.value = current.copy(
                calculated1hXg = quickMath.calculated1hXg,
                dangerousAttackRatePerMin = quickMath.dangerousAttackRatePerMin,
                isSterileState = quickMath.isSterileState,
                under05Prob = quickMath.under05Prob,
                over05Prob = quickMath.over05Prob,
                under15Prob = quickMath.under15Prob,
                over15Prob = quickMath.over15Prob,
                dominantMarketDirection = quickMath.dominantMarketDirection,
                mostValuableMarket = quickMath.mostValuableMarket,
                mostLikelyHtScore = quickMath.mostLikelyHtScore
            )
        }
    }

    fun runLiveHalfAiAnalysis() {
        viewModelScope.launch {
            _isLiveHalfLoading.value = true
            try {
                val result = geminiService.analyzeLiveHalf(_liveHalfInput.value)
                _liveHalfAnalysis.value = result
                _statusMessage.value = "Az élő 1. félidős kinetikai modell sikeresen lefutott!"
            } catch (e: Exception) {
                _statusMessage.value = "Hiba történt: ${e.message}"
            } finally {
                _isLiveHalfLoading.value = false
            }
        }
    }

    fun loadLiveHalfPreset(name: String) {
        when (name) {
            "sterile_deep_block" -> {
                _liveHalfInput.value = LiveHalfInput(
                    homeTeam = "Arsenal",
                    awayTeam = "Juventus",
                    currentScore = "0-0",
                    shotsHome = 1,
                    shotsHomeOnTarget = 0,
                    shotsAway = 0,
                    shotsAwayOnTarget = 0,
                    dangerousAttacksHome = 8,
                    dangerousAttacksAway = 5,
                    cornersHome = 0,
                    cornersAway = 0,
                    possessionHome = 55,
                    possessionAway = 45,
                    foulsAndStoppages = "6 szabálytalanság, sok ápolás és taktikai időhúzás",
                    tacticalImpression = "Steril labdajáratás hátul, kompakt dupla védőfal, pontatlan passzok",
                    xg0To15 = 0.06,
                    xg16To45Base = 0.50,
                    kszMultiplier = 0.60
                )
            }
            "early_lead_parked" -> {
                _liveHalfInput.value = LiveHalfInput(
                    homeTeam = "Atlético Madrid",
                    awayTeam = "Sevilla",
                    currentScore = "1-0",
                    shotsHome = 2,
                    shotsHomeOnTarget = 1,
                    shotsAway = 1,
                    shotsAwayOnTarget = 0,
                    dangerousAttacksHome = 9,
                    dangerousAttacksAway = 6,
                    cornersHome = 1,
                    cornersAway = 0,
                    possessionHome = 40,
                    possessionAway = 60,
                    foulsAndStoppages = "5 szabálytalanság a középső harmadban",
                    tacticalImpression = "A hazai csapat korai gól után mélyen visszaállt védeni, vendégek meddőn körbeadogatnak",
                    xg0To15 = 0.45,
                    xg16To45Base = 0.40,
                    kszMultiplier = 0.65
                )
            }
            "open_chaos" -> {
                _liveHalfInput.value = LiveHalfInput(
                    homeTeam = "Liverpool",
                    awayTeam = "Bayern München",
                    currentScore = "0-0",
                    shotsHome = 4,
                    shotsHomeOnTarget = 2,
                    shotsAway = 3,
                    shotsAwayOnTarget = 1,
                    dangerousAttacksHome = 14,
                    dangerousAttacksAway = 11,
                    cornersHome = 3,
                    cornersAway = 2,
                    possessionHome = 52,
                    possessionAway = 48,
                    foulsAndStoppages = "2 szabálytalanság, folyamatos lüktető játék",
                    tacticalImpression = "Azonnali vertikális átmenetek, fellazult szélek, mindkét kapu előtt tiszta box xG helyzetek",
                    xg0To15 = 0.55,
                    xg16To45Base = 0.65,
                    kszMultiplier = 1.30
                )
            }
        }
        _liveHalfAnalysis.value = BioKineticsEngine.generateLocalLiveHalfAnalysis(_liveHalfInput.value)
    }

    // --- Chatbot interaction ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = "user", message = userText)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatThinking.value = true
            try {
                val reply = geminiService.chatConsultation(_chatMessages.value, userText)
                val modelMsg = ChatMessage(sender = "model", message = reply)
                _chatMessages.value = _chatMessages.value + modelMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = "model",
                    message = "A dinamikai modell feldolgozási hibát észlelt: ${e.message}. A kinetikai paraméterek változatlanok."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isChatThinking.value = false
            }
        }
    }

    // --- Database & Local Storage actions ---
    fun saveCurrentPreMatch() {
        val analysis = _preMatchAnalysis.value ?: return
        viewModelScope.launch {
            val concreteTip = if (analysis.concreteBetTip.isNotBlank()) analysis.concreteBetTip else "Gólszám: ${analysis.mostLikelyInterval}"
            val entity = SavedAnalysisEntity(
                type = "PRE_MATCH",
                homeTeam = analysis.homeTeam,
                awayTeam = analysis.awayTeam,
                calculatedXg = analysis.calculatedXg,
                dominantDirectionOrInterval = analysis.mostLikelyInterval,
                summary = "🎯 Tipp: $concreteTip | Affinitás: ${analysis.bindingAffinityIndex}/10 | U2.5: ${String.format("%.1f", analysis.under25Prob * 100)}%",
                fullReport = AnalysisDossierBuilder.buildFullPreMatchDossier(_preMatchInput.value, analysis),
                blackSwan = analysis.blackSwanFactor
            )
            val id = repository.saveAnalysis(entity)
            val savedWithId = entity.copy(id = id)
            LocalTipStorageManager.saveTipToLocalFolder(getApplication(), savedWithId)
            _statusMessage.value = "Pre-match tipp elmentve az adatbázisba és a BioMatchTips helyi mappába!"
        }
    }

    fun saveCurrentLiveHalf() {
        val analysis = _liveHalfAnalysis.value ?: return
        viewModelScope.launch {
            val concreteTip = if (analysis.concreteBetTip.isNotBlank()) analysis.concreteBetTip else analysis.mostValuableMarket
            val entity = SavedAnalysisEntity(
                type = "LIVE_1H",
                homeTeam = analysis.homeTeam,
                awayTeam = analysis.awayTeam,
                calculatedXg = analysis.calculated1hXg,
                dominantDirectionOrInterval = analysis.dominantMarketDirection,
                summary = "🎯 Tipp: $concreteTip | HT: ${analysis.mostLikelyHtScore} (Steril: ${if (analysis.isSterileState) "Igen" else "Nem"})",
                fullReport = AnalysisDossierBuilder.buildFullLiveHalfDossier(_liveHalfInput.value, analysis),
                blackSwan = analysis.blackSwanFactor
            )
            val id = repository.saveAnalysis(entity)
            val savedWithId = entity.copy(id = id)
            LocalTipStorageManager.saveTipToLocalFolder(getApplication(), savedWithId)
            _statusMessage.value = "Élő 1H tipp elmentve az adatbázisba és a BioMatchTips helyi mappába!"
        }
    }

    fun deleteSavedAnalysis(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            _statusMessage.value = "Elemzés törölve."
        }
    }

    fun resolveTipResult(entity: SavedAnalysisEntity, actualScore: String) {
        if (actualScore.isBlank()) {
            _statusMessage.value = "Kérlek add meg a tényleges végeredményt!"
            return
        }
        viewModelScope.launch {
            _isResolvingTip.value = true
            _statusMessage.value = "Eredmény értékelése és AI önkalibrációs tanulás folyamatban..."
            try {
                val (status, conclusion, learned) = geminiService.evaluateFinishedMatch(entity, actualScore)
                val updated = entity.copy(
                    actualScore = actualScore.trim(),
                    tipStatus = status,
                    conclusion = conclusion,
                    learnedInsight = learned,
                    isResolved = true
                )
                repository.updateAnalysis(updated)
                LocalTipStorageManager.saveTipToLocalFolder(getApplication(), updated)
                _statusMessage.value = "Tipp lezárva: $status! Eredmény és tanulság a BioMatchTips helyi mappába mentve."
            } catch (e: Exception) {
                _statusMessage.value = "Kiértékelési hiba: ${e.message}"
            } finally {
                _isResolvingTip.value = false
            }
        }
    }

    fun extractScoreFromScreenshot(context: Context, uri: Uri, onScoreExtracted: (String) -> Unit) {
        viewModelScope.launch {
            _isResolvingTip.value = true
            _statusMessage.value = "Végeredmény kiolvasása a meccs képernyőképéből (OCR)..."
            try {
                var extractedScore: String? = null

                // 1. Try fast on-device OCR
                val bitmap = ImageUtils.uriToBitmap(context, uri)
                if (bitmap != null) {
                    val ocrData = OcrMatchExtractor.extractFromBitmaps(listOf(bitmap), isLiveMode = true)
                    extractedScore = ocrData.score
                }

                // 2. If not found and Gemini is configured, try Gemini Vision
                if (extractedScore.isNullOrBlank() && geminiService.isKeyValid) {
                    val pair = ImageUtils.uriToBase64Jpeg(context, uri)
                    if (pair != null) {
                        val (base64, mime) = pair
                        extractedScore = geminiService.extractFinalScoreFromImage(base64, mime)
                    }
                }

                if (!extractedScore.isNullOrBlank()) {
                    onScoreExtracted(extractedScore)
                    _statusMessage.value = "Eredmény kiolvasva a képről: $extractedScore"
                } else {
                    _statusMessage.value = "Nem sikerült egyértelműen leolvasni az eredményt, kérlek írd be kézzel."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Kép kiolvasási hiba: ${e.message}"
            } finally {
                _isResolvingTip.value = false
            }
        }
    }

    fun exportAllTipsToLocalFolder() {
        viewModelScope.launch {
            val list = repository.allSaved
            val context = getApplication<Application>()
            var count = 0
            // Take current saved items
            val currentList = _savedAnalysesSnapshot()
            currentList.forEach { entity ->
                LocalTipStorageManager.saveTipToLocalFolder(context, entity)
                count++
            }
            _statusMessage.value = "$count db elemzés sikeresen frissítve a helyi BioMatchTips mappában!"
        }
    }

    private fun _savedAnalysesSnapshot(): List<SavedAnalysisEntity> {
        return savedAnalyses.value
    }

    // --- Automatic Data Extraction from Screenshot / Clipboard ---
    fun importScreenshot(context: Context, uri: Uri, isLiveMode: Boolean) {
        importScreenshots(context, listOf(uri), isLiveMode)
    }

    fun importScreenshots(context: Context, uris: List<Uri>, isLiveMode: Boolean) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _isExtractingData.value = true
            _statusMessage.value = if (uris.size > 1) {
                "${uris.size} db képernyőkép közvetlen kiolvasása (OCR + AI)..."
            } else {
                "Képernyőkép közvetlen kiolvasása (OCR + AI)..."
            }

            try {
                // 1. First: Load bitmaps and run local on-device ML Kit OCR
                val bitmaps = mutableListOf<android.graphics.Bitmap>()
                val images = mutableListOf<Pair<String, String>>()
                for (uri in uris) {
                    val bitmap = ImageUtils.uriToBitmap(context, uri)
                    if (bitmap != null) bitmaps.add(bitmap)
                    val pair = ImageUtils.uriToBase64Jpeg(context, uri)
                    if (pair != null) images.add(pair)
                }

                if (bitmaps.isEmpty() && images.isEmpty()) {
                    _statusMessage.value = "Nem sikerült megnyitni a kiválasztott képeket."
                    return@launch
                }

                val ocrData = if (bitmaps.isNotEmpty()) {
                    OcrMatchExtractor.extractFromBitmaps(bitmaps, isLiveMode)
                } else null

                // 2. Second: If Gemini API Key is configured, run Gemini Vision for deep synthesis
                val geminiData = if (images.isNotEmpty() && geminiService.isKeyValid) {
                    try {
                        geminiService.extractMatchDataFromMultipleImages(images, isLiveMode)
                    } catch (e: Throwable) {
                        null
                    }
                } else null

                // 3. Merge: prefer Gemini if present, fallback to OCR
                val finalData = ExtractedMatchData(
                    homeTeam = geminiData?.homeTeam ?: ocrData?.homeTeam,
                    awayTeam = geminiData?.awayTeam ?: ocrData?.awayTeam,
                    score = geminiData?.score ?: ocrData?.score,
                    minute = geminiData?.minute ?: ocrData?.minute,
                    shotsHome = geminiData?.shotsHome ?: ocrData?.shotsHome,
                    shotsAway = geminiData?.shotsAway ?: ocrData?.shotsAway,
                    shotsHomeOnTarget = geminiData?.shotsHomeOnTarget ?: ocrData?.shotsHomeOnTarget,
                    shotsAwayOnTarget = geminiData?.shotsAwayOnTarget ?: ocrData?.shotsAwayOnTarget,
                    dangerousAttacksHome = geminiData?.dangerousAttacksHome ?: ocrData?.dangerousAttacksHome,
                    dangerousAttacksAway = geminiData?.dangerousAttacksAway ?: ocrData?.dangerousAttacksAway,
                    cornersHome = geminiData?.cornersHome ?: ocrData?.cornersHome,
                    cornersAway = geminiData?.cornersAway ?: ocrData?.cornersAway,
                    possessionHome = geminiData?.possessionHome ?: ocrData?.possessionHome,
                    possessionAway = geminiData?.possessionAway ?: ocrData?.possessionAway,
                    context = geminiData?.context ?: ocrData?.context,
                    homeBaseXg = geminiData?.homeBaseXg ?: ocrData?.homeBaseXg,
                    awayBaseXg = geminiData?.awayBaseXg ?: ocrData?.awayBaseXg,
                    tacticalImpression = geminiData?.tacticalImpression ?: ocrData?.tacticalImpression
                )

                applyExtractedMatchData(finalData, isLiveMode)
                val h = finalData.homeTeam ?: "Hazai"
                val a = finalData.awayTeam ?: "Vendég"
                _statusMessage.value = "Képernyőképről sikeresen kiolvasva: $h vs $a"
            } catch (e: Exception) {
                _statusMessage.value = "Képfeldolgozási hiba: ${e.message}"
            } finally {
                _isExtractingData.value = false
            }
        }
    }

    fun importFromClipboard(clipboardText: String, isLiveMode: Boolean) {
        if (clipboardText.isBlank()) {
            _statusMessage.value = "A vágólap üres! Másolj ki meccs-szöveget vagy statisztikát."
            return
        }
        viewModelScope.launch {
            _isExtractingData.value = true
            _statusMessage.value = "Vágólap szövegének feldolgozása..."
            try {
                val data = geminiService.extractMatchDataFromText(clipboardText, isLiveMode)
                applyExtractedMatchData(data, isLiveMode)
                _statusMessage.value = "Vágólapról beolvasva: ${data.homeTeam ?: "Hazai"} vs ${data.awayTeam ?: "Vendég"}!"
            } catch (e: Exception) {
                _statusMessage.value = "Vágólap feldolgozási hiba: ${e.message}"
            } finally {
                _isExtractingData.value = false
            }
        }
    }

    private fun applyExtractedMatchData(data: ExtractedMatchData, isLiveMode: Boolean) {
        if (isLiveMode) {
            val current = _liveHalfInput.value
            val validatedScore = OcrMatchExtractor.sanitizeFootballScore(data.score) ?: current.currentScore
            val updated = current.copy(
                homeTeam = data.homeTeam ?: current.homeTeam,
                awayTeam = data.awayTeam ?: current.awayTeam,
                currentScore = validatedScore,
                shotsHome = data.shotsHome ?: current.shotsHome,
                shotsAway = data.shotsAway ?: current.shotsAway,
                shotsHomeOnTarget = data.shotsHomeOnTarget ?: current.shotsHomeOnTarget,
                shotsAwayOnTarget = data.shotsAwayOnTarget ?: current.shotsAwayOnTarget,
                dangerousAttacksHome = data.dangerousAttacksHome ?: current.dangerousAttacksHome,
                dangerousAttacksAway = data.dangerousAttacksAway ?: current.dangerousAttacksAway,
                cornersHome = data.cornersHome ?: current.cornersHome,
                cornersAway = data.cornersAway ?: current.cornersAway,
                possessionHome = data.possessionHome ?: current.possessionHome,
                possessionAway = data.possessionAway ?: current.possessionAway,
                tacticalImpression = data.tacticalImpression ?: current.tacticalImpression
            )
            _liveHalfInput.value = updated
            _liveHalfAnalysis.value = BioKineticsEngine.generateLocalLiveHalfAnalysis(updated)
        } else {
            val current = _preMatchInput.value
            val updated = current.copy(
                homeTeam = data.homeTeam ?: current.homeTeam,
                awayTeam = data.awayTeam ?: current.awayTeam,
                context = data.context ?: current.context,
                homeBaseXg = data.homeBaseXg ?: current.homeBaseXg,
                awayBaseXg = data.awayBaseXg ?: current.awayBaseXg
            )
            _preMatchInput.value = updated
            _preMatchAnalysis.value = BioKineticsEngine.generateLocalPreMatchAnalysis(updated)
        }
    }

    // --- App Update Methods ---
    fun checkForAppUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            if (isManual) {
                _statusMessage.value = "Frissítések keresése a GitHub tárolóban (${updateManager.githubRepo})..."
            }
            try {
                val info = updateManager.checkForUpdates()
                if (isManual) {
                    _updateInfo.value = info
                    if (info.hasUpdate) {
                        _statusMessage.value = "Új verzió elérhető: v${info.latestVersionName}!"
                    } else {
                        _statusMessage.value = "A legfrissebb verziót használod (v${info.currentVersionName})."
                    }
                } else {
                    // Auto-check on launch: only popup if there is genuinely an update and it wasn't dismissed
                    if (info.hasUpdate && !updateManager.isVersionDismissed(info.latestVersionName)) {
                        _updateInfo.value = info
                    } else {
                        _updateInfo.value = null
                    }
                }
            } catch (e: Exception) {
                if (isManual) {
                    _statusMessage.value = "Frissítés-ellenőrzési hiba: ${e.message}"
                }
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun downloadAndInstallUpdate(downloadUrl: String) {
        viewModelScope.launch {
            _downloadProgress.value = 0f
            _statusMessage.value = "Új verzió letöltése folyamatban..."
            try {
                val file = updateManager.downloadApk(downloadUrl) { progress ->
                    _downloadProgress.value = progress
                }
                _downloadProgress.value = null
                _statusMessage.value = "Letöltés kész! Telepítő megnyitása..."
                updateManager.promptInstall(file)
            } catch (e: Exception) {
                _downloadProgress.value = null
                _statusMessage.value = "Hiba a letöltés során: ${e.message}"
            }
        }
    }

    fun dismissUpdateDialog(versionToDismiss: String? = null) {
        if (versionToDismiss != null) {
            updateManager.dismissVersion(versionToDismiss)
        }
        _updateInfo.value = null
    }
}
