package com.hobbycoding.wattbench.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.hobbycoding.wattbench.R
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
            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.plug_ac)
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.plug_usb)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.plug_wireless)
            else -> if (isCharging) context.getString(R.string.plug_connected) else context.getString(R.string.plug_disconnected)
        }

        val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val healthStr = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.health_good)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.health_overheat)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.health_dead)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.health_over_voltage)
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.health_cold)
            else -> context.getString(R.string.health_unspecified)
        }

        val batteryCapacityMah = getBatteryCapacityMah()

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
            batteryHealth = healthStr,
            batteryCapacityMah = batteryCapacityMah,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseCurrentToAmperes(rawCurrent: Int, isCharging: Boolean): Double {
        if (rawCurrent == Int.MIN_VALUE) return 0.0
        val absVal = abs(rawCurrent).toDouble()
        if (absVal == 0.0) return 0.0

        val amperes = when {
            absVal > 10000.0 -> absVal / 1_000_000.0
            else -> absVal / 1000.0
        }

        return amperes
    }

    private fun getChargingSpeedCategory(watts: Double, isCharging: Boolean): String {
        if (!isCharging) return context.getString(R.string.profile_not_charging)
        return when {
            watts >= 65.0 -> context.getString(R.string.profile_ultra_hyper)
            watts >= 33.0 -> context.getString(R.string.profile_super_turbo)
            watts >= 18.0 -> context.getString(R.string.profile_fast)
            watts >= 7.5 -> context.getString(R.string.profile_normal)
            else -> context.getString(R.string.profile_slow)
        }
    }

    private fun getBatteryCapacityMah(): Int {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            val capacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
            capacity.toInt()
        } catch (e: Exception) {
            0
        }
    }
}
