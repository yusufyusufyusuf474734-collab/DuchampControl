package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.PerformanceProfiles
import com.duchamp.control.ThermalInfo

@Composable
fun ThermalScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var alertTempC by remember { mutableFloatStateOf(state.thermalAlertTempC.toFloat()) }
    var throttleTempC by remember { mutableFloatStateOf(state.thermalThrottleTempC.toFloat()) }

    LaunchedEffect(Unit) { vm.loadMtkEas() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sıcaklık bildirimi
        item {
            SectionCard("CPU Sıcaklık Bildirimi", Icons.Default.NotificationsActive) {
                ControlRow(
                    label = "Sıcaklık Uyarısı",
                    description = "Eşik aşılınca sistem bildirimi gönder",
                    checked = state.thermalAlertEnabled,
                    onCheckedChange = { vm.setThermalAlert(context, it, alertTempC.toInt()) }
                )
                Spacer(Modifier.height(10.dp))
                SliderRow(
                    label = "Uyarı Eşiği",
                    value = alertTempC,
                    valueRange = 50f..95f,
                    steps = 8,
                    displayValue = "${alertTempC.toInt()}°C",
                    onValueChangeFinished = {
                        alertTempC = it
                        vm.setThermalAlert(context, state.thermalAlertEnabled, it.toInt())
                    }
                )
                if (state.thermalAlertEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("≥${alertTempC.toInt()}°C olunca bildirim gönderilir",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Termal throttle
        item {
            SectionCard("Otomatik Termal Throttle", Icons.Default.Thermostat) {
                Text(
                    "CPU sıcaklığı belirlenen eşiği aştığında seçili profil otomatik uygulanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                ControlRow(
                    label = "Termal Throttle",
                    description = "Aşırı ısınmayı önlemek için otomatik profil değiştir",
                    checked = state.thermalThrottleEnabled,
                    onCheckedChange = {
                        vm.setThermalThrottle(it, throttleTempC.toInt(), state.thermalThrottleProfileId)
                    }
                )
                Spacer(Modifier.height(10.dp))
                SliderRow(
                    label = "Sıcaklık Eşiği",
                    value = throttleTempC,
                    valueRange = 50f..95f,
                    steps = 8,
                    displayValue = "${throttleTempC.toInt()}°C",
                    onValueChangeFinished = {
                        throttleTempC = it
                        vm.setThermalThrottle(state.thermalThrottleEnabled, it.toInt(), state.thermalThrottleProfileId)
                    }
                )
                Spacer(Modifier.height(10.dp))
                Text("Uygulanacak Profil",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerformanceProfiles.presets.forEach { p ->
                        FilterChip(
                            selected = state.thermalThrottleProfileId == p.id,
                            onClick = { vm.setThermalThrottle(state.thermalThrottleEnabled, throttleTempC.toInt(), p.id) },
                            label = { Text(p.name, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(profileIcon(p.id), null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
                if (state.thermalThrottleEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("≥${throttleTempC.toInt()}°C → ${
                                PerformanceProfiles.presets.find { it.id == state.thermalThrottleProfileId }?.name ?: state.thermalThrottleProfileId
                            }",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
            }
        }

        // MTK EAS/HMP
        item {
            SectionCard("MTK EAS / CPU Boost", Icons.Default.Bolt) {
                val eas = state.mtkEasInfo
                if (eas == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Yükleniyor...", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    InfoRow("Sched Boost", eas.schedBoost)
                    InfoRow("Input Boost Frekans", eas.inputBoostFreq)
                    InfoRow("Input Boost Süresi", "${eas.inputBoostDuration} ms")
                    InfoRow("Sched Down Migrate", "${eas.schedDownMigrateLoad}%")
                    InfoRow("Sched Up Migrate", "${eas.schedUpMigrateLoad}%")
                    SectionDivider()
                    ControlRow(
                        label = "CPU Input Boost",
                        description = "Dokunuş anında CPU frekansını geçici artır",
                        checked = eas.cpuInputBoostEnabled,
                        onCheckedChange = {
                            vm.setMtkParam("/sys/module/cpu_boost/parameters/input_boost_enabled",
                                if (it) "1" else "0", "CPU Input Boost")
                        }
                    )
                    SectionDivider()
                    Text("Sched Boost Seviyesi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("0" to "Kapalı", "1" to "Hafif", "2" to "Orta", "3" to "Tam").forEach { (v, lbl) ->
                            FilterChip(
                                selected = eas.schedBoost == v,
                                onClick = { vm.setMtkParam("/proc/sys/kernel/sched_boost", v, "Sched Boost") },
                                label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    SectionDivider()
                    Text("Input Boost Süresi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("40" to "40ms", "80" to "80ms", "120" to "120ms", "200" to "200ms").forEach { (v, lbl) ->
                            FilterChip(
                                selected = eas.inputBoostDuration == v,
                                onClick = { vm.setMtkParam("/sys/module/cpu_boost/parameters/input_boost_ms", v, "Input Boost Süresi") },
                                label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { vm.loadMtkEas() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Yenile")
                }
            }
        }

        // Termal bölgeler
        item {
            SectionCard("Termal Bölgeler", Icons.Default.DeviceThermostat,
                badge = "${state.thermals.size} bölge") {
                if (state.thermals.isEmpty()) {
                    EmptyState(Icons.Default.Thermostat, "Termal bilgi alınamadı")
                } else {
                    state.thermals.forEach { t ->
                        ThermalZoneRow(t)
                        if (t != state.thermals.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = { vm.refreshThermals() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Termal Bilgiyi Yenile")
            }
        }
    }
}

@Composable
fun ThermalZoneRow(t: ThermalInfo) {
    val (color, label) = when {
        t.tempRaw > 80000 -> MaterialTheme.colorScheme.error to "Kritik"
        t.tempRaw > 60000 -> Color(0xFFF59E0B) to "Yüksek"
        t.tempRaw > 40000 -> MaterialTheme.colorScheme.secondary to "Normal"
        else               -> MaterialTheme.colorScheme.onSurfaceVariant to "Soğuk"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(shape = MaterialTheme.shapes.extraSmall, color = color,
                modifier = Modifier.size(8.dp)) {}
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.zone, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
        Text(t.tempC, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
