package com.example.data.model

data class ExtractedMatchData(
    val homeTeam: String? = null,
    val awayTeam: String? = null,
    val score: String? = null,
    val minute: Int? = null,
    val shotsHome: Int? = null,
    val shotsAway: Int? = null,
    val shotsHomeOnTarget: Int? = null,
    val shotsAwayOnTarget: Int? = null,
    val dangerousAttacksHome: Int? = null,
    val dangerousAttacksAway: Int? = null,
    val cornersHome: Int? = null,
    val cornersAway: Int? = null,
    val possessionHome: Int? = null,
    val possessionAway: Int? = null,
    val context: String? = null,
    val homeBaseXg: Double? = null,
    val awayBaseXg: Double? = null,
    val tacticalImpression: String? = null
)
