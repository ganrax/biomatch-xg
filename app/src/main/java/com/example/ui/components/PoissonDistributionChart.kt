package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScoreProbability
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KineticEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardHover
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProbabilityBar(
    label: String,
    percentage: Double,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedWidth by animateFloatAsState(
        targetValue = percentage.toFloat().coerceIn(0.01f, 1f),
        animationSpec = tween(700),
        label = "prob_bar"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "${String.format("%.1f", percentage * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = barColor,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF0D1524), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .height(8.dp)
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun ScoreMatrixGrid(
    scores: List<ScoreProbability>,
    modifier: Modifier = Modifier
) {
    if (scores.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("score_matrix_card")
    ) {
        Column {
            Text(
                text = "LEGGVALÓSZÍNŰBB PONTOS EREDMÉNYEK (POISSON-MÁTRIX)",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2 column grid of top scores
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scores.chunked(3).forEach { rowScores ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowScores.forEach { score ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(SurfaceCardHover, RoundedCornerShape(10.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = score.scoreText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = score.formattedPercent,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KineticEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
