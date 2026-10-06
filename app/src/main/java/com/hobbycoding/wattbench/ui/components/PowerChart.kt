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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
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
    val textMeasurer = rememberTextMeasurer()

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
                val screenIcon = if (lastSample != null) {
                    if (lastSample.isScreenOn) " 📱" else " 🔒"
                } else ""
                Text(
                    text = "$formattedWatts$screenIcon",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = topRightColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
                        val topPaddingPx = 22.dp.toPx()
                        val bottomPaddingPx = 18.dp.toPx()
                        val leftPaddingPx = 6.dp.toPx()
                        val rightPaddingPx = 6.dp.toPx()

                        val chartWidth = size.width - leftPaddingPx - rightPaddingPx
                        val chartHeight = size.height - topPaddingPx - bottomPaddingPx

                        val maxWatts = (history.maxOfOrNull { it.watt } ?: 10.0).coerceAtLeast(10.0).toFloat()
                        val minWatts = 0.0f

                        val stepX = chartWidth / (history.size - 1).coerceAtLeast(1)

                        val chargingColor = NeonCyan
                        val dischargingColor = TextSecondary

                        val iconTextStyle = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default
                        )

                        // 1. Screen On/Off Background Region & Transition Indicators
                        for (i in 0 until history.size - 1) {
                            val s1 = history[i]
                            val s2 = history[i + 1]
                            val x1 = leftPaddingPx + i * stepX
                            val x2 = leftPaddingPx + (i + 1) * stepX

                            // Tint background when screen is OFF
                            if (!s1.isScreenOn) {
                                drawRect(
                                    color = Color(0x2210B981),
                                    topLeft = androidx.compose.ui.geometry.Offset(x1, topPaddingPx),
                                    size = androidx.compose.ui.geometry.Size(x2 - x1, chartHeight)
                                )
                            }

                            // Transition line and icon
                            if (s1.isScreenOn != s2.isScreenOn) {
                                val transX = x2
                                drawLine(
                                    color = if (s2.isScreenOn) NeonAmber else NeonGreen,
                                    start = androidx.compose.ui.geometry.Offset(transX, topPaddingPx),
                                    end = androidx.compose.ui.geometry.Offset(transX, topPaddingPx + chartHeight),
                                    strokeWidth = 1.dp.toPx()
                                )
                                val iconStr = if (s2.isScreenOn) "📱" else "🔒"
                                val iconLayout = textMeasurer.measure(iconStr, iconTextStyle)
                                drawText(
                                    textLayoutResult = iconLayout,
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        x = (transX - iconLayout.size.width / 2f).coerceIn(0f, size.width - iconLayout.size.width),
                                        y = (topPaddingPx - 18.dp.toPx()).coerceAtLeast(0f)
                                    )
                                )
                            }
                        }

                        // 2. Bottom Baseline
                        drawLine(
                            color = Color(0xFF334155),
                            start = androidx.compose.ui.geometry.Offset(leftPaddingPx, topPaddingPx + chartHeight),
                            end = androidx.compose.ui.geometry.Offset(leftPaddingPx + chartWidth, topPaddingPx + chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // 3. Segment Line & Fill
                        for (i in 0 until history.size - 1) {
                            val s1 = history[i]
                            val s2 = history[i + 1]

                            val x1 = leftPaddingPx + i * stepX
                            val y1 = topPaddingPx + chartHeight - ((s1.watt.toFloat() - minWatts) / (maxWatts - minWatts) * chartHeight)

                            val x2 = leftPaddingPx + (i + 1) * stepX
                            val y2 = topPaddingPx + chartHeight - ((s2.watt.toFloat() - minWatts) / (maxWatts - minWatts) * chartHeight)

                            val segmentColor = if (s2.isCharging) chargingColor else dischargingColor

                            val fillPath = Path().apply {
                                moveTo(x1, topPaddingPx + chartHeight)
                                lineTo(x1, y1)
                                lineTo(x2, y2)
                                lineTo(x2, topPaddingPx + chartHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                color = segmentColor.copy(alpha = 0.22f)
                            )

                            drawLine(
                                color = segmentColor,
                                start = androidx.compose.ui.geometry.Offset(x1, y1),
                                end = androidx.compose.ui.geometry.Offset(x2, y2),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // 4. Subtle Time Ticks
                        if (history.size >= 10) {
                            val tickTextStyle = TextStyle(
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                            val tickStep = if (history.size > 60) 30 else 15
                            for (i in 0 until history.size step tickStep) {
                                val xPos = leftPaddingPx + i * stepX
                                val secondsAgo = (history.size - 1 - i)
                                val timeLabel = if (secondsAgo == 0) "Now" else "-${secondsAgo}s"
                                val layout = textMeasurer.measure(timeLabel, tickTextStyle)
                                drawText(
                                    textLayoutResult = layout,
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        x = (xPos - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
                                        y = topPaddingPx + chartHeight + 3.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
