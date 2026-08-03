package com.hobbycoding.wattbench.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hobbycoding.wattbench.data.model.BenchmarkSession
import com.hobbycoding.wattbench.data.model.PowerStats
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.data.repository.BatteryRepository
import com.hobbycoding.wattbench.service.PowerTelemetryService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val _pollingIntervalMs = MutableStateFlow(1000L)
    val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

    private val _showWelcomeTips = MutableStateFlow(false)
    val showWelcomeTips: StateFlow<Boolean> = _showWelcomeTips.asStateFlow()

    private var currentBenchmarkAdapter = ""
    private var currentBenchmarkCable = ""
    private var telemetryJob: Job? = null

    init {
        _benchmarkSessions.value = loadSessionsFromDisk()
        _showWelcomeTips.value = checkShouldShowWelcomeTips()
        startTelemetryLoop()
        observeServiceLiveStats()
    }

    private fun checkShouldShowWelcomeTips(): Boolean {
        val prefs = getApplication<Application>().getSharedPreferences("watt_benchmark_prefs", Context.MODE_PRIVATE)
        val dontShow = prefs.getBoolean("dont_show_welcome_tips", false)
        return !dontShow
    }

    fun dismissWelcomeTips(dontShowAgain: Boolean) {
        _showWelcomeTips.value = false
        if (dontShowAgain) {
            val prefs = getApplication<Application>().getSharedPreferences("watt_benchmark_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("dont_show_welcome_tips", true).apply()
        }
    }

    fun setPollingInterval(intervalMs: Long) {
        if (_pollingIntervalMs.value != intervalMs) {
            _pollingIntervalMs.value = intervalMs
            startTelemetryLoop()
        }
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
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            repository.getPowerStatsFlow(_pollingIntervalMs.value).collect { stats ->
                if (!PowerTelemetryService.isServiceRunning.value) {
                    _powerStats.value = stats
                }

                // Maintain rolling history of last 60 seconds
                val currentHistory = _wattHistory.value.toMutableList()
                currentHistory.add(WattSample(_powerStats.value.powerWatts, _powerStats.value.isCharging, _powerStats.value.batteryLevel, true))
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

    fun exportSessionsToCSV(context: Context): Boolean {
        val sessions = _benchmarkSessions.value
        if (sessions.isEmpty()) return false

        try {
            val fileName = "WattBench_Benchmark_Results_${System.currentTimeMillis()}.csv"
            val cacheFile = File(context.cacheDir, fileName)

            FileWriter(cacheFile).use { writer ->
                writer.append("ID,Title,Adapter,Cable,Duration(sec),Peak Watts,Avg Watts,Avg Voltage(V),Avg Current(A),Start Battery%,End Battery%,Start Time,End Time\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                for (s in sessions) {
                    val startStr = if (s.startTimeMillis > 0) dateFormat.format(Date(s.startTimeMillis)) else ""
                    val endStr = if (s.endTimeMillis > 0) dateFormat.format(Date(s.endTimeMillis)) else ""
                    writer.append("\"${s.id}\",\"${s.title}\",\"${s.adapterName}\",\"${s.cableName}\",${s.durationSeconds},${s.peakWatts},${s.avgWatts},${s.avgVoltage},${s.avgAmperes},${s.batteryLevelStart},${s.batteryLevelEnd},\"$startStr\",\"$endStr\"\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Export Benchmark CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
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
                        sampleObj.put("batteryLevel", sample.batteryLevel)
                        sampleObj.put("isScreenOn", sample.isScreenOn)
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
                            samplesList.add(
                                WattSample(
                                    watt = item.getDouble("watt"),
                                    isCharging = item.optBoolean("isCharging", true),
                                    batteryLevel = item.optInt("batteryLevel", 0),
                                    isScreenOn = item.optBoolean("isScreenOn", true)
                                )
                            )
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
