package com.hobbycoding.wattbench.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun WattGauge(
    watts: Double,
    peakWatts: Double,
    avgWatts: Double,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val maxGaugeWatts = 120.0f
    val targetSweep = if (isCharging) ((watts / maxGaugeWatts).coerceIn(0.0, 1.0) * 240.0).toFloat() else 0f
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(durationMillis = 500),
        label = "GaugeSweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background & Foreground arc canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    // Draw background arc
                    drawArc(
                        color = Color(0xFF334155),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw animated progress arc
                    if (animatedSweep > 0) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(NeonCyan, NeonAmber, NeonGreen)
                            ),
                            startAngle = 150f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Text Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bolt",
                            tint = if (isCharging) NeonAmber else TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        val formattedWatts = if (isCharging) {
                            String.format(Locale.US, "%.1f", watts)
                        } else {
                            String.format(Locale.US, "-%.1f", watts)
                        }
                        Text(
                            text = formattedWatts,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (isCharging) NeonCyan else TextSecondary
                        )
                    }
                    Text(
                        text = "WATTS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peak & Average Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PEAK POWER",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f W", peakWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF475569))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AVERAGE POWER",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f W", avgWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }
        }
    }
}
