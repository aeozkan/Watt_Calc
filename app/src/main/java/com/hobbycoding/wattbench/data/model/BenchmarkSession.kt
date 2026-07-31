package com.hobbycoding.wattbench.data.model

import java.util.UUID

data class BenchmarkSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val adapterName: String,
    val cableName: String,
    val durationSeconds: Int,
    val peakWatts: Double,
    val avgWatts: Double,
    val avgVoltage: Double,
    val avgAmperes: Double,
    val batteryLevelStart: Int,
    val batteryLevelEnd: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isCharging: Boolean = true,
    val wattSamples: List<WattSample> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
