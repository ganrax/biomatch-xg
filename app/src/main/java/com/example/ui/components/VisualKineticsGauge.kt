package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.BlackSwanRose
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BindingAffinityGauge(
    affinityIndex: Int, // 1 to 10
    modifier: Modifier = Modifier
) {
    val progress = (affinityIndex / 10f).coerceIn(0.1f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "gauge"
    )

    val gaugeColor = when {
        affinityIndex >= 8 -> BlackSwanRose
        affinityIndex >= 5 -> NeonCyan
        else -> ElectricBlue
    }

    val interpretation = when {
        affinityIndex >= 8 -> "Magas súrlódás: nyílt, kaotikus, gólveszélyes meccs"
        affinityIndex >= 5 -> "Átlagos súrlódás: kiegyensúlyozott taktikai tér"
        else -> "Alacsony súrlódás: neutralizált, taktikai sakkjátszma"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("binding_affinity_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KÖTÉSI AFFINITÁSI INDEX",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$affinityIndex / 10",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = interpretation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Radial arc gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val stroke = 8.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(stroke / 2, stroke / 2)

                    // Track
                    drawArc(
                        color = BorderSubtle,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )

                    // Active sweep
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ElectricBlue, gaugeColor)
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${affinityIndex * 10}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor
                )
            }
        }
    }
}

@Composable
fun SterilityIndicatorGauge(
    dangerousAttacksRatePerMin: Double,
    isSterileState: Boolean,
    modifier: Modifier = Modifier
) {
    val maxScale = 2.0
    val progress = (dangerousAttacksRatePerMin / maxScale).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "sterility_gauge"
    )

    val stateColor = if (isSterileState) KineticEmerald else AnomalyAmber
    val stateTitle = if (isSterileState) "TÖKÉLETESEN STERIL ÁLLAPOT" else "AKTÍV KINETIKAI ZÓNA"
    val stateDesc = if (isSterileState) {
        "Veszélyes támadások: ${String.format("%.2f", dangerousAttacksRatePerMin)} / perc (< 0.70 küszöb) -> Erős 1H UNDER jelzés!"
    } else {
        "Veszélyes támadások: ${String.format("%.2f", dangerousAttacksRatePerMin)} / perc (>= 0.70 küszöb) -> Nyitottabb folyosók"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, if (isSterileState) KineticEmerald.copy(alpha = 0.4f) else BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("sterility_indicator_card")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TÉRBELI STERILITÁSI TESZT",
                        style = MaterialTheme.typography.labelMedium,
                        color = stateColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }

                Text(
                    text = "${String.format("%.2f", dangerousAttacksRatePerMin)} / perc",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bar gauge with threshold tick at 0.70 / 2.0 = 35%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
            ) {
                // Active fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(12.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(KineticEmerald, stateColor)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                )

                // 0.70 Marker indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(12.dp)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.0 (Steril)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "0.70 Küszöb",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "2.0+ (Kaotikus)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$stateTitle: $stateDesc",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun BlackSwanAlertCard(
    anomalyText: String,
    onCopied: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF200F17), RoundedCornerShape(14.dp))
            .border(1.dp, BlackSwanRose.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("black_swan_card")
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BlackSwanRose.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Fekete Hattyú",
                    tint = BlackSwanRose,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "„FEKETE HATTYÚ” ANOMÁLIA-FAKTOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = BlackSwanRose,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("⚠️ „FEKETE HATTYÚ” ANOMÁLIA-FAKTOR:\n$anomalyText"))
                            onCopied?.invoke()
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = BlackSwanRose),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Anomália másolása",
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = anomalyText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD1DC)
                    )
                }
            }
        }
    }
}
