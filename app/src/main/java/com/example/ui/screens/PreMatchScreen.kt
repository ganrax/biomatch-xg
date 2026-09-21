package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.BindingAffinityGauge
import com.example.ui.components.BlackSwanAlertCard
import com.example.ui.components.PhaseCard
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.QuickImportBar
import com.example.ui.components.ScoreMatrixGrid
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.BioViolet
import com.example.ui.theme.BlackSwanRose
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PreMatchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.preMatchInput.collectAsState()
    val analysis by viewModel.preMatchAnalysis.collectAsState()
    val isLoading by viewModel.isPreMatchLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var showAdvancedParams by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepVoid)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Screen Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PRE-MATCH GÓLSZÁM MODELLEZŐ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Molekuláris dokkolás & Komplex kinetikai fizika",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan
                    )
                }
            }
        }

        // Quick Import Bar (Screenshot OCR & Clipboard Paste)
        item {
            QuickImportBar(
                viewModel = viewModel,
                isLiveMode = false
            )
        }

        // Preset Chips
        item {
            Text(
                text = "Gyors Taktikai Modellek:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = input.homeTeam.contains("City"),
                        onClick = { viewModel.loadPreMatchPreset("mancity_realmadrid") },
                        label = { Text("Man City vs Real Madrid (Nyílt affinitás)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("preset_mancity")
                    )
                }
                item {
                    FilterChip(
                        selected = input.homeTeam == "Inter",
                        onClick = { viewModel.loadPreMatchPreset("inter_juventus") },
                        label = { Text("Inter vs Juventus (Sakkjátszma)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricBlue
                        ),
                        modifier = Modifier.testTag("preset_inter")
                    )
                }
                item {
                    FilterChip(
                        selected = input.homeTeam == "Arsenal",
                        onClick = { viewModel.loadPreMatchPreset("arsenal_liverpool") },
                        label = { Text("Arsenal vs Liverpool (Gegenpressing)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KineticEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = KineticEmerald
                        ),
                        modifier = Modifier.testTag("preset_arsenal")
                    )
                }
            }
        }

        // Team and Context Inputs
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = input.homeTeam,
                            onValueChange = { newName -> viewModel.updatePreMatchInput { it.copy(homeTeam = newName) } },
                            label = { Text("Hazai csapat") },
                            leadingIcon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = NeonCyan) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_team_input")
                        )
                        OutlinedTextField(
                            value = input.awayTeam,
                            onValueChange = { newName -> viewModel.updatePreMatchInput { it.copy(awayTeam = newName) } },
                            label = { Text("Vendég csapat") },
                            leadingIcon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = ElectricBlue) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("away_team_input")
                        )
                    }

                    OutlinedTextField(
                        value = input.context,
                        onValueChange = { newCtx -> viewModel.updatePreMatchInput { it.copy(context = newCtx) } },
                        label = { Text("Kontextus / Hiányzók / Tét / Időjárás") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("context_input")
                    )

                    // Toggle for advanced sliders
                    OutlinedButton(
                        onClick = { showAdvancedParams = !showAdvancedParams },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Text(if (showAdvancedParams) "Fejlett Kinetikai Paraméterek Elrejtése ▲" else "Fejlett Kinetikai Paraméterek Megjelenítése (TSSz, DT) ▼")
                    }

                    AnimatedVisibility(visible = showAdvancedParams) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Home base xG
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Hazai Bázis xG (H_xG):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(String.format("%.2f", input.homeBaseXg), style = MaterialTheme.typography.bodySmall, color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = input.homeBaseXg.toFloat(),
                                    onValueChange = { v -> viewModel.updatePreMatchInput { it.copy(homeBaseXg = v.toDouble()) } },
                                    valueRange = 0.4f..3.5f,
                                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                                )
                            }

                            // Away base xG
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Vendég Bázis xG (V_xG):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(String.format("%.2f", input.awayBaseXg), style = MaterialTheme.typography.bodySmall, color = ElectricBlue, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = input.awayBaseXg.toFloat(),
                                    onValueChange = { v -> viewModel.updatePreMatchInput { it.copy(awayBaseXg = v.toDouble()) } },
                                    valueRange = 0.4f..3.5f,
                                    colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                                )
                            }

                            // TSSz Home (0.8 - 1.3)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Taktikai Súrlódási Szorzó Hazai (TSSz):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(String.format("%.2f", input.tsszHome), style = MaterialTheme.typography.bodySmall, color = KineticEmerald, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = input.tsszHome.toFloat(),
                                    onValueChange = { v -> viewModel.updatePreMatchInput { it.copy(tsszHome = v.toDouble()) } },
                                    valueRange = 0.8f..1.3f,
                                    colors = SliderDefaults.colors(thumbColor = KineticEmerald, activeTrackColor = KineticEmerald)
                                )
                            }

                            // TSSz Away (0.8 - 1.3)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Taktikai Súrlódási Szorzó Vendég (TSSz):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(String.format("%.2f", input.tsszAway), style = MaterialTheme.typography.bodySmall, color = KineticEmerald, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = input.tsszAway.toFloat(),
                                    onValueChange = { v -> viewModel.updatePreMatchInput { it.copy(tsszAway = v.toDouble()) } },
                                    valueRange = 0.8f..1.3f,
                                    colors = SliderDefaults.colors(thumbColor = KineticEmerald, activeTrackColor = KineticEmerald)
                                )
                            }

                            // Dynamic Bias DT (-0.5 to +0.5)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Dinamikai Torzítás DT (időjárás, tét):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(String.format("%+.2f", input.dynamicBias), style = MaterialTheme.typography.bodySmall, color = AnomalyAmber, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = input.dynamicBias.toFloat(),
                                    onValueChange = { v -> viewModel.updatePreMatchInput { it.copy(dynamicBias = v.toDouble()) } },
                                    valueRange = -0.5f..0.5f,
                                    colors = SliderDefaults.colors(thumbColor = AnomalyAmber, activeTrackColor = AnomalyAmber)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action CTA
        item {
            Button(
                onClick = { viewModel.runPreMatchAiAnalysis() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepVoid),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("run_prematch_analysis_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DeepVoid)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("MOLEKULÁRIS DOKKOLÁS ÉS ELEMZÉS...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("FUTTATÁS: 4 FÁZISÚ TUDOMÁNYOS ELEMZÉS", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Results Section
        analysis?.let { res ->
            // Concrete Betting Tip Banner
            item {
                val isUnder = res.calculatedXg <= 2.45 || res.under25Prob >= 0.52
                val bannerColor = if (isUnder) KineticEmerald else NeonCyan
                val bannerBg = if (isUnder) Color(0xFF042F24) else Color(0xFF052538)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg, RoundedCornerShape(20.dp))
                        .border(2.dp, bannerColor, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                        .testTag("prematch_bet_tip_banner")
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = bannerColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PREDIKTÍV PIACI AJÁNLÁS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = bannerColor,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(bannerColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isUnder) "UNDER-PROFIL" else "OVER-PROFIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PRIMARY CONCRETE BET TIP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 KONKRÉT FOGADÁSI TIPP:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            IconButton(
                                onClick = {
                                    val tipText = buildString {
                                        appendLine("🎯 BioMatch xG Pre-Match Tipp: ${input.homeTeam} vs ${input.awayTeam}")
                                        appendLine("📌 Fő tipp: ${if (res.concreteBetTip.isNotBlank()) res.concreteBetTip else (if (isUnder) "Under 2.5 gól" else "Over 2.5 gól")}")
                                        if (res.secondaryBetTip.isNotBlank()) {
                                            appendLine("🛡️ Másodlagos piac: ${res.secondaryBetTip}")
                                        }
                                        appendLine("📊 Várható xG: ${String.format("%.2f", res.calculatedXg)} | Intervallum: ${res.mostLikelyInterval}")
                                        appendLine("📈 Over 2.5: ${(res.over25Prob * 100).toInt()}% | Under 2.5: ${(res.under25Prob * 100).toInt()}%")
                                        if (res.blackSwanFactor.isNotBlank()) {
                                            appendLine("⚠️ Fekete Hattyú: ${res.blackSwanFactor}")
                                        }
                                    }
                                    clipboardManager.setText(AnnotatedString(tipText))
                                    viewModel.showStatusMessage("Tipp kimásolva a vágólapra! 📋")
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Tipp másolása",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        SelectionContainer {
                            Text(
                                text = if (res.concreteBetTip.isNotBlank()) res.concreteBetTip else "Mérkőzés ${if (isUnder) "Kevesebb mint 2.5 gól (Under 2.5)" else "Több mint 2.5 gól (Over 2.5)"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SECONDARY MARKET
                        if (res.secondaryBetTip.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DeepVoid.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .border(1.dp, bannerColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = bannerColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🛡️ MÁSODLAGOS / BIZTONSÁGI PIAC:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = bannerColor
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = res.secondaryBetTip,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // KPI Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Calculated xG Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("SZÁMÍTOTT VÁRHATÓ xG", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.2f", res.calculatedXg),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("Total Expected Goals", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    // Goal Range Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, KineticEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("LEGVALÓSZÍNŰBB TARTOMÁNY", style = MaterialTheme.typography.labelSmall, color = KineticEmerald)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.mostLikelyInterval,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text("Poisson-súlyozott intervallum", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            // Binding Affinity Gauge
            item {
                BindingAffinityGauge(affinityIndex = res.bindingAffinityIndex)
            }

            // Over / Under 2.5 Bars
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "2.5 GÓL SPEKTRUM VALÓSZÍNŰSÉG",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        ProbabilityBar(
                            label = "2.5 Gól Felett (Over 2.5)",
                            percentage = res.over25Prob,
                            barColor = if (res.over25Prob >= 0.5) NeonCyan else ElectricBlue
                        )
                        ProbabilityBar(
                            label = "2.5 Gól Alatt (Under 2.5)",
                            percentage = res.under25Prob,
                            barColor = if (res.under25Prob >= 0.5) KineticEmerald else BorderSubtle
                        )
                    }
                }
            }

            // Score Matrix Grid
            item {
                ScoreMatrixGrid(scores = res.scoreProbabilities)
            }

            // Black Swan Alert
            item {
                BlackSwanAlertCard(
                    anomalyText = res.blackSwanFactor,
                    onCopied = { viewModel.showStatusMessage("Fekete Hattyú leírás kimásolva! 📋") }
                )
            }

            // The 4 Core Phases
            item {
                Text(
                    text = "A 4 SZINTŰ TUDOMÁNYOS GONDOLATMENET",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 1,
                    phaseTitle = "Molekuláris dokkolás",
                    phaseSubtitle = "Taktikai és Geometriai Affinitás & Receptor-illeszkedés",
                    content = res.phase1MolecularDocking,
                    accentColor = NeonCyan,
                    onCopied = { viewModel.showStatusMessage("1. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 2,
                    phaseTitle = "Metabolikus Kinetika",
                    phaseSubtitle = "Időbeli Degradáció, Entrópia & 60. perc utáni görbe",
                    content = res.phase2MetabolicKinetics,
                    accentColor = BioViolet,
                    onCopied = { viewModel.showStatusMessage("2. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 3,
                    phaseTitle = "Nemlineáris Gólképlet",
                    phaseSubtitle = "Szimbolikus Regresszió & Taktikai Súrlódási Szorzó",
                    content = res.phase3FormulaExplanation,
                    accentColor = ElectricBlue,
                    onCopied = { viewModel.showStatusMessage("3. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 4,
                    phaseTitle = "Monte-Carlo Valószínűségi Predikció",
                    phaseSubtitle = "xG spektrum & Fekete Hattyú anomália-faktor",
                    content = res.phase4MonteCarloText,
                    accentColor = KineticEmerald,
                    onCopied = { viewModel.showStatusMessage("4. Fázis kimásolva! 📋") }
                )
            }

            // Action Buttons: Copy Full Analysis & Save to Room
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val isUnder = res.calculatedXg <= 2.45 || res.under25Prob >= 0.52
                            val fullText = buildString {
                                appendLine("═══════════════════════════════════════")
                                appendLine("🔬 BIOMATCH xG TUDOMÁNYOS PREDIKCIÓ")
                                appendLine("Mérkőzés: ${input.homeTeam} vs ${input.awayTeam}")
                                appendLine("═══════════════════════════════════════")
                                appendLine("🎯 FŐ AJÁNLÁS: ${if (res.concreteBetTip.isNotBlank()) res.concreteBetTip else if (isUnder) "Under 2.5 gól" else "Over 2.5 gól"}")
                                if (res.secondaryBetTip.isNotBlank()) {
                                    appendLine("🛡️ BIZTONSÁGI PIAC: ${res.secondaryBetTip}")
                                }
                                appendLine("📊 Számított xG: ${String.format("%.2f", res.calculatedXg)}")
                                appendLine("🎯 Legvalószínűbb tartomány: ${res.mostLikelyInterval}")
                                appendLine("🧬 Kötési Affinitási Index: ${res.bindingAffinityIndex}/10")
                                appendLine("📈 Valószínűségek: Over 2.5: ${(res.over25Prob * 100).toInt()}% | Under 2.5: ${(res.under25Prob * 100).toInt()}%")
                                appendLine("\n1️⃣ FÁZIS - MOLEKULÁRIS DOKKOLÁS:\n${res.phase1MolecularDocking}")
                                appendLine("\n2️⃣ FÁZIS - METABOLIKUS KINETIKA:\n${res.phase2MetabolicKinetics}")
                                appendLine("\n3️⃣ FÁZIS - NEMLINEÁRIS GÓLKÉPLET:\n${res.phase3FormulaExplanation}")
                                appendLine("\n4️⃣ FÁZIS - MONTE-CARLO PREDIKCIÓ:\n${res.phase4MonteCarloText}")
                                if (res.blackSwanFactor.isNotBlank()) {
                                    appendLine("\n⚠️ FEKETE HATTYÚ ANOMÁLIA:\n${res.blackSwanFactor}")
                                }
                                appendLine("═══════════════════════════════════════")
                            }
                            clipboardManager.setText(AnnotatedString(fullText))
                            viewModel.showStatusMessage("Teljes elemzés kimásolva a vágólapra! 📋")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.2f), contentColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("copy_full_prematch_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TELJES ELEMZÉS MÁSOLÁSA VÁGÓLAPRA", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.saveCurrentPreMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .testTag("save_prematch_btn")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elemzés Mentése az Archívumba (Room DB)")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
