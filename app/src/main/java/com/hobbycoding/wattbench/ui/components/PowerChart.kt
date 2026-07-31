package com.hobbycoding.wattbench.ui.components

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PowerChart(
    history: List<WattSample>,
    modifier: Modifier = Modifier
) {
    val lastSample = history.lastOrNull()
    val isChargingNow = lastSample?.isCharging ?: false
    val topRightColor = if (isChargingNow) NeonCyan else TextSecondary

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
                    text = stringResource(R.string.chart_realtime_title),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                val currentWatts = lastSample?.watt ?: 0.0
                val formattedWatts = if (isChargingNow) {
                    String.format(Locale.US, "%.1f W", currentWatts)
                } else {
                    String.format(Locale.US, "-%.1f W", currentWatts)
                }
                Text(
                    text = formattedWatts,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = topRightColor
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
                            text = stringResource(R.string.chart_waiting_data),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxWatts = (history.maxOfOrNull { it.watt } ?: 10.0).coerceAtLeast(10.0).toFloat()
                        val minWatts = 0.0f

                        val stepX = size.width / (history.size - 1).coerceAtLeast(1)

                        val chargingColor = NeonCyan
                        val dischargingColor = TextSecondary

                        for (i in 0 until history.size - 1) {
                            val s1 = history[i]
                            val s2 = history[i + 1]

                            val x1 = i * stepX
                            val y1 = size.height - ((s1.watt.toFloat() - minWatts) / (maxWatts - minWatts) * size.height)

                            val x2 = (i + 1) * stepX
                            val y2 = size.height - ((s2.watt.toFloat() - minWatts) / (maxWatts - minWatts) * size.height)

                            val segmentColor = if (s2.isCharging) chargingColor else dischargingColor

                            // Draw segment fill polygon under line
                            val fillPath = Path().apply {
                                moveTo(x1, size.height)
                                lineTo(x1, y1)
                                lineTo(x2, y2)
                                lineTo(x2, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                color = segmentColor.copy(alpha = 0.2f)
                            )

                            // Draw segment line
                            drawLine(
                                color = segmentColor,
                                start = androidx.compose.ui.geometry.Offset(x1, y1),
                                end = androidx.compose.ui.geometry.Offset(x2, y2),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
