package com.hobbycoding.wattbench.data.model

data class WattSample(
    val watt: Double,
    val isCharging: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
