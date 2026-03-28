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

@Composable
fun DashboardScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cihaz başlık kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Poco X6 Pro 5G", style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Dimensity 8300 Ultra · Android ${state.deviceBasic["Android"] ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(state.deviceBasic["Build"] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Hızlı istatistikler
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.batteryInfo?.let {
                    item {
                        StatChipSmall("Batarya", "${it.capacity}%",
                            if (it.capacity > 20) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error)
                    }
                    item { StatChipSmall("Sıcaklık", it.tempC, MaterialTheme.colorScheme.secondary) }
                }
                state.cpuInfo?.let {
                    item { StatChipSmall("CPU", it.curFreqMhz, MaterialTheme.colorScheme.tertiary) }
                    item { StatChipSmall("Governor", it.governor, MaterialTheme.colorScheme.primary) }
                }
                state.memInfo?.let {
                    item {
                        val usedPct = if (it.totalMb > 0) (it.usedMb * 100 / it.totalMb).toInt() else 0
                        StatChipSmall("RAM", "%$usedPct", MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // CPU özet
        state.cpuInfo?.let { cpu ->
            item {
                SectionCard("CPU", Icons.Default.Memory) {
                    InfoRow("Governor", cpu.governor)
                    InfoRow("Mevcut Frekans", cpu.curFreqMhz)
                    InfoRow("Max / Min", "${cpu.maxFreqMhz} / ${cpu.minFreqMhz}")
                    SectionDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cpu.cores.take(8).forEach { core ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (core.online) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("C${core.id}", style = MaterialTheme.typography.labelSmall)
                                    Text(core.freqMhz.replace(" MHz", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // GPU özet
        state.gpuInfo?.let { gpu ->
            item {
                SectionCard("GPU - Mali G615-MC6", Icons.Default.Videocam) {
                    InfoRow("Governor", gpu.governor)
                    InfoRow("Frekans", gpu.curFreqMhz)
                    InfoRow("Kullanım", gpu.utilization)
                }
            }
        }

        // Batarya özet
        state.batteryInfo?.let { bat ->
            item {
                SectionCard("Batarya", Icons.Default.BatteryFull) {
                    val pct = bat.capacity.toFloat() / 100f
                    UsageBar("Şarj Seviyesi", pct)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Durum", bat.status)
                    InfoRow("Sağlık", bat.health)
                    InfoRow("Sıcaklık", bat.tempC)
                    InfoRow("Akım", bat.currentNow)
                }
            }
        }

        // RAM özet
        state.memInfo?.let { mem ->
            item {
                SectionCard("Bellek", Icons.Default.Storage) {
                    val usedPct = if (mem.totalMb > 0) mem.usedMb.toFloat() / mem.totalMb.toFloat() else 0f
                    UsageBar("RAM Kullanımı", usedPct)
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Toplam", "${mem.totalMb} MB")
                    InfoRow("Kullanılan", "${mem.usedMb} MB")
                    InfoRow("Boş", "${mem.availMb} MB")
                }
            }
        }

        // Termal özet (ilk 5)
        if (state.thermals.isNotEmpty()) {
            item {
                SectionCard("Termal (Özet)", Icons.Default.Thermostat) {
                    state.thermals.take(5).forEach { t ->
                        val raw = t.tempRaw
                        val color = when {
                            raw > 80000 -> MaterialTheme.colorScheme.error
                            raw > 60000 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        InfoRow(t.zone, t.tempC, color)
                    }
                }
            }
        }

        // Sistem bilgisi
        state.systemInfo?.let { sys ->
            item {
                SectionCard("Sistem", Icons.Default.Info) {
                    InfoRow("Kernel", sys.kernelVersion)
                    InfoRow("SELinux", sys.selinuxMode,
                        if (sys.selinuxMode == "Enforcing") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                    InfoRow("Uptime", sys.uptime)
                    InfoRow("Load Avg", sys.loadAvg)
                }
            }
        }
    }
}
