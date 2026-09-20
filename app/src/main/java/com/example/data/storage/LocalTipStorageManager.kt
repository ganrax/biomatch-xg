package com.example.data.storage

import android.content.Context
import com.example.data.db.SavedAnalysisEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalTipStorageManager {

    private const val FOLDER_NAME = "BioMatchTips"

    fun getTipsDirectory(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val folder = File(baseDir, FOLDER_NAME)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    suspend fun saveTipToLocalFolder(context: Context, entity: SavedAnalysisEntity): File =
        withContext(Dispatchers.IO) {
            val dir = getTipsDirectory(context)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(entity.timestamp))
            val safeName = "${entity.homeTeam}_vs_${entity.awayTeam}"
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(35)
            val fileName = "tipp_${entity.id}_${dateStr}_$safeName.txt"
            val file = File(dir, fileName)

            val content = buildString {
                appendLine("=================================================================")
                appendLine("          BIOMATCH xG - LOKÁLIS TIPP ÉS ELEMZÉS JELENTÉS        ")
                appendLine("=================================================================")
                appendLine("Azonosító: ${entity.id}")
                appendLine("Dátum: ${SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(Date(entity.timestamp))}")
                appendLine("Típus: ${if (entity.type == "PRE_MATCH") "Pre-Match Kinetikai Modellezés" else "Élő 1. Félidő (15. perc) Elemzés"}")
                appendLine("Mérkőzés: ${entity.homeTeam} vs. ${entity.awayTeam}")
                appendLine("-----------------------------------------------------------------")
                appendLine("Számított xG: ${String.format("%.2f", entity.calculatedXg)}")
                appendLine("Prediktált Irány / Góltartomány: ${entity.dominantDirectionOrInterval}")
                appendLine("Összegzés: ${entity.summary}")
                appendLine("-----------------------------------------------------------------")
                appendLine("TIPP STÁTUSZ: ${entity.tipStatus} (Lezárva: ${if (entity.isResolved) "IGEN" else "FÜGGŐBEN"})")
                appendLine("Végeredmény: ${entity.actualScore ?: "Még nincs rögzítve"}")
                if (!entity.conclusion.isNullOrBlank()) {
                    appendLine()
                    appendLine("[AI UTÓLAGOS KONKLÚZIÓ]")
                    appendLine(entity.conclusion)
                }
                if (!entity.learnedInsight.isNullOrBlank()) {
                    appendLine()
                    appendLine("[ÖNKALIBRÁCIÓ ÉS TANULÁSI LEVONT KÖVETKEZTETÉS]")
                    appendLine(entity.learnedInsight)
                }
                appendLine("-----------------------------------------------------------------")
                appendLine("Fekete Hattyú Kockázati Tényező:")
                appendLine(entity.blackSwan)
                appendLine("=================================================================")
                appendLine("TELJES TUDOMÁNYOS JELENTÉS:")
                appendLine(entity.fullReport)
                appendLine("=================================================================")
            }

            file.writeText(content)

            // Also write a compact json format for easy machine parsing
            val jsonFile = File(dir, "tipp_${entity.id}_$safeName.json")
            val json = JSONObject().apply {
                put("id", entity.id)
                put("type", entity.type)
                put("homeTeam", entity.homeTeam)
                put("awayTeam", entity.awayTeam)
                put("calculatedXg", entity.calculatedXg)
                put("predictedDirection", entity.dominantDirectionOrInterval)
                put("tipStatus", entity.tipStatus)
                put("actualScore", entity.actualScore ?: "")
                put("conclusion", entity.conclusion ?: "")
                put("learnedInsight", entity.learnedInsight ?: "")
                put("timestamp", entity.timestamp)
            }
            jsonFile.writeText(json.toString(2))

            file
        }

    fun getLocalTipsCount(context: Context): Int {
        val dir = getTipsDirectory(context)
        return dir.listFiles { f -> f.extension == "txt" }?.size ?: 0
    }

    fun getLocalFolderPath(context: Context): String {
        return getTipsDirectory(context).absolutePath
    }
}
