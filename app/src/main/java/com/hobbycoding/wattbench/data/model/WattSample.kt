package com.hobbycoding.wattbench.data.model

data class WattSample(
    val watt: Double,
    val isCharging: Boolean = true,
    val batteryLevel: Int = 0,
    val isScreenOn: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
