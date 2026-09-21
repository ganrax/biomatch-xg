package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SavedAnalysisEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.BioViolet
import com.example.ui.theme.BlackSwanRose
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedList by viewModel.savedAnalyses.collectAsState()
    val isResolving by viewModel.isResolvingTip.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedEntityForDetails by remember { mutableStateOf<SavedAnalysisEntity?>(null) }
    var entityToResolve by remember { mutableStateOf<SavedAnalysisEntity?>(null) }
    var entityToEdit by remember { mutableStateOf<SavedAnalysisEntity?>(null) }
    var entityToDelete by remember { mutableStateOf<SavedAnalysisEntity?>(null) }

    val filteredList = when (selectedFilter) {
        "PENDING" -> savedList.filter { !it.isResolved }
        "WON" -> savedList.filter { it.tipStatus == "WON" }
        "LOST" -> savedList.filter { it.tipStatus == "LOST" }
        "PRE_MATCH" -> savedList.filter { it.type == "PRE_MATCH" }
        "LIVE_1H" -> savedList.filter { it.type == "LIVE_1H" }
        else -> savedList
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepVoid)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TIPP ARCHÍVUM & TANULÁSI MODELL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Helyi adatbázis & Offline mappa szinkronizáció",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Local Storage Folder Info Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = AnomalyAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HELYI MAPPA (NEM FELHŐBEN)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.syncTipsFromLocalFolder() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Beolvasás",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.exportAllTipsToLocalFolder() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Exportálás",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mappa: ${viewModel.localTipsFolderPath}/BioMatchTips",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Minden mentett tipp külön .txt és .json formátumban helyben tárolódik a telefonodon!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Mind (${savedList.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("Eredményre vár (${savedList.count { !it.isResolved }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AnomalyAmber.copy(alpha = 0.2f),
                            selectedLabelColor = AnomalyAmber
                        ),
                        modifier = Modifier.testTag("filter_pending")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "WON",
                        onClick = { selectedFilter = "WON" },
                        label = { Text("Nyertes (${savedList.count { it.tipStatus == "WON" }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KineticEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = KineticEmerald
                        ),
                        modifier = Modifier.testTag("filter_won")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "LOST",
                        onClick = { selectedFilter = "LOST" },
                        label = { Text("Vesztes (${savedList.count { it.tipStatus == "LOST" }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlackSwanRose.copy(alpha = 0.2f),
                            selectedLabelColor = BlackSwanRose
                        ),
                        modifier = Modifier.testTag("filter_lost")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "PRE_MATCH",
                        onClick = { selectedFilter = "PRE_MATCH" },
                        label = { Text("Pre-Match") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricBlue
                        ),
                        modifier = Modifier.testTag("filter_prematch")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "LIVE_1H",
                        onClick = { selectedFilter = "LIVE_1H" },
                        label = { Text("Élő 1. Félidő") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KineticEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = KineticEmerald
                        ),
                        modifier = Modifier.testTag("filter_live")
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nincsenek megjeleníthető tippek ebben a kategóriában.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                val isPre = item.type == "PRE_MATCH"
                val typeBadgeColor = if (isPre) ElectricBlue else KineticEmerald
                val typeLabel = if (isPre) "PRE-MATCH" else "ÉLŐ 1H"
                val formattedDate = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(item.timestamp))

                val (statusColor, statusText, statusIcon) = when (item.tipStatus) {
                    "WON" -> Triple(KineticEmerald, "NYERTES TIPP", Icons.Default.CheckCircle)
                    "LOST" -> Triple(BlackSwanRose, "VESZTES TIPP", Icons.Default.Close)
                    else -> Triple(AnomalyAmber, "EREDMÉNYRE VÁR", Icons.Default.HourglassEmpty)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (item.tipStatus == "WON") KineticEmerald.copy(alpha = 0.6f)
                            else if (item.tipStatus == "LOST") BlackSwanRose.copy(alpha = 0.6f)
                            else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedEntityForDetails = item }
                        .padding(16.dp)
                        .testTag("history_item_${item.id}")
                ) {
                    Column {
                        // Header row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(typeBadgeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = typeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = typeBadgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                IconButton(
                                    onClick = { entityToEdit = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Szerkesztés",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val cardSummary = buildString {
                                            appendLine("📋 BioMatch Mentett Elemzés: ${item.homeTeam} vs. ${item.awayTeam}")
                                            appendLine("Típus: $typeLabel ($formattedDate)")
                                            appendLine("Prediktált xG: ${String.format("%.2f", item.calculatedXg)} | ${item.dominantDirectionOrInterval}")
                                            if (!item.actualScore.isNullOrBlank()) {
                                                appendLine("Végeredmény: ${item.actualScore} ($statusText)")
                                            }
                                            if (!item.conclusion.isNullOrBlank()) {
                                                appendLine("Konklúzió: ${item.conclusion}")
                                            }
                                        }
                                        clipboardManager.setText(AnnotatedString(cardSummary))
                                        viewModel.showStatusMessage("Archív tétel kimásolva a vágólapra! 📋")
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Tipp másolása",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { entityToDelete = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Törlés",
                                        tint = BlackSwanRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${item.homeTeam} vs. ${item.awayTeam}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Prediktált xG: ${String.format("%.2f", item.calculatedXg)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.dominantDirectionOrInterval,
                                style = MaterialTheme.typography.bodyMedium,
                                color = typeBadgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!item.actualScore.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DeepVoid.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Végeredmény: ${item.actualScore}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Conclusion and Learned Insight Preview if available
                        if (!item.conclusion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BioViolet.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, BioViolet.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI KONKLÚZIÓ & TANULÁS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.conclusion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                    if (!item.learnedInsight.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = AnomalyAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Önkalibráció: ${item.learnedInsight}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AnomalyAmber,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Button to enter outcome if still pending
                        if (!item.isResolved) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { entityToResolve = item },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_resolve_tip_${item.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AnomalyAmber,
                                    contentColor = DeepVoid
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Eredmény rögzítése & AI Tanulás",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // --- Dialog to Resolve Tip Outcome (Manual or Screenshot) ---
    entityToResolve?.let { entity ->
        ResolveTipDialog(
            entity = entity,
            isResolving = isResolving,
            onDismiss = { entityToResolve = null },
            onResolve = { score ->
                viewModel.resolveTipResult(entity, score)
                entityToResolve = null
            },
            onExtractFromScreenshot = { uri, callback ->
                viewModel.extractScoreFromScreenshot(context, uri, callback)
            }
        )
    }

    // --- Dialog to Edit Tip Details ---
    entityToEdit?.let { entity ->
        EditTipDialog(
            entity = entity,
            onDismiss = { entityToEdit = null },
            onSave = { home, away, direction, score, status, conclusion, learned ->
                viewModel.updateSavedAnalysisDetails(
                    entity = entity,
                    homeTeam = home,
                    awayTeam = away,
                    dominantDirection = direction,
                    actualScore = score,
                    tipStatus = status,
                    conclusion = conclusion,
                    learnedInsight = learned
                )
                entityToEdit = null
            }
        )
    }

    // --- Dialog to Confirm Deletion ---
    entityToDelete?.let { entity ->
        DeleteConfirmDialog(
            entity = entity,
            onDismiss = { entityToDelete = null },
            onConfirm = {
                viewModel.deleteSavedAnalysis(entity.id)
                entityToDelete = null
            }
        )
    }

    // --- Full Detail Dialog ---
    selectedEntityForDetails?.let { entity ->
        AlertDialog(
            onDismissRequest = { selectedEntityForDetails = null },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            entityToEdit = entity
                            selectedEntityForDetails = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Szerkesztés")
                    }

                    OutlinedButton(
                        onClick = {
                            entityToDelete = entity
                            selectedEntityForDetails = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BlackSwanRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Törlés")
                    }

                    OutlinedButton(
                        onClick = {
                            val fullDetailText = buildString {
                                if (entity.fullReport.contains("0. LÉPÉS")) {
                                    appendLine(entity.fullReport)
                                    if (!entity.actualScore.isNullOrBlank()) {
                                        appendLine()
                                        appendLine("▶ 6. LÉPÉS: UTÓLAGOS KIÉRTÉKELÉS & AI ÖNKALIBRÁCIÓ")
                                        appendLine("───────────────────────────────────────────────────────────────────────────")
                                        appendLine("• Rögzített végeredmény: ${entity.actualScore} (Tipp státusz: ${entity.tipStatus})")
                                        if (!entity.conclusion.isNullOrBlank()) {
                                            appendLine("• AI Szakértői Konklúzió:\n${entity.conclusion}")
                                        }
                                        if (!entity.learnedInsight.isNullOrBlank()) {
                                            appendLine("• Önképzési / Tanulási Paraméterek:\n${entity.learnedInsight}")
                                        }
                                        appendLine("═══════════════════════════════════════════════════════════════════════════")
                                    }
                                } else {
                                    appendLine("═══════════════════════════════════════")
                                    appendLine("📋 BIOMATCH ARCHÍV ELEMZÉS")
                                    appendLine("Mérkőzés: ${entity.homeTeam} vs. ${entity.awayTeam}")
                                    appendLine("Típus: ${if (entity.type == "PRE_MATCH") "Pre-Match" else "Élő 1. Félidő"}")
                                    appendLine("xG: ${String.format("%.2f", entity.calculatedXg)} | Irány: ${entity.dominantDirectionOrInterval}")
                                    if (!entity.actualScore.isNullOrBlank()) {
                                        appendLine("Végeredmény: ${entity.actualScore} (${entity.tipStatus})")
                                    }
                                    if (!entity.conclusion.isNullOrBlank()) {
                                        appendLine("\nAI KONKLÚZIÓ:\n${entity.conclusion}")
                                    }
                                    if (!entity.learnedInsight.isNullOrBlank()) {
                                        appendLine("\nTANULT PARAMÉTEREK:\n${entity.learnedInsight}")
                                    }
                                    appendLine("\nFEKETE HATTYÚ:\n${entity.blackSwan}")
                                    appendLine("\nTELJES FÁZISANALÍZIS:\n${entity.fullReport}")
                                    appendLine("═══════════════════════════════════════")
                                }
                            }
                            clipboardManager.setText(AnnotatedString(fullDetailText))
                            viewModel.showStatusMessage("Teljes döntési dosszié kimásolva! 📋")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Másolás")
                    }

                    Button(
                        onClick = { selectedEntityForDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepVoid)
                    ) {
                        Text("Bezárás")
                    }
                }
            },
            title = {
                Text(
                    text = "${entity.homeTeam} vs. ${entity.awayTeam}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Típus: ${if (entity.type == "PRE_MATCH") "Pre-Match Elemzés" else "Élő 1. Félidős Elemzés"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "xG: ${String.format("%.2f", entity.calculatedXg)} | ${entity.dominantDirectionOrInterval}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (!entity.actualScore.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Végeredmény: ${entity.actualScore} (Státusz: ${entity.tipStatus})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (entity.tipStatus == "WON") KineticEmerald else BlackSwanRose
                            )
                        }

                        if (!entity.conclusion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "AI UTÓLAGOS KONKLÚZIÓ:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entity.conclusion,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                        }

                        if (!entity.learnedInsight.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ÖNKALIBRÁCIÓ & TANULT PARAMÉTEREK:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AnomalyAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entity.learnedInsight,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Fekete Hattyú Anomália:\n${entity.blackSwan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlackSwanRose
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "TELJES FÁZISANALÍZIS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entity.fullReport,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ResolveTipDialog(
    entity: SavedAnalysisEntity,
    isResolving: Boolean,
    onDismiss: () -> Unit,
    onResolve: (String) -> Unit,
    onExtractFromScreenshot: (Uri, (String) -> Unit) -> Unit
) {
    var scoreInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onExtractFromScreenshot(uri) { extractedScore ->
                scoreInput = extractedScore
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isResolving) onDismiss() },
        title = {
            Text(
                text = "Eredmény Rögzítése & AI Tanulás",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${entity.homeTeam} vs. ${entity.awayTeam}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Predikció: ${entity.dominantDirectionOrInterval} (xG: ${String.format("%.2f", entity.calculatedXg)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "1. Opció: Kiolvasás Képernyőképről",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isResolving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepVoid),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📸 Eredmény Képernyőkép Beolvasása",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. Opció: Kézi Eredmény Megadás",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = scoreInput,
                    onValueChange = { scoreInput = it },
                    label = { Text("Végeredmény (pl. 2-1 vagy HT 0-0)") },
                    placeholder = { Text("2-1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                AnimatedVisibility(visible = isResolving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AnomalyAmber
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI elemzi a kimenetelt és frissíti a modell tanulási memóriáját...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AnomalyAmber
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onResolve(scoreInput) },
                enabled = !isResolving && scoreInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AnomalyAmber, contentColor = DeepVoid)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kiértékelés & Tanulás")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isResolving
            ) {
                Text("Mégse", color = TextMuted)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditTipDialog(
    entity: SavedAnalysisEntity,
    onDismiss: () -> Unit,
    onSave: (
        homeTeam: String,
        awayTeam: String,
        dominantDirection: String,
        actualScore: String?,
        tipStatus: String,
        conclusion: String?,
        learnedInsight: String?
    ) -> Unit
) {
    var homeTeam by remember { mutableStateOf(entity.homeTeam) }
    var awayTeam by remember { mutableStateOf(entity.awayTeam) }
    var dominantDirection by remember { mutableStateOf(entity.dominantDirectionOrInterval) }
    var actualScore by remember { mutableStateOf(entity.actualScore ?: "") }
    var tipStatus by remember { mutableStateOf(entity.tipStatus) }
    var conclusion by remember { mutableStateOf(entity.conclusion ?: "") }
    var learnedInsight by remember { mutableStateOf(entity.learnedInsight ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tipp Szerkesztése",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ID: #${entity.id} | ${if (entity.type == "PRE_MATCH") "Pre-Match" else "Élő 1H"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = homeTeam,
                    onValueChange = { homeTeam = it },
                    label = { Text("Hazai csapat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = awayTeam,
                    onValueChange = { awayTeam = it },
                    label = { Text("Vendég csapat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = dominantDirection,
                    onValueChange = { dominantDirection = it },
                    label = { Text("Fő Ajánlás / Piac / Irány") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = actualScore,
                    onValueChange = { actualScore = it },
                    label = { Text("Tényleges eredmény (pl. 2-1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text(
                    text = "Kimenetel / Validáció Státusz:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PENDING" to "Függőben", "WON" to "NYERT", "LOST" to "VESZTETT").forEach { (statusKey, statusLabel) ->
                        FilterChip(
                            selected = (tipStatus == statusKey),
                            onClick = { tipStatus = statusKey },
                            label = { Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (statusKey) {
                                    "WON" -> KineticEmerald
                                    "LOST" -> BlackSwanRose
                                    else -> AnomalyAmber
                                },
                                selectedLabelColor = DeepVoid
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = conclusion,
                    onValueChange = { conclusion = it },
                    label = { Text("AI Retrospektív Konklúzió") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = learnedInsight,
                    onValueChange = { learnedInsight = it },
                    label = { Text("Tanulási Paraméterek / Modell Insight") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        homeTeam.trim(),
                        awayTeam.trim(),
                        dominantDirection.trim(),
                        actualScore.trim().ifEmpty { null },
                        tipStatus,
                        conclusion.trim().ifEmpty { null },
                        learnedInsight.trim().ifEmpty { null }
                    )
                },
                enabled = homeTeam.isNotBlank() && awayTeam.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepVoid)
            ) {
                Text("Mentés", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse", color = TextMuted)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DeleteConfirmDialog(
    entity: SavedAnalysisEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = BlackSwanRose, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tipp Törlése",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Biztosan törölni szeretnéd a következő mentett elemzést?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${entity.homeTeam} vs. ${entity.awayTeam}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ajánlás: ${entity.dominantDirectionOrInterval}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Figyelem: A tétel az adatbázisból és a helyi fájlmappából (.txt / .json) is véglegesen eltávolításra kerül.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackSwanRose
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BlackSwanRose, contentColor = DeepVoid)
            ) {
                Text("Végleges törlés", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse", color = TextMuted)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

