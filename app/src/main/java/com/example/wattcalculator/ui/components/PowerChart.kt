package com.example.wattcalculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wattcalculator.ui.theme.NeonCyan
import com.example.wattcalculator.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PowerChart(
    history: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME WATTAGE GRAPH (LAST 60S)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                val currentWatts = history.lastOrNull()?.second ?: 0.0
                Text(
                    text = String.format(Locale.US, "%.1f W", currentWatts),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (history.size < 2) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for live data...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxWatts = (history.maxOfOrNull { it.second } ?: 10.0).coerceAtLeast(10.0).toFloat()
                        val minWatts = 0.0f

                        val stepX = size.width / (history.size - 1).coerceAtLeast(1)

                        val linePath = Path()
                        val fillPath = Path()

                        history.forEachIndexed { index, pair ->
                            val x = index * stepX
                            val wattVal = pair.second.toFloat()
                            val normalizedY = (wattVal - minWatts) / (maxWatts - minWatts)
                            val y = size.height - (normalizedY * size.height)

                            if (index == 0) {
                                linePath.moveTo(x, y)
                                fillPath.moveTo(x, size.height)
                                fillPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (index == history.size - 1) {
                                fillPath.lineTo(x, size.height)
                                fillPath.close()
                            }
                        }

                        // Fill under line with gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // Draw main curve line
                        drawPath(
                            path = linePath,
                            color = NeonCyan,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
