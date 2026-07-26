package com.example.wattcalculator.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.wattcalculator.MainActivity
import com.example.wattcalculator.data.model.PowerStats
import com.example.wattcalculator.data.repository.BatteryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class PowerTelemetryService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var batteryRepository: BatteryRepository
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "watt_telemetry_channel"
        const val NOTIFICATION_ID = 1001

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _liveStats = MutableStateFlow(PowerStats())
        val liveStats: StateFlow<PowerStats> = _liveStats.asStateFlow()

        private val _benchmarkWattSamples = mutableListOf<com.example.wattcalculator.data.model.WattSample>()
        val benchmarkWattSamples: List<com.example.wattcalculator.data.model.WattSample> get() = _benchmarkWattSamples

        private val _benchmarkVoltSamples = mutableListOf<Double>()
        val benchmarkVoltSamples: List<Double> get() = _benchmarkVoltSamples

        private val _benchmarkAmpSamples = mutableListOf<Double>()
        val benchmarkAmpSamples: List<Double> get() = _benchmarkAmpSamples

        var isRecordingBenchmark = false
        var benchmarkStartTime = 0L
        var benchmarkStartBatteryLevel = 0
        var adapterName = ""
        var cableName = ""

        fun startService(context: Context, isBenchmark: Boolean = false, adapter: String = "", cable: String = "") {
            val intent = Intent(context, PowerTelemetryService::class.java).apply {
                putExtra("IS_BENCHMARK", isBenchmark)
                putExtra("ADAPTER_NAME", adapter)
                putExtra("CABLE_NAME", cable)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PowerTelemetryService::class.java)
            context.stopService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): PowerTelemetryService = this@PowerTelemetryService
    }

    override fun onCreate() {
        super.onCreate()
        batteryRepository = BatteryRepository(this)
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WattCalculator::TelemetryWakeLock")
        wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max timeout
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isBenchmark = intent?.getBooleanExtra("IS_BENCHMARK", false) ?: false
        if (isBenchmark) {
            isRecordingBenchmark = true
            adapterName = intent?.getStringExtra("ADAPTER_NAME") ?: "Charger"
            cableName = intent?.getStringExtra("CABLE_NAME") ?: "Cable"
            benchmarkStartTime = System.currentTimeMillis()
            _benchmarkWattSamples.clear()
            _benchmarkVoltSamples.clear()
            _benchmarkAmpSamples.clear()
        }

        startForeground(NOTIFICATION_ID, buildNotification("Monitoring power telemetry..."))
        _isServiceRunning.value = true
        startTelemetryLoop()

        return START_STICKY
    }

    private fun startTelemetryLoop() {
        serviceScope.launch {
            while (isActive) {
                val stats = batteryRepository.fetchCurrentPowerStats()
                _liveStats.value = stats

                if (isRecordingBenchmark) {
                    _benchmarkWattSamples.add(com.example.wattcalculator.data.model.WattSample(stats.powerWatts, stats.isCharging))
                    _benchmarkVoltSamples.add(stats.voltageVolts)
                    _benchmarkAmpSamples.add(stats.currentAmperes)
                }

                updateNotification(stats)
                delay(1000L)
            }
        }
    }

    private fun updateNotification(stats: PowerStats) {
        val contentText = if (stats.isCharging) {
            String.format(Locale.US, "Charging: %.1f W | %.2f V | %.0f mA", stats.powerWatts, stats.voltageVolts, stats.currentMilliAmperes)
        } else {
            String.format(Locale.US, "Discharging: %.1f W | Battery: %d%%", stats.powerWatts, stats.batteryLevel)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isRecordingBenchmark) "⚡ Watt Benchmark Recording" else "⚡ Live Watt Telemetry")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Watt Power Telemetry",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time power measurements when screen is off"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        _isServiceRunning.value = false
        isRecordingBenchmark = false
    }
}
