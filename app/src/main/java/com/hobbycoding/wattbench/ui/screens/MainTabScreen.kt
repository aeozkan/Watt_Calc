package com.hobbycoding.wattbench.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import com.hobbycoding.wattbench.ui.viewmodel.WattViewModel

@Composable
fun MainTabScreen(
    viewModel: WattViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val powerStats by viewModel.powerStats.collectAsState()
    val wattHistory by viewModel.wattHistory.collectAsState()
    val isRecordingBenchmark by viewModel.isRecordingBenchmark.collectAsState()
    val benchmarkSessions by viewModel.benchmarkSessions.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Live Telemetry") },
                    label = { Text("Live Telemetry") },
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
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Benchmark") },
                    label = { Text("Benchmark") },
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
                    onResetStats = { viewModel.resetStats() }
                )
                1 -> BenchmarkScreen(
                    powerStats = powerStats,
                    isRecording = isRecordingBenchmark,
                    sessions = benchmarkSessions,
                    onStartRecording = { adapter, cable -> viewModel.startBenchmarkSession(adapter, cable) },
                    onStopRecording = { viewModel.stopBenchmarkSession() },
                    onDeleteSession = { sessionId -> viewModel.deleteBenchmarkSession(sessionId) }
                )
            }
        }
    }
}
