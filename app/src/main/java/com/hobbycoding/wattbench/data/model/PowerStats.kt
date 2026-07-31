package com.hobbycoding.wattbench.data.model

data class PowerStats(
    val voltageVolts: Double = 0.0,
    val currentAmperes: Double = 0.0,
    val currentMilliAmperes: Double = 0.0,
    val powerWatts: Double = 0.0,
    val batteryLevel: Int = 0,
    val temperatureCelsius: Double = 0.0,
    val isCharging: Boolean = false,
    val chargePlugType: String = "Disconnected",
    val chargingSpeedCategory: String = "Not Charging",
    val peakPowerWatts: Double = 0.0,
    val averagePowerWatts: Double = 0.0,
    val batteryHealth: String = "Good",
    val batteryCapacityMah: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
