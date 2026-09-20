package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_analyses")
data class SavedAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "PRE_MATCH" or "LIVE_1H"
    val homeTeam: String,
    val awayTeam: String,
    val calculatedXg: Double,
    val dominantDirectionOrInterval: String,
    val summary: String,
    val fullReport: String,
    val blackSwan: String,
    val actualScore: String? = null,
    val tipStatus: String = "PENDING", // "PENDING", "WON", "LOST", "VOID"
    val conclusion: String? = null,
    val learnedInsight: String? = null,
    val isResolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

