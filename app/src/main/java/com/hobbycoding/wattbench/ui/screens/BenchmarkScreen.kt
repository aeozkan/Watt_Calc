package com.hobbycoding.wattbench.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.data.model.BenchmarkSession
import com.hobbycoding.wattbench.data.model.PowerStats
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.ui.theme.DangerRed
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
import com.hobbycoding.wattbench.ui.theme.PeakGold
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> stringResource(R.string.duration_min_sec, minutes, seconds)
        minutes > 0 -> stringResource(R.string.duration_min, minutes)
        else -> stringResource(R.string.duration_sec, seconds)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    powerStats: PowerStats,
    isRecording: Boolean,
    sessions: List<BenchmarkSession>,
    onStartRecording: (String, String) -> Unit,
    onStopRecording: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    var adapterInput by remember { mutableStateOf("") }
    var cableInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section with Top-Left Menu Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = NeonAmber
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_header_benchmark),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeonAmber,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = stringResource(R.string.subtitle_benchmark),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
        }

        // Session Setup Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isRecording) stringResource(R.string.benchmark_in_progress) else stringResource(R.string.benchmark_start_new),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) NeonGreen else NeonCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adapterInput,
                        onValueChange = { adapterInput = it },
                        label = { Text(stringResource(R.string.label_charger_adapter)) },
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
                        label = { Text(stringResource(R.string.label_cable)) },
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
                            text = if (isRecording) stringResource(R.string.btn_stop_benchmark) else stringResource(R.string.btn_start_benchmark),
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
                                text = stringResource(R.string.live_power_label),
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
        }

        // Saved Results Section Title
        item {
            Text(
                text = stringResource(R.string.saved_results_title),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        // Saved Results List or Empty State
        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_benchmark_msg),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                BenchmarkSessionCard(
                    session = session,
                    onDelete = { onDeleteSession(session.id) }
                )
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
    var touchedSample by remember { mutableStateOf<WattSample?>(null) }

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
                    text = stringResource(R.string.label_start, startDateStr),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Text(
                    text = stringResource(R.string.label_end, endDateStr),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row: Peak Watts | Avg Watts | Touched Point Watt & Battery & Screen Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.metric_peak_watts), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f W", session.peakWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeakGold
                    )
                }
                Column {
                    Text(stringResource(R.string.metric_avg_watts), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(
                        text = String.format(Locale.US, "%.1f W", session.avgWatts),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
                Column {
                    Text(stringResource(R.string.metric_point_watt), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    val sample = touchedSample
                    if (sample != null) {
                        val wattStr = if (sample.isCharging) {
                            String.format(Locale.US, "%.1f W", sample.watt)
                        } else {
                            String.format(Locale.US, "-%.1f W", sample.watt)
                        }
                        val screenStatusStr = if (sample.isScreenOn) " 📱" else " 🔒"
                        val batteryStr = if (sample.batteryLevel > 0) " / %${sample.batteryLevel}" else ""
                        val textVal = "$wattStr$batteryStr$screenStatusStr"
                        val textColor = if (sample.isCharging) NeonCyan else NeonAmber
                        Text(
                            text = textVal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    } else {
                        Text(
                            text = "- -",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration display with formatted dk & sn
            Text(
                text = stringResource(R.string.label_duration, formatDuration(session.durationSeconds)),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            // Watt vs Time Chart
            if (session.wattSamples.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chart_watt_time_graph, session.wattSamples.size),
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

                // Scrollable Chart with Touch Point callback
                BenchmarkWattChart(
                    samples = session.wattSamples,
                    onSampleSelected = { touchedSample = it }
                )
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
    onSampleSelected: ((WattSample) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val minStepPx = 22.dp
    val textMeasurer = rememberTextMeasurer()
    val notEnoughSamplesText = stringResource(R.string.chart_not_enough_samples)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
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
                    text = notEnoughSamplesText,
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
                            .pointerInput(samples) {
                                detectTapGestures(
                                    onLongPress = { offset ->
                                        if (samples.isEmpty()) return@detectTapGestures
                                        val leftPaddingPx = 36.dp.toPx()
                                        val rightPaddingPx = 12.dp.toPx()
                                        val chartWidth = (size.width - leftPaddingPx - rightPaddingPx).coerceAtLeast(1f)
                                        val stepX = chartWidth / (samples.size - 1).coerceAtLeast(1)

                                        val touchX = (offset.x - leftPaddingPx).coerceIn(0f, chartWidth)
                                        val idx = (touchX / stepX).toInt().coerceIn(0, samples.lastIndex)
                                        onSampleSelected?.invoke(samples[idx])
                                    },
                                    onTap = { offset ->
                                        if (samples.isEmpty()) return@detectTapGestures
                                        val leftPaddingPx = 36.dp.toPx()
                                        val rightPaddingPx = 12.dp.toPx()
                                        val chartWidth = (size.width - leftPaddingPx - rightPaddingPx).coerceAtLeast(1f)
                                        val stepX = chartWidth / (samples.size - 1).coerceAtLeast(1)

                                        val touchX = (offset.x - leftPaddingPx).coerceIn(0f, chartWidth)
                                        val idx = (touchX / stepX).toInt().coerceIn(0, samples.lastIndex)
                                        onSampleSelected?.invoke(samples[idx])
                                    }
                                )
                            }
                    ) {
                        val leftPaddingPx = 36.dp.toPx()
                        val bottomPaddingPx = 24.dp.toPx()
                        val topPaddingPx = 12.dp.toPx()
                        val rightPaddingPx = 12.dp.toPx()

                        val chartWidth = size.width - leftPaddingPx - rightPaddingPx
                        val chartHeight = size.height - topPaddingPx - bottomPaddingPx

                        val maxWatts = (samples.maxOfOrNull { it.watt } ?: 10.0).coerceAtLeast(5.0).toFloat()
                        val minWatts = 0.0f
                        val stepX = chartWidth / (samples.size - 1).coerceAtLeast(1)

                        val chargingColor = NeonCyan
                        val dischargingColor = NeonAmber
                        val labelTextStyle = TextStyle(
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        val iconTextStyle = TextStyle(
                            color = Color.White,
                            fontSize = 8.sp
                        )

                        // 1. Draw Y-Axis Labels & Horizontal Grid Lines
                        val ySteps = 3
                        for (j in 0..ySteps) {
                            val ratio = j.toFloat() / ySteps
                            val yPos = topPaddingPx + chartHeight * (1f - ratio)
                            val wattVal = minWatts + (maxWatts - minWatts) * ratio

                            // Horizontal dashed line
                            drawLine(
                                color = Color(0xFF475569),
                                start = androidx.compose.ui.geometry.Offset(leftPaddingPx, yPos),
                                end = androidx.compose.ui.geometry.Offset(size.width - rightPaddingPx, yPos),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Y Label (SI symbol W)
                            val yLabelText = String.format(Locale.US, "%.0fW", wattVal)
                            val textLayout = textMeasurer.measure(yLabelText, labelTextStyle)
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    x = leftPaddingPx - textLayout.size.width - 4.dp.toPx(),
                                    y = yPos - textLayout.size.height / 2f
                                )
                            )
                        }

                        // 2. Screen On/Off Background Region & Transition Indicators
                        for (i in 0 until samples.size - 1) {
                            val s1 = samples[i]
                            val s2 = samples[i + 1]
                            val x1 = leftPaddingPx + i * stepX
                            val x2 = leftPaddingPx + (i + 1) * stepX

                            // If screen is OFF (ideal test condition), tint region background slightly darker with a subtle accent line
                            if (!s1.isScreenOn) {
                                drawRect(
                                    color = Color(0x1A10B981), // subtle emerald green tint for screen off optimal test
                                    topLeft = androidx.compose.ui.geometry.Offset(x1, topPaddingPx),
                                    size = androidx.compose.ui.geometry.Size(x2 - x1, chartHeight)
                                )
                            }

                            // Detect Screen ON <-> OFF transition
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
                                        x = transX - iconLayout.size.width / 2f,
                                        y = topPaddingPx - 2.dp.toPx()
                                    )
                                )
                            }
                        }

                        // 3. Draw X-Axis Time Ticks & Battery Level %
                        val tickIndices = mutableListOf<Int>()
                        val firstMinLimit = 60.coerceAtMost(samples.lastIndex)
                        for (i in 0..firstMinLimit step 10) {
                            tickIndices.add(i)
                        }
                        if (samples.lastIndex > 60) {
                            for (i in 90..samples.lastIndex step 30) {
                                tickIndices.add(i)
                            }
                        }

                        for (i in tickIndices) {
                            val xPos = leftPaddingPx + i * stepX
                            val minutes = i / 60
                            val seconds = i % 60
                            val timeStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
                            val sample = samples[i]
                            val labelStr = if (sample.batteryLevel > 0) "$timeStr, %${sample.batteryLevel}" else timeStr

                            // Vertical tick mark
                            drawLine(
                                color = Color(0xFF475569),
                                start = androidx.compose.ui.geometry.Offset(xPos, size.height - bottomPaddingPx),
                                end = androidx.compose.ui.geometry.Offset(xPos, size.height - bottomPaddingPx + 4.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )

                            // X Label (Time + Battery %)
                            val textLayout = textMeasurer.measure(labelStr, labelTextStyle)
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    x = (xPos - textLayout.size.width / 2f).coerceIn(leftPaddingPx, size.width - rightPaddingPx - textLayout.size.width),
                                    y = size.height - bottomPaddingPx + 4.dp.toPx()
                                )
                            )
                        }

                        // 4. Draw Segment Line & Fills
                        for (i in 0 until samples.size - 1) {
                            val s1 = samples[i]
                            val s2 = samples[i + 1]

                            val x1 = leftPaddingPx + i * stepX
                            val y1 = topPaddingPx + chartHeight - ((s1.watt.toFloat() - minWatts) / (maxWatts - minWatts) * chartHeight)

                            val x2 = leftPaddingPx + (i + 1) * stepX
                            val y2 = topPaddingPx + chartHeight - ((s2.watt.toFloat() - minWatts) / (maxWatts - minWatts) * chartHeight)

                            val segmentColor = if (s2.isCharging) chargingColor else dischargingColor

                            // Draw segment fill polygon under line
                            val fillPath = Path().apply {
                                moveTo(x1, topPaddingPx + chartHeight)
                                lineTo(x1, y1)
                                lineTo(x2, y2)
                                lineTo(x2, topPaddingPx + chartHeight)
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
    var modalTouchedSample by remember { mutableStateOf<WattSample?>(null) }

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
                            text = stringResource(R.string.chart_full_detailed_title),
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

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.US, "Peak: %.1f W", session.peakWatts),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeakGold
                    )
                    Text(
                        text = String.format(Locale.US, "Avg: %.1f W", session.avgWatts),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = formatDuration(session.durationSeconds),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                if (modalTouchedSample != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val sample = modalTouchedSample!!
                    val wattStr = if (sample.isCharging) {
                        String.format(Locale.US, "Selected Point: %.1f W", sample.watt)
                    } else {
                        String.format(Locale.US, "Selected Point: -%.1f W", sample.watt)
                    }
                    val screenStatusStr = if (sample.isScreenOn) " 📱" else " 🔒"
                    val batteryStr = if (sample.batteryLevel > 0) " / %${sample.batteryLevel}" else ""
                    val textVal = "$wattStr$batteryStr$screenStatusStr"
                    val textColor = if (sample.isCharging) NeonCyan else NeonAmber
                    Text(
                        text = textVal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
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
                        onSampleSelected = { modalTouchedSample = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.chart_modal_hint),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }
    }
}
