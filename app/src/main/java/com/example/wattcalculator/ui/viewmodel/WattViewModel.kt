package com.example.wattcalculator.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wattcalculator.data.model.BenchmarkSession
import com.example.wattcalculator.data.model.PowerStats
import com.example.wattcalculator.data.model.WattSample
import com.example.wattcalculator.data.repository.BatteryRepository
import com.example.wattcalculator.service.PowerTelemetryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WattViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BatteryRepository(application)

    private val _powerStats = MutableStateFlow(PowerStats())
    val powerStats: StateFlow<PowerStats> = _powerStats.asStateFlow()

    private val _wattHistory = MutableStateFlow<List<WattSample>>(emptyList())
    val wattHistory: StateFlow<List<WattSample>> = _wattHistory.asStateFlow()

    private val _benchmarkSessions = MutableStateFlow<List<BenchmarkSession>>(emptyList())
    val benchmarkSessions: StateFlow<List<BenchmarkSession>> = _benchmarkSessions.asStateFlow()

    private val _isRecordingBenchmark = MutableStateFlow(false)
    val isRecordingBenchmark: StateFlow<Boolean> = _isRecordingBenchmark.asStateFlow()

    private var currentBenchmarkAdapter = ""
    private var currentBenchmarkCable = ""

    init {
        _benchmarkSessions.value = loadSessionsFromDisk()
        startTelemetryLoop()
        observeServiceLiveStats()
    }

    private fun observeServiceLiveStats() {
        viewModelScope.launch {
            PowerTelemetryService.liveStats.collect { stats ->
                if (PowerTelemetryService.isServiceRunning.value) {
                    _powerStats.value = stats
                }
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            repository.getPowerStatsFlow(1000L).collect { stats ->
                if (!PowerTelemetryService.isServiceRunning.value) {
                    _powerStats.value = stats
                }

                // Maintain rolling history of last 60 seconds
                val currentHistory = _wattHistory.value.toMutableList()
                currentHistory.add(WattSample(_powerStats.value.powerWatts, _powerStats.value.isCharging))
                if (currentHistory.size > 60) {
                    currentHistory.removeAt(0)
                }
                _wattHistory.value = currentHistory
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
        _isRecordingBenchmark.value = true

        PowerTelemetryService.startService(
            context = getApplication(),
            isBenchmark = true,
            adapter = currentBenchmarkAdapter,
            cable = currentBenchmarkCable
        )
    }

    fun stopBenchmarkSession() {
        if (!_isRecordingBenchmark.value) return

        _isRecordingBenchmark.value = false
        val durationSec = ((System.currentTimeMillis() - PowerTelemetryService.benchmarkStartTime) / 1000).toInt().coerceAtLeast(1)

        val wattSamples = PowerTelemetryService.benchmarkWattSamples
        val voltSamples = PowerTelemetryService.benchmarkVoltSamples
        val ampSamples = PowerTelemetryService.benchmarkAmpSamples

        val wattValues = wattSamples.map { it.watt }
        val peakWatts = if (wattValues.isNotEmpty()) wattValues.maxOrNull() ?: 0.0 else _powerStats.value.powerWatts
        val avgWatts = if (wattValues.isNotEmpty()) wattValues.average() else _powerStats.value.powerWatts
        val avgVolts = if (voltSamples.isNotEmpty()) voltSamples.average() else _powerStats.value.voltageVolts
        val avgAmps = if (ampSamples.isNotEmpty()) ampSamples.average() else _powerStats.value.currentAmperes

        val startTime = PowerTelemetryService.benchmarkStartTime
        val endTime = System.currentTimeMillis()
        val isChargingRun = _powerStats.value.isCharging

        val session = BenchmarkSession(
            title = "$currentBenchmarkAdapter + $currentBenchmarkCable",
            adapterName = currentBenchmarkAdapter,
            cableName = currentBenchmarkCable,
            durationSeconds = durationSec,
            peakWatts = peakWatts,
            avgWatts = avgWatts,
            avgVoltage = avgVolts,
            avgAmperes = avgAmps,
            batteryLevelStart = PowerTelemetryService.benchmarkStartBatteryLevel,
            batteryLevelEnd = _powerStats.value.batteryLevel,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            isCharging = isChargingRun,
            wattSamples = ArrayList(wattSamples)
        )

        val updatedList = _benchmarkSessions.value.toMutableList()
        updatedList.add(0, session)
        _benchmarkSessions.value = updatedList
        saveSessionsToDisk(updatedList)

        PowerTelemetryService.stopService(getApplication())
    }

    fun deleteBenchmarkSession(sessionId: String) {
        val updatedList = _benchmarkSessions.value.filter { it.id != sessionId }
        _benchmarkSessions.value = updatedList
        saveSessionsToDisk(updatedList)
    }

    private fun saveSessionsToDisk(sessions: List<BenchmarkSession>) {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("watt_benchmark_prefs", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (session in sessions) {
                val obj = JSONObject().apply {
                    put("id", session.id)
                    put("title", session.title)
                    put("adapterName", session.adapterName)
                    put("cableName", session.cableName)
                    put("durationSeconds", session.durationSeconds)
                    put("peakWatts", session.peakWatts)
                    put("avgWatts", session.avgWatts)
                    put("avgVoltage", session.avgVoltage)
                    put("avgAmperes", session.avgAmperes)
                    put("batteryLevelStart", session.batteryLevelStart)
                    put("batteryLevelEnd", session.batteryLevelEnd)
                    put("startTimeMillis", session.startTimeMillis)
                    put("endTimeMillis", session.endTimeMillis)
                    put("isCharging", session.isCharging)
                    put("timestamp", session.timestamp)

                    val samplesArr = JSONArray()
                    session.wattSamples.forEach { sample ->
                        val sampleObj = JSONObject()
                        sampleObj.put("watt", sample.watt)
                        sampleObj.put("isCharging", sample.isCharging)
                        samplesArr.put(sampleObj)
                    }
                    put("wattSamples", samplesArr)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_sessions", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSessionsFromDisk(): List<BenchmarkSession> {
        val result = mutableListOf<BenchmarkSession>()
        try {
            val prefs = getApplication<Application>().getSharedPreferences("watt_benchmark_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("saved_sessions", null) ?: return emptyList()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val samplesList = mutableListOf<WattSample>()
                if (obj.has("wattSamples")) {
                    val samplesArr = obj.getJSONArray("wattSamples")
                    for (j in 0 until samplesArr.length()) {
                        val item = samplesArr.get(j)
                        if (item is JSONObject) {
                            samplesList.add(WattSample(item.getDouble("watt"), item.optBoolean("isCharging", true)))
                        } else if (item is Number) {
                            samplesList.add(WattSample(item.toDouble(), true))
                        }
                    }
                }
                val session = BenchmarkSession(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.optString("title", ""),
                    adapterName = obj.optString("adapterName", ""),
                    cableName = obj.optString("cableName", ""),
                    durationSeconds = obj.optInt("durationSeconds", 0),
                    peakWatts = obj.optDouble("peakWatts", 0.0),
                    avgWatts = obj.optDouble("avgWatts", 0.0),
                    avgVoltage = obj.optDouble("avgVoltage", 0.0),
                    avgAmperes = obj.optDouble("avgAmperes", 0.0),
                    batteryLevelStart = obj.optInt("batteryLevelStart", 0),
                    batteryLevelEnd = obj.optInt("batteryLevelEnd", 0),
                    startTimeMillis = obj.optLong("startTimeMillis", 0L),
                    endTimeMillis = obj.optLong("endTimeMillis", 0L),
                    isCharging = obj.optBoolean("isCharging", true),
                    wattSamples = samplesList,
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
                result.add(session)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
