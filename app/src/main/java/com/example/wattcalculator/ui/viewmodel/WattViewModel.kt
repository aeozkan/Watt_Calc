package com.example.wattcalculator.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wattcalculator.data.model.BenchmarkSession
import com.example.wattcalculator.data.model.PowerStats
import com.example.wattcalculator.data.repository.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class WattViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BatteryRepository(application)

    private val _powerStats = MutableStateFlow(PowerStats())
    val powerStats: StateFlow<PowerStats> = _powerStats.asStateFlow()

    private val _wattHistory = MutableStateFlow<List<Pair<Long, Double>>>(emptyList())
    val wattHistory: StateFlow<List<Pair<Long, Double>>> = _wattHistory.asStateFlow()

    private val _benchmarkSessions = MutableStateFlow<List<BenchmarkSession>>(emptyList())
    val benchmarkSessions: StateFlow<List<BenchmarkSession>> = _benchmarkSessions.asStateFlow()

    // Benchmark recording state
    private val _isRecordingBenchmark = MutableStateFlow(false)
    val isRecordingBenchmark: StateFlow<Boolean> = _isRecordingBenchmark.asStateFlow()

    private var benchmarkStartTime: Long = 0L
    private var benchmarkStartBatteryLevel: Int = 0
    private val benchmarkWattSamples = mutableListOf<Double>()
    private val benchmarkVoltSamples = mutableListOf<Double>()
    private val benchmarkAmpSamples = mutableListOf<Double>()
    private var currentBenchmarkAdapter = ""
    private var currentBenchmarkCable = ""

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            repository.getPowerStatsFlow(1000L).collect { stats ->
                _powerStats.value = stats

                // Maintain rolling history of last 60 seconds
                val currentHistory = _wattHistory.value.toMutableList()
                currentHistory.add(Pair(stats.timestamp, stats.powerWatts))
                if (currentHistory.size > 60) {
                    currentHistory.removeAt(0)
                }
                _wattHistory.value = currentHistory

                // If currently recording benchmark
                if (_isRecordingBenchmark.value) {
                    benchmarkWattSamples.add(stats.powerWatts)
                    benchmarkVoltSamples.add(stats.voltageVolts)
                    benchmarkAmpSamples.add(stats.currentAmperes)
                }
            }
        }
    }

    fun resetStats() {
        repository.resetPeakAndAverage()
        _wattHistory.value = emptyList()
    }

    fun startBenchmarkSession(adapterName: String, cableName: String) {
        currentBenchmarkAdapter = adapterName.ifBlank { "Standard Charger" }
        currentBenchmarkCable = cableName.ifBlank { "Standard Cable" }
        benchmarkStartTime = System.currentTimeMillis()
        benchmarkStartBatteryLevel = _powerStats.value.batteryLevel
        benchmarkWattSamples.clear()
        benchmarkVoltSamples.clear()
        benchmarkAmpSamples.clear()
        _isRecordingBenchmark.value = true
    }

    fun stopBenchmarkSession() {
        if (!_isRecordingBenchmark.value) return

        _isRecordingBenchmark.value = false
        val durationSec = ((System.currentTimeMillis() - benchmarkStartTime) / 1000).toInt()

        val peakWatts = if (benchmarkWattSamples.isNotEmpty()) benchmarkWattSamples.maxOrNull() ?: 0.0 else 0.0
        val avgWatts = if (benchmarkWattSamples.isNotEmpty()) benchmarkWattSamples.average() else 0.0
        val avgVolts = if (benchmarkVoltSamples.isNotEmpty()) benchmarkVoltSamples.average() else 0.0
        val avgAmps = if (benchmarkAmpSamples.isNotEmpty()) benchmarkAmpSamples.average() else 0.0

        val session = BenchmarkSession(
            title = "$currentBenchmarkAdapter + $currentBenchmarkCable",
            adapterName = currentBenchmarkAdapter,
            cableName = currentBenchmarkCable,
            durationSeconds = durationSec,
            peakWatts = peakWatts,
            avgWatts = avgWatts,
            avgVoltage = avgVolts,
            avgAmperes = avgAmps,
            batteryLevelStart = benchmarkStartBatteryLevel,
            batteryLevelEnd = _powerStats.value.batteryLevel
        )

        val updatedList = _benchmarkSessions.value.toMutableList()
        updatedList.add(0, session)
        _benchmarkSessions.value = updatedList
    }

    fun deleteBenchmarkSession(sessionId: String) {
        _benchmarkSessions.value = _benchmarkSessions.value.filter { it.id != sessionId }
    }
}
