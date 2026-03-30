package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.PerformanceProfiles

@Composable
fun DashboardScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Cihaz başlık kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhoneAndroid, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Poco X6 Pro 5G",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("Dimensity 8300 Ultra (MT6897)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Android ${state.deviceBasic["Android"] ?: "—"}  ·  ${state.deviceBasic["Build"]?.take(20) ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    if (state.isRooted) {
                        StatusBadge("Root", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Aktif profil
        item {
            val active = PerformanceProfiles.presets.find { it.id == state.activeProfileId }
            if (active != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(profileIcon(active.id), null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aktif Profil",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(active.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Text(active.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Metrik grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.batteryInfo?.let { bat ->
                    MetricCard(
                        label = "Batarya",
                        value = "${bat.capacity}%",
                        icon = Icons.Default.BatteryFull,
                        color = if (bat.capacity > 20) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.error,
                        sub = bat.status,
                        modifier = Modifier.weight(1f)
                    )
                }
                state.cpuInfo?.let { cpu ->
                    MetricCard(
                        label = "CPU",
                        value = cpu.clusterPrime.curFreqMhz,
                        icon = Icons.Default.Memory,
                        color = MaterialTheme.colorScheme.primary,
                        sub = cpu.governor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.memInfo?.let { mem ->
                    val pct = if (mem.totalMb > 0) (mem.usedMb * 100 / mem.totalMb).toInt() else 0
                    MetricCard(
                        label = "RAM",
                        value = "$pct%",
                        icon = Icons.Default.Storage,
                        color = MaterialTheme.colorScheme.secondary,
                        sub = "${mem.usedMb} / ${mem.totalMb} MB",
                        modifier = Modifier.weight(1f)
                    )
                }
                state.gpuInfo?.let { gpu ->
                    MetricCard(
                        label = "GPU",
                        value = gpu.curFreqMhz,
                        icon = Icons.Default.Videocam,
                        color = Color(0xFFF59E0B),
                        sub = gpu.governor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // CPU cluster özeti
        state.cpuInfo?.let { cpu ->
            item {
                SectionCard("CPU Cluster Durumu", Icons.Default.Memory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Little", cpu.clusterLittle.curFreqMhz, Color(0xFF10B981)),
                            Triple("Big",    cpu.clusterBig.curFreqMhz,    MaterialTheme.colorScheme.primary),
                            Triple("Prime",  cpu.clusterPrime.curFreqMhz,  Color(0xFFF59E0B))
                        ).forEach { (name, freq, color) ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = color.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(name, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(freq, style = MaterialTheme.typography.labelMedium, color = color)
                                }
                            }
                        }
                    }
                    SectionDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cpu.cores.take(8).forEach { core ->
                            val clColor = when (core.cluster) {
                                "Little" -> Color(0xFF10B981)
                                "Big"    -> MaterialTheme.colorScheme.primary
                                "Prime"  -> Color(0xFFF59E0B)
                                else     -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (core.online) clColor.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("C${core.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(core.freqMhz.replace(" MHz", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (core.online) clColor
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Batarya özet
        state.batteryInfo?.let { bat ->
            item {
                SectionCard("Batarya", Icons.Default.BatteryFull) {
                    UsageBar("Şarj Seviyesi", bat.capacity / 100f,
                        color = if (bat.capacity > 20) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Durum" to bat.status,
                            "Sıcaklık" to bat.tempC,
                            "Akım" to bat.currentNow,
                            "Voltaj" to bat.voltage
                        ).forEach { (k, v) ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(v, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(k, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Termal özet
        if (state.thermals.isNotEmpty()) {
            item {
                SectionCard("Termal", Icons.Default.Thermostat) {
                    state.thermals.take(6).forEach { t ->
                        val raw = t.tempRaw
                        val color = when {
                            raw > 80000 -> MaterialTheme.colorScheme.error
                            raw > 60000 -> Color(0xFFF59E0B)
                            else        -> MaterialTheme.colorScheme.onSurface
                        }
                        InfoRow(t.zone, t.tempC, color)
                    }
                }
            }
        }

        // Sistem özet
        state.systemInfo?.let { sys ->
            item {
                SectionCard("Sistem", Icons.Default.Info) {
                    InfoRow("Kernel", sys.kernelVersion)
                    InfoRow("SELinux", sys.selinuxMode,
                        if (sys.selinuxMode == "Enforcing") MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error)
                    InfoRow("Uptime", sys.uptime)
                    InfoRow("Load Avg", sys.loadAvg)
                }
            }
        }
    }
}
