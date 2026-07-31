package com.hobbycoding.wattbench.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hobbycoding.wattbench.R
import com.hobbycoding.wattbench.data.model.PowerStats
import com.hobbycoding.wattbench.ui.theme.NeonAmber
import com.hobbycoding.wattbench.ui.theme.NeonCyan
import com.hobbycoding.wattbench.ui.theme.NeonGreen
import com.hobbycoding.wattbench.ui.theme.NeonPurple
import com.hobbycoding.wattbench.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    powerStats: PowerStats,
    currentPollingIntervalMs: Long,
    onPollingIntervalChanged: (Long) -> Unit,
    onExportCSV: (Context) -> Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            // Sheet Header Title
            Text(
                text = stringResource(R.string.menu_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. SETTINGS & PREFERENCES
            SectionTitle(title = stringResource(R.string.section_settings), icon = Icons.Default.Settings, iconColor = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.polling_rate_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PollingOptionRow(
                        label = stringResource(R.string.polling_500ms),
                        selected = currentPollingIntervalMs == 500L,
                        onClick = { onPollingIntervalChanged(500L) }
                    )
                    PollingOptionRow(
                        label = stringResource(R.string.polling_1000ms),
                        selected = currentPollingIntervalMs == 1000L,
                        onClick = { onPollingIntervalChanged(1000L) }
                    )
                    PollingOptionRow(
                        label = stringResource(R.string.polling_2000ms),
                        selected = currentPollingIntervalMs == 2000L,
                        onClick = { onPollingIntervalChanged(2000L) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. BATTERY & DEVICE INFO
            SectionTitle(title = stringResource(R.string.section_battery_info), icon = Icons.Default.BatteryStd, iconColor = NeonGreen)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(
                        label = stringResource(R.string.battery_health_label),
                        value = powerStats.batteryHealth,
                        valueColor = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        label = stringResource(R.string.battery_capacity_label),
                        value = if (powerStats.batteryCapacityMah > 0) "${powerStats.batteryCapacityMah} mAh" else "N/A",
                        valueColor = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. DATA EXPORT
            SectionTitle(title = stringResource(R.string.section_export), icon = Icons.Default.Download, iconColor = NeonAmber)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val success = onExportCSV(context)
                    if (!success) {
                        Toast.makeText(context, context.getString(R.string.msg_no_sessions_export), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_export_csv),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. ABOUT & LEGAL
            SectionTitle(title = stringResource(R.string.section_about), icon = Icons.Default.Info, iconColor = NeonPurple)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    MenuActionRow(
                        icon = Icons.Default.Policy,
                        label = stringResource(R.string.menu_privacy_policy),
                        onClick = { showPrivacyDialog = true }
                    )
                    MenuActionRow(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.menu_rate_app),
                        onClick = {
                            val appPackageName = context.packageName
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                            }
                        }
                    )
                    MenuActionRow(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.menu_about),
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.privacy_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.privacy_dialog_body), fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(stringResource(R.string.dialog_close), color = NeonCyan)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.about_dialog_body), fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.dialog_close), color = NeonCyan)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}

@Composable
private fun PollingOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 14.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun MenuActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
