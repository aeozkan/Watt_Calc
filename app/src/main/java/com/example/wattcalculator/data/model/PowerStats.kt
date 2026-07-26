package com.example.wattcalculator.data.model

data class WattSample(
    val watt: Double,
    val isCharging: Boolean
)

data class PowerStats(
    val voltageVolts: Double = 0.0,
    val currentAmperes: Double = 0.0,
    val currentMilliAmperes: Double = 0.0,
    val powerWatts: Double = 0.0,
    val batteryLevel: Int = 0,
    val temperatureCelsius: Double = 0.0,
    val isCharging: Boolean = false,
    val chargePlugType: String = "Disconnected",
    val chargingSpeedCategory: String = "None",
    val peakPowerWatts: Double = 0.0,
    val averagePowerWatts: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class BenchmarkSession(
    val id: String = java.util.UUID.randomUUID().toString(),
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
