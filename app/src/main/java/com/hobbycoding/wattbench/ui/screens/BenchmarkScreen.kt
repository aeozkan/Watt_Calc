package com.hobbycoding.wattbench.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hobbycoding.wattbench.data.model.BenchmarkSession
import com.hobbycoding.wattbench.data.model.PowerStats
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.ui.theme.DangerRed
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    powerStats: PowerStats,
    isRecording: Boolean,
    sessions: List<BenchmarkSession>,
    onStartRecording: (String, String) -> Unit,
    onStopRecording: () -> Unit,
    onDeleteSession: (String) -> Unit
) {
    var adapterInput by remember { mutableStateOf("") }
    var cableInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "CHARGER & CABLE BENCHMARK",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = NeonAmber,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Compare different charging bricks and USB cables",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Session Setup Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isRecording) "TEST IN PROGRESS (RECORDING BACKGROUND DATA)..." else "START NEW BENCHMARK RUN",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) NeonGreen else NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adapterInput,
                    onValueChange = { adapterInput = it },
                    label = { Text("Charger / Adapter Name (e.g. Xiaomi 90W)") },
                    enabled = !isRecording,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cableInput,
                    onValueChange = { cableInput = it },
                    label = { Text("Cable Name (e.g. 6A Type-C Cable)") },
                    enabled = !isRecording,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isRecording) {
                            onStopRecording()
                        } else {
                            onStartRecording(adapterInput, cableInput)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) DangerRed else NeonGreen
                    )
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "STOP BENCHMARK TEST" else "START BENCHMARK TEST",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Power:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        val formattedLiveWatts = if (powerStats.isCharging) {
                            String.format(Locale.US, "%.1f W", powerStats.powerWatts)
                        } else {
                            String.format(Locale.US, "-%.1f W", powerStats.powerWatts)
                        }
                        Text(
                            text = formattedLiveWatts,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (powerStats.isCharging) NeonCyan else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SAVED BENCHMARK RESULTS",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No benchmark tests recorded yet.\nStart a test session above to compare performance!",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sessions, key = { it.id }) { session ->
                    BenchmarkSessionCard(
                        session = session,
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BenchmarkSessionCard(
    session: BenchmarkSession,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    val startDateStr = if (session.startTimeMillis > 0) dateFormat.format(Date(session.startTimeMillis)) else "N/A"
    val endDateStr = if (session.endTimeMillis > 0) dateFormat.format(Date(session.endTimeMillis)) else "N/A"
    var showFullGraphDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Start / End Date Time Display
            Column {
                Text(
                    text = "Start: $startDateStr",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Text(
                    text = "End:   $endDateStr",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PEAK WATTS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f W", session.peakWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                }
                Column {
                    Text("AVG WATTS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f W", session.avgWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
                Column {
                    Text("AVG VOLTS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.2f V", session.avgVoltage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: ${session.durationSeconds}s",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Text(
                    text = "Battery: ${session.batteryLevelStart}% ➔ ${session.batteryLevelEnd}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            // Watt vs Time Chart
            if (session.wattSamples.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WATT - TIME GRAPH (${session.wattSamples.size} SAMPLES)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    IconButton(
                        onClick = { showFullGraphDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand Full Screen Graph",
                            tint = NeonCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Chart
                BenchmarkWattChart(samples = session.wattSamples)
            }
        }
    }

    if (showFullGraphDialog) {
        FullGraphModalDialog(
            session = session,
            onDismiss = { showFullGraphDialog = false }
        )
    }
}

@Composable
fun BenchmarkWattChart(
    samples: List<WattSample>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val minStepPx = 16.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        if (samples.size < 2) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not enough samples",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerWidth = maxWidth
                val totalCalculatedWidth = (samples.size * minStepPx.value).dp.coerceAtLeast(containerWidth)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(scrollState)
                        .width(totalCalculatedWidth)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(totalCalculatedWidth)
                    ) {
                        val maxWatts = (samples.maxOfOrNull { it.watt } ?: 10.0).coerceAtLeast(5.0).toFloat()
                        val minWatts = 0.0f
                        val stepX = size.width / (samples.size - 1).coerceAtLeast(1)

                        val chargingColor = NeonCyan
                        val dischargingColor = NeonAmber

                        for (i in 0 until samples.size - 1) {
                            val s1 = samples[i]
                            val s2 = samples[i + 1]

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
                                color = segmentColor.copy(alpha = 0.25f)
                            )

                            // Draw segment line
                            drawLine(
                                color = segmentColor,
                                start = androidx.compose.ui.geometry.Offset(x1, y1),
                                end = androidx.compose.ui.geometry.Offset(x2, y2),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullGraphModalDialog(
    session: BenchmarkSession,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "FULL DETAILED TIMELINE GRAPH",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DangerRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.US, "Peak: %.1f W", session.peakWatts),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                    Text(
                        text = String.format(Locale.US, "Avg: %.1f W", session.avgWatts),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = "${session.durationSeconds}s duration",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Detailed Chart in Modal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    BenchmarkWattChart(
                        samples = session.wattSamples,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💡 Swipe left and right inside graph to scroll across test timeline.",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }
    }
}
