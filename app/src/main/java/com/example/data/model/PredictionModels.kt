package com.example.data.model

data class PreMatchInput(
    val homeTeam: String = "Manchester City",
    val awayTeam: String = "Real Madrid",
    val context: String = "Bajnokok Ligája negyeddöntő, enyhe eső, vendégeknél kulcsfontosságú belső védő sérülés miatt hiányzik, magas tét.",
    val homeBaseXg: Double = 1.75,
    val awayBaseXg: Double = 1.35,
    val tsszHome: Double = 1.15,
    val tsszAway: Double = 1.05,
    val dynamicBias: Double = 0.10
)

data class ScoreProbability(
    val homeGoals: Int,
    val awayGoals: Int,
    val probability: Double
) {
    val scoreText: String get() = "$homeGoals - $awayGoals"
    val formattedPercent: String get() = "${(probability * 100).coerceAtLeast(0.1).let { String.format("%.1f", it) }}%"
}

data class PreMatchAnalysis(
    val id: Long = 0,
    val homeTeam: String,
    val awayTeam: String,
    val calculatedXg: Double,
    val mostLikelyInterval: String,
    val over25Prob: Double,
    val under25Prob: Double,
    val bindingAffinityIndex: Int, // 1 to 10 scale
    val concreteBetTip: String = "", // e.g. "Mérkőzés Kevesebb mint 2.5 gól (Under 2.5)"
    val secondaryBetTip: String = "", // e.g. "Mindkét csapat szerez gólt: NEM"
    val blackSwanFactor: String,
    val phase1MolecularDocking: String,
    val phase2MetabolicKinetics: String,
    val phase3FormulaExplanation: String,
    val phase4MonteCarloText: String,
    val scoreProbabilities: List<ScoreProbability> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveHalfInput(
    val homeTeam: String = "Arsenal",
    val awayTeam: String = "Juventus",
    val currentScore: String = "0-0",
    val shotsHome: Int = 1,
    val shotsHomeOnTarget: Int = 0,
    val shotsAway: Int = 0,
    val shotsAwayOnTarget: Int = 0,
    val dangerousAttacksHome: Int = 8,
    val dangerousAttacksAway: Int = 5,
    val cornersHome: Int = 0,
    val cornersAway: Int = 0,
    val possessionHome: Int = 55,
    val possessionAway: Int = 45,
    val foulsAndStoppages: String = "6 szabálytalanság, sok ápolás/időhúzás",
    val tacticalImpression: String = "Steril labdajáratás hátul, tömör védőfal, pontatlan passzok, agresszív letámadás a középpályán",
    val xg0To15: Double = 0.08,
    val xg16To45Base: Double = 0.52,
    val kszMultiplier: Double = 0.65 // [0.5 - 0.8] Under, [0.9 - 1.1] Balanced, [1.2 - 1.4] Over
)

data class LiveHalfAnalysis(
    val id: Long = 0,
    val homeTeam: String,
    val awayTeam: String,
    val calculated1hXg: Double,
    val dominantMarketDirection: String, // "ERŐSEN UNDER-PROFILÚ" or "OVER-PROFILÚ"
    val mostValuableMarket: String, // e.g. "1H Under 0.5 gól" or "1H Under 1.5 gól"
    val concreteBetTip: String = "", // e.g. "1. Félidő Under 0.5 gól (Szünetben 0–0 marad)"
    val secondaryBetTip: String = "", // e.g. "Mérkőzés Under 2.5 gól | BTTS: NEM"
    val under05Prob: Double,
    val over05Prob: Double,
    val under15Prob: Double,
    val over15Prob: Double,
    val mostLikelyHtScore: String,
    val blackSwanFactor: String,
    val phase1ReactionAndInhibition: String,
    val phase2KineticFlux: String,
    val phase3FormulaDetails: String,
    val phase4MarketText: String,
    val dangerousAttackRatePerMin: Double,
    val isSterileState: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "model"
    val message: String,
    val thinking: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
