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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.BlackSwanAlertCard
import com.example.ui.components.PhaseCard
import com.example.ui.components.ProbabilityBar
import com.example.ui.components.QuickImportBar
import com.example.ui.components.SterilityIndicatorGauge
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.BioViolet
import com.example.ui.theme.BlackSwanRose
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepVoid
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.KineticEmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.util.AnalysisDossierBuilder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveHalfScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.liveHalfInput.collectAsState()
    val analysis by viewModel.liveHalfAnalysis.collectAsState()
    val isLoading by viewModel.isLiveHalfLoading.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var showDetailedStats by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = KineticEmerald,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ÉLŐ 1. FÉLIDŐS KINETIKAI AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "0–15. perc élő adatok -> Enzimatikus inhibíció vs katalízis",
                        style = MaterialTheme.typography.bodySmall,
                        color = KineticEmeraldGlow
                    )
                }
            }
        }

        // Quick Import Bar (Screenshot OCR & Clipboard Paste)
        item {
            QuickImportBar(
                viewModel = viewModel,
                isLiveMode = true
            )
        }

        // Live Scenario Presets
        item {
            Text(
                text = "15. Perces Élő Forgatókönyv Presetek:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = input.currentScore == "0-0" && input.kszMultiplier <= 0.70,
                        onClick = { viewModel.loadLiveHalfPreset("sterile_deep_block") },
                        label = { Text("0–0 Steril Mélyblokk (Erős Under)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KineticEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = KineticEmerald
                        ),
                        modifier = Modifier.testTag("preset_sterile_block")
                    )
                }
                item {
                    FilterChip(
                        selected = input.currentScore == "1-0",
                        onClick = { viewModel.loadLiveHalfPreset("early_lead_parked") },
                        label = { Text("1–0 Korai Gól után Visszaállás") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricBlue
                        ),
                        modifier = Modifier.testTag("preset_early_lead")
                    )
                }
                item {
                    FilterChip(
                        selected = input.kszMultiplier >= 1.2,
                        onClick = { viewModel.loadLiveHalfPreset("open_chaos") },
                        label = { Text("0–0 Kaotikus Adok-Kapok (Over)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AnomalyAmber.copy(alpha = 0.2f),
                            selectedLabelColor = AnomalyAmber
                        ),
                        modifier = Modifier.testTag("preset_open_chaos")
                    )
                }
            }
        }

        // Live Match Info Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Team names and current score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = input.homeTeam,
                            onValueChange = { s -> viewModel.updateLiveHalfInput { it.copy(homeTeam = s) } },
                            label = { Text("Hazai") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("live_home_input")
                        )

                        // 15th min Score
                        OutlinedTextField(
                            value = input.currentScore,
                            onValueChange = { s -> viewModel.updateLiveHalfInput { it.copy(currentScore = s) } },
                            label = { Text("15.p Állás") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AnomalyAmber,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.width(90.dp).testTag("live_score_input")
                        )

                        OutlinedTextField(
                            value = input.awayTeam,
                            onValueChange = { s -> viewModel.updateLiveHalfInput { it.copy(awayTeam = s) } },
                            label = { Text("Vendég") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("live_away_input")
                        )
                    }

                    // Key Quick Stats: Dangerous Attacks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = input.dangerousAttacksHome.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { v -> viewModel.updateLiveHalfInput { it.copy(dangerousAttacksHome = v) } } },
                            label = { Text("Veszélyes Tám. Hazai") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("live_da_home")
                        )
                        OutlinedTextField(
                            value = input.dangerousAttacksAway.toString(),
                            onValueChange = { s -> s.toIntOrNull()?.let { v -> viewModel.updateLiveHalfInput { it.copy(dangerousAttacksAway = v) } } },
                            label = { Text("Veszélyes Tám. Vendég") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("live_da_away")
                        )
                    }

                    // KSz Multiplier Slider (The Core Inhibitory vs Catalytic Driver)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Inhibíciós / Kinetikai Korrekciós Szorzó (KSz):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = String.format("%.2f", input.kszMultiplier) + when {
                                    input.kszMultiplier <= 0.85 -> " (ERŐS UNDER)"
                                    input.kszMultiplier >= 1.15 -> " (OVER-ZÓNA)"
                                    else -> " (Átlagos)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (input.kszMultiplier <= 0.85) KineticEmerald else if (input.kszMultiplier >= 1.15) AnomalyAmber else NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = input.kszMultiplier.toFloat(),
                            onValueChange = { v -> viewModel.updateLiveHalfInput { it.copy(kszMultiplier = v.toDouble()) } },
                            valueRange = 0.5f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (input.kszMultiplier <= 0.85) KineticEmerald else AnomalyAmber,
                                activeTrackColor = if (input.kszMultiplier <= 0.85) KineticEmerald else AnomalyAmber
                            ),
                            modifier = Modifier.testTag("ksz_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.50 (Magas gátlás / Steril)", style = MaterialTheme.typography.labelSmall, color = KineticEmerald)
                            Text("1.0 (Átlag)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("1.40 (Nyitott / Over)", style = MaterialTheme.typography.labelSmall, color = AnomalyAmber)
                        }
                    }

                    // Optional details expansion
                    OutlinedButton(
                        onClick = { showDetailedStats = !showDetailedStats },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KineticEmerald)
                    ) {
                        Text(if (showDetailedStats) "Részletes Lövések, Birtoklás és Félidős xG Elrejtése ▲" else "Részletes Lövések, Megszakítások és Félidős xG Megjelenítése ▼")
                    }

                    AnimatedVisibility(visible = showDetailedStats) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Shots
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = "${input.shotsHome} (kapura: ${input.shotsHomeOnTarget})",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Hazai lövések (össz/kapu)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = "${input.shotsAway} (kapura: ${input.shotsAwayOnTarget})",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Vendég lövések (össz/kapu)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Fouls and tactical impression
                            OutlinedTextField(
                                value = input.foulsAndStoppages,
                                onValueChange = { s -> viewModel.updateLiveHalfInput { it.copy(foulsAndStoppages = s) } },
                                label = { Text("Szabálytalanságok / Játékmegszakítások / Ápolások") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KineticEmerald,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = input.tacticalImpression,
                                onValueChange = { s -> viewModel.updateLiveHalfInput { it.copy(tacticalImpression = s) } },
                                label = { Text("Taktikai benyomás (pl. steril labdajáratás, tömör védőfal)") },
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KineticEmerald,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 0-15 xG and 16-45 base xG sliders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("0-15.p xG: ${String.format("%.2f", input.xg0To15)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Slider(
                                        value = input.xg0To15.toFloat(),
                                        onValueChange = { v -> viewModel.updateLiveHalfInput { it.copy(xg0To15 = v.toDouble()) } },
                                        valueRange = 0.0f..0.8f
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("16-45.p Bázis: ${String.format("%.2f", input.xg16To45Base)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Slider(
                                        value = input.xg16To45Base.toFloat(),
                                        onValueChange = { v -> viewModel.updateLiveHalfInput { it.copy(xg16To45Base = v.toDouble()) } },
                                        valueRange = 0.2f..1.2f
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Action CTA
        item {
            Button(
                onClick = { viewModel.runLiveHalfAiAnalysis() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = KineticEmerald, contentColor = DeepVoid),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("run_live_analysis_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DeepVoid)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("1H KINETIKAI & INHIBÍCIÓS SZÁMÍTÁS...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("FUTTATÁS: 1. FÉLIDŐS ÉLŐ PREDIKCIÓ", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Half Results
        analysis?.let { res ->
            // Dominant Market Direction & Concrete Betting Tip Banner (CRITICAL USER FOCUS)
            item {
                val isUnder = res.dominantMarketDirection.contains("UNDER")
                val bannerColor = if (isUnder) KineticEmerald else AnomalyAmber
                val bannerBg = if (isUnder) Color(0xFF042F24) else Color(0xFF2E1C05)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg, RoundedCornerShape(20.dp))
                        .border(2.dp, bannerColor, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                        .testTag("dominant_market_banner")
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header tag & Dominant direction
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUnder) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = bannerColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PREDIKTÍV AJÁNLÁS",
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
                                    text = res.dominantMarketDirection,
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
                                text = "🎯 KONKRÉT FOGADÁSI TIPP (15' ÁLLÁS: ${input.currentScore}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            IconButton(
                                onClick = {
                                    val tipText = buildString {
                                        appendLine("⚡ BioMatch xG Élő 1. Félidős Tipp: ${input.homeTeam} vs ${input.awayTeam}")
                                        appendLine("⏱️ 15. perces állás: ${input.currentScore}")
                                        appendLine("📌 Fő tipp: ${if (res.concreteBetTip.isNotBlank()) res.concreteBetTip else res.mostValuableMarket}")
                                        if (res.secondaryBetTip.isNotBlank()) {
                                            appendLine("🛡️ Biztonsági piac: ${res.secondaryBetTip}")
                                        }
                                        appendLine("📊 1H Várható xG: ${String.format("%.2f", res.calculated1hXg)} | Domináns irány: ${res.dominantMarketDirection}")
                                        appendLine("📈 1H Under 0.5: ${(res.under05Prob * 100).toInt()}% | 1H Under 1.5: ${(res.under15Prob * 100).toInt()}%")
                                        if (res.blackSwanFactor.isNotBlank()) {
                                            appendLine("⚠️ Gátlástörő anomália: ${res.blackSwanFactor}")
                                        }
                                    }
                                    clipboardManager.setText(AnnotatedString(tipText))
                                    viewModel.showStatusMessage("Élő tipp kimásolva a vágólapra! 📋")
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Élő tipp másolása",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        SelectionContainer {
                            Text(
                                text = if (res.concreteBetTip.isNotBlank()) res.concreteBetTip else res.mostValuableMarket,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SECONDARY / SAFETY MARKET
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
                                            text = "🛡️ BIZTONSÁGI / MÁSODLAGOS PIAC:",
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

            // Sterility Indicator Gauge
            item {
                SterilityIndicatorGauge(
                    dangerousAttacksRatePerMin = res.dangerousAttackRatePerMin,
                    isSterileState = res.isSterileState
                )
            }

            // KPI Grid: 1H xG & Most Likely HT Score
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("SZÁMÍTOTT 1H xG", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.2f", res.calculated1hXg),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("xG(0-15) + (xG(16-45) * KSz)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, KineticEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("VÁRHATÓ SZÜNETI ÁLLÁS", style = MaterialTheme.typography.labelSmall, color = KineticEmerald)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.mostLikelyHtScore,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("Legvalószínűbb HT Score", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            // Probability Matrix Card (0.5 & 1.5 Goals)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("probability_matrix_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "ELSŐ FÉLIDEI VALÓSZÍNŰSÉGI MÁTRIX (PÁROS BONTÁS)",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )

                        // 0.5 Line
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("0.5 GÓL HATÁRON:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                            ProbabilityBar(
                                label = "1H Under 0.5 (szünetben 0–0)",
                                percentage = res.under05Prob,
                                barColor = if (res.under05Prob >= 0.5) KineticEmerald else BorderSubtle
                            )
                            ProbabilityBar(
                                label = "1H Over 0.5",
                                percentage = res.over05Prob,
                                barColor = if (res.over05Prob > 0.5) AnomalyAmber else BorderSubtle
                            )
                        }

                        // 1.5 Line
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1.5 GÓL HATÁRON:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                            ProbabilityBar(
                                label = "1H Under 1.5 (maximum 1 gól)",
                                percentage = res.under15Prob,
                                barColor = if (res.under15Prob >= 0.7) KineticEmerald else ElectricBlue
                            )
                            ProbabilityBar(
                                label = "1H Over 1.5",
                                percentage = res.over15Prob,
                                barColor = if (res.over15Prob > 0.3) BlackSwanRose else BorderSubtle
                            )
                        }
                    }
                }
            }

            // Black Swan Hazard
            item {
                BlackSwanAlertCard(
                    anomalyText = "Gátlástörő Fekete Hattyú: ${res.blackSwanFactor}",
                    onCopied = { viewModel.showStatusMessage("Fekete Hattyú leírás kimásolva! 📋") }
                )
            }

            // The 4 Core Phases for 1H
            item {
                Text(
                    text = "A 4 FÁZISÚ ELSŐ FÉLIDŐS KINETIKAI MODELL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = KineticEmerald,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 1,
                    phaseTitle = "Reakciósebesség (v0) vs Inhibíció",
                    phaseSubtitle = "0–15. perc: Katalízis vagy Blokkolás & Sterilitási teszt",
                    content = res.phase1ReactionAndInhibition,
                    accentColor = KineticEmerald,
                    onCopied = { viewModel.showStatusMessage("1. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 2,
                    phaseTitle = "Kinetikai Fluxus & Ritmus-degradáció",
                    phaseSubtitle = "16–45. perc: Meddőségi kockázat, Game-state gátlás & Entrópia",
                    content = res.phase2KineticFlux,
                    accentColor = ElectricBlue,
                    onCopied = { viewModel.showStatusMessage("2. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 3,
                    phaseTitle = "Nemlineáris Első Félidős Gólképlet",
                    phaseSubtitle = "xG(0-15) + (xG(16-45) * KSz) Számítás",
                    content = res.phase3FormulaDetails,
                    accentColor = NeonCyan,
                    onCopied = { viewModel.showStatusMessage("3. Fázis kimásolva! 📋") }
                )
            }

            item {
                PhaseCard(
                    phaseNumber = 4,
                    phaseTitle = "Monte-Carlo 1H & Piaci Preferencia",
                    phaseSubtitle = "Domináns piac, Valószínűségi Mátrix & HT Score",
                    content = res.phase4MarketText,
                    accentColor = BioViolet,
                    onCopied = { viewModel.showStatusMessage("4. Fázis kimásolva! 📋") }
                )
            }

            // Action Buttons: Copy Full Analysis & Save to Room
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val fullLiveDossier = AnalysisDossierBuilder.buildFullLiveHalfDossier(input, res)
                            clipboardManager.setText(AnnotatedString(fullLiveDossier))
                            viewModel.showStatusMessage("Teljes élő döntési folyamat kimásolva (0. lépéstől a konklúzióig)! 📋")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KineticEmerald.copy(alpha = 0.2f), contentColor = KineticEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, KineticEmerald.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("copy_full_live_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TELJES DÖNTÉSI FOLYAMAT MÁSOLÁSA (0-5. LÉPÉS)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.saveCurrentLiveHalf() },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = KineticEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, KineticEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .testTag("save_live_btn")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Élő Elemzés Mentése az Archívumba (Room DB)")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
