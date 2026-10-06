package com.hobbycoding.wattbench.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.service.PowerTelemetryService
import com.hobbycoding.wattbench.ui.components.SettingsBottomSheet
import com.hobbycoding.wattbench.ui.components.WelcomeTipsDialog
import com.hobbycoding.wattbench.ui.theme.DangerRed
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import com.hobbycoding.wattbench.ui.viewmodel.WattViewModel

@Composable
fun MainTabScreen(
    viewModel: WattViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        showExitDialog = true
    }

    val powerStats by viewModel.powerStats.collectAsState()
    val wattHistory by viewModel.wattHistory.collectAsState()
    val isRecordingBenchmark by viewModel.isRecordingBenchmark.collectAsState()
    val benchmarkSessions by viewModel.benchmarkSessions.collectAsState()
    val pollingIntervalMs by viewModel.pollingIntervalMs.collectAsState()
    val showWelcomeTips by viewModel.showWelcomeTips.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ElectricBolt, contentDescription = stringResource(R.string.tab_live)) },
                    label = { Text(stringResource(R.string.tab_live)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = stringResource(R.string.tab_benchmark)) },
                    label = { Text(stringResource(R.string.tab_benchmark)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    powerStats = powerStats,
                    wattHistory = wattHistory,
                    onResetStats = { viewModel.resetStats() },
                    onOpenSettings = { showSettingsSheet = true }
                )
                1 -> BenchmarkScreen(
                    powerStats = powerStats,
                    isRecording = isRecordingBenchmark,
                    sessions = benchmarkSessions,
                    onStartRecording = { adapter, cable -> viewModel.startBenchmarkSession(adapter, cable) },
                    onStopRecording = { viewModel.stopBenchmarkSession() },
                    onDeleteSession = { sessionId -> viewModel.deleteBenchmarkSession(sessionId) },
                    onOpenSettings = { showSettingsSheet = true }
                )
            }
        }

        if (showWelcomeTips) {
            WelcomeTipsDialog(
                onDismiss = { dontShowAgain -> viewModel.dismissWelcomeTips(dontShowAgain) }
            )
        }

        if (showSettingsSheet) {
            SettingsBottomSheet(
                powerStats = powerStats,
                currentPollingIntervalMs = pollingIntervalMs,
                onPollingIntervalChanged = { viewModel.setPollingInterval(it) },
                onExportCSV = { ctx -> viewModel.exportSessionsToCSV(ctx) },
                onDismiss = { showSettingsSheet = false }
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.dialog_exit_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.dialog_exit_body),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            PowerTelemetryService.stopService(context)
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_exit_confirm),
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(
                            text = stringResource(R.string.dialog_exit_cancel),
                            color = NeonCyan
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
