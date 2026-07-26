package com.example.wattcalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wattcalculator.data.model.PowerStats
import com.example.wattcalculator.ui.components.MetricCard
import com.example.wattcalculator.ui.components.PowerChart
import com.example.wattcalculator.ui.components.WattGauge
import com.example.wattcalculator.ui.theme.NeonAmber
import com.example.wattcalculator.ui.theme.NeonCyan
import com.example.wattcalculator.ui.theme.NeonGreen
import com.example.wattcalculator.ui.theme.NeonPurple
import com.example.wattcalculator.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun HomeScreen(
    powerStats: PowerStats,
    wattHistory: List<com.example.wattcalculator.data.model.WattSample>,
    onResetStats: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WATT CALCULATOR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (powerStats.isCharging) "CHARGING ACTIVE • ${powerStats.chargePlugType.uppercase()}" else "DISCONNECTED",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (powerStats.isCharging) NeonGreen else TextSecondary
                )
            }
            IconButton(
                onClick = onResetStats,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed Classification Chip
        if (powerStats.isCharging) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonPurple.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CHARGING PROFILE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = powerStats.chargingSpeedCategory,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Watt Gauge
        WattGauge(
            watts = powerStats.powerWatts,
            peakWatts = powerStats.peakPowerWatts,
            avgWatts = powerStats.averagePowerWatts,
            isCharging = powerStats.isCharging
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metric Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Voltage",
                value = String.format(Locale.US, "%.2f", powerStats.voltageVolts),
                unit = "V",
                icon = Icons.Default.ElectricBolt,
                iconColor = NeonAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Current",
                value = String.format(Locale.US, "%.0f", powerStats.currentMilliAmperes),
                unit = "mA",
                icon = Icons.Default.Speed,
                iconColor = NeonCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Battery Level",
                value = "${powerStats.batteryLevel}",
                unit = "%",
                icon = Icons.Default.BatteryChargingFull,
                iconColor = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Temperature",
                value = String.format(Locale.US, "%.1f", powerStats.temperatureCelsius),
                unit = "°C",
                icon = Icons.Default.Thermostat,
                iconColor = Color(0xFFF43F5E),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Power Chart
        PowerChart(history = wattHistory)

        Spacer(modifier = Modifier.height(24.dp))
    }
}
