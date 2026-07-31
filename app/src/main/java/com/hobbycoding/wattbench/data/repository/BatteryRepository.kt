package com.hobbycoding.wattbench.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.hobbycoding.wattbench.data.model.PowerStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

class BatteryRepository(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private var peakWatts: Double = 0.0
    private var totalWattsSum: Double = 0.0
    private var sampleCount: Int = 0

    fun resetPeakAndAverage() {
        peakWatts = 0.0
        totalWattsSum = 0.0
        sampleCount = 0
    }

    fun getPowerStatsFlow(pollIntervalMs: Long = 1000L): Flow<PowerStats> = flow {
        while (true) {
            val stats = fetchCurrentPowerStats()
            emit(stats)
            delay(pollIntervalMs)
        }
    }

    fun fetchCurrentPowerStats(): PowerStats {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val rawVoltageMs = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val voltageVolts = if (rawVoltageMs > 0) rawVoltageMs / 1000.0 else 0.0

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 0

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = tempTenths / 10.0

        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Connected" else "Disconnected"
        }

        // Fetch current from BatteryManager property
        val rawCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        // Convert current (handles uA vs mA automatically based on scale)
        val currentAmperes: Double = parseCurrentToAmperes(rawCurrentNow, isCharging)
        val currentMilliAmperes: Double = currentAmperes * 1000.0

        // Calculate Wattage: P = V * I
        val powerWatts = voltageVolts * currentAmperes

        if (isCharging && powerWatts > peakWatts) {
            peakWatts = powerWatts
        }

        if (isCharging && powerWatts > 0) {
            totalWattsSum += powerWatts
            sampleCount++
        }

        val avgWatts = if (sampleCount > 0) totalWattsSum / sampleCount else powerWatts

        val speedCategory = getChargingSpeedCategory(powerWatts, isCharging)

        return PowerStats(
            voltageVolts = voltageVolts,
            currentAmperes = currentAmperes,
            currentMilliAmperes = currentMilliAmperes,
            powerWatts = powerWatts,
            batteryLevel = batteryPct,
            temperatureCelsius = tempCelsius,
            isCharging = isCharging,
            chargePlugType = plugType,
            chargingSpeedCategory = speedCategory,
            peakPowerWatts = peakWatts,
            averagePowerWatts = avgWatts,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseCurrentToAmperes(rawCurrent: Int, isCharging: Boolean): Double {
        val absVal = abs(rawCurrent).toDouble()
        if (absVal == 0.0) return 0.0

        val amperes = when {
            absVal > 10000.0 -> absVal / 1_000_000.0
            else -> absVal / 1000.0
        }

        return amperes
    }

    private fun getChargingSpeedCategory(watts: Double, isCharging: Boolean): String {
        if (!isCharging) return "Not Charging"
        return when {
            watts >= 65.0 -> "Ultra HyperCharge (65W+)"
            watts >= 33.0 -> "Super Turbo Charge (33W+)"
            watts >= 18.0 -> "Fast Charge (18W+)"
            watts >= 7.5 -> "Normal Charge (7.5W+)"
            else -> "Slow / Trickle Charge"
        }
    }
}
