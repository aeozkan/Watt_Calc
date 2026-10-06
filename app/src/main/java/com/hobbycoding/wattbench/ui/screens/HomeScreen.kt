package com.hobbycoding.wattbench.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.data.model.PowerStats
import com.hobbycoding.wattbench.data.model.WattSample
import com.hobbycoding.wattbench.ui.components.MetricCard
import com.hobbycoding.wattbench.ui.components.PowerChart
import com.hobbycoding.wattbench.ui.components.WattGauge
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
import com.hobbycoding.wattbench.ui.theme.NeonPurple
import com.hobbycoding.wattbench.ui.theme.TextSecondary
import com.hobbycoding.wattbench.util.LocaleHelper
import java.util.Locale

@Composable
fun HomeScreen(
    powerStats: PowerStats,
    wattHistory: List<WattSample>,
    onResetStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var easterEggTapCount by remember { mutableIntStateOf(0) }
    var lastEasterEggTapTime by remember { mutableLongStateOf(0L) }
    var showSecretLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header with Top-Left Menu Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val now = System.currentTimeMillis()
                        if (now - lastEasterEggTapTime > 1500L) {
                            easterEggTapCount = 1
                        } else {
                            easterEggTapCount++
                        }
                        lastEasterEggTapTime = now

                        if (easterEggTapCount >= 5) {
                            easterEggTapCount = 0
                            showSecretLanguageDialog = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.app_header_live),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = if (powerStats.isCharging) {
                            stringResource(R.string.status_charging_active, powerStats.chargePlugType.uppercase())
                        } else {
                            stringResource(R.string.status_disconnected)
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (powerStats.isCharging) NeonGreen else TextSecondary
                    )
                }
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
                        text = stringResource(R.string.profile_header),
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
                title = stringResource(R.string.metric_voltage),
                value = String.format(Locale.US, "%.2f", powerStats.voltageVolts),
                unit = "V",
                icon = Icons.Default.ElectricBolt,
                iconColor = NeonAmber,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.metric_current),
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
                title = stringResource(R.string.metric_battery_level),
                value = "${powerStats.batteryLevel}",
                unit = "%",
                icon = Icons.Default.BatteryChargingFull,
                iconColor = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(R.string.metric_temperature),
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

    if (showSecretLanguageDialog) {
        val currentLang = LocaleHelper.getSelectedLanguage(context) // null = system default, "tr" = Türkçe, "en" = English

        AlertDialog(
            onDismissRequest = { showSecretLanguageDialog = false },
            title = {
                Text(
                    text = "🛠️ Developer Language Mode",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = NeonCyan
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Gizli geliştirici modu aktif. Uygulama arayüz dilini anında değiştirebilirsiniz:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LanguageOptionItem(
                        label = "🇹🇷 Türkçe",
                        isSelected = currentLang == "tr",
                        onClick = {
                            showSecretLanguageDialog = false
                            LocaleHelper.setLanguage(context, "tr")
                            Toast.makeText(context, "Dil: Türkçe olarak ayarlandı", Toast.LENGTH_SHORT).show()
                        }
                    )

                    LanguageOptionItem(
                        label = "🇬🇧 English",
                        isSelected = currentLang == "en",
                        onClick = {
                            showSecretLanguageDialog = false
                            LocaleHelper.setLanguage(context, "en")
                            Toast.makeText(context, "Language: English", Toast.LENGTH_SHORT).show()
                        }
                    )

                    LanguageOptionItem(
                        label = "⚙️ Sistem Varsayılanı (System Default)",
                        isSelected = currentLang == null,
                        onClick = {
                            showSecretLanguageDialog = false
                            LocaleHelper.setLanguage(context, null)
                            Toast.makeText(context, "Sistem varsayılan diline dönüldü", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecretLanguageDialog = false }) {
                    Text("Kapat", color = NeonCyan)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun LanguageOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, NeonCyan) else BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Text(
                    text = "✓",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }
    }
}
