package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun CpuScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val cpu = state.cpuInfo ?: return
    val gpu = state.gpuInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CPU Genel
        item {
            SectionCard("CPU - Dimensity 8300 Ultra", Icons.Default.Memory) {
                InfoRow("Çekirdek Sayısı", "${cpu.cores.size}")
                InfoRow("Mevcut Frekans", cpu.curFreqMhz)
                InfoRow("Max Frekans", cpu.maxFreqMhz)
                InfoRow("Min Frekans", cpu.minFreqMhz)
                InfoRow("Governor", cpu.governor)
            }
        }

        // Çekirdek durumu
        item {
            SectionCard("Çekirdek Frekansları", Icons.Default.Speed) {
                cpu.cores.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { core ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (core.online)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("CPU${core.id}", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        core.freqMhz,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (core.online) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        if (core.online) "Aktif" else "Kapalı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        // Boş hücre dolgusu
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Governor seçimi
        if (cpu.availableGovernors.isNotEmpty()) {
            item {
                SectionCard("CPU Governor", Icons.Default.Tune) {
                    Text("Aktif governor performans ve pil ömrünü etkiler.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(8.dp))
                    ChipGroup(
                        label = "Governor Seç",
                        items = cpu.availableGovernors,
                        selected = cpu.governor,
                        onSelect = { vm.setCpuGovernor(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                    val desc = when (cpu.governor) {
                        "performance" -> "Maksimum performans, yüksek güç tüketimi"
                        "powersave"   -> "Minimum frekans, maksimum pil tasarrufu"
                        "schedutil"   -> "Kernel scheduler tabanlı, dengeli"
                        "interactive" -> "Etkileşim odaklı, hızlı tepki"
                        "ondemand"    -> "Yüke göre dinamik frekans"
                        else          -> "Seçili governor"
                    }
                    Text(desc, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Max frekans kilitleme
        if (cpu.availableFreqs.isNotEmpty()) {
            item {
                SectionCard("Frekans Kilitleme", Icons.Default.Lock) {
                    var expanded by remember { mutableStateOf(false) }
                    var expandedMin by remember { mutableStateOf(false) }

                    Text("Max Frekans", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(4.dp))
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(cpu.maxFreqMhz)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            cpu.availableFreqs.reversed().forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq) },
                                    onClick = {
                                        val khz = freq.replace(" MHz", "").trim().toLongOrNull()?.times(1000)?.toString() ?: ""
                                        if (khz.isNotEmpty()) vm.setCpuMaxFreq(khz)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Min Frekans", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(4.dp))
                    Box {
                        OutlinedButton(onClick = { expandedMin = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(cpu.minFreqMhz)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedMin, onDismissRequest = { expandedMin = false }) {
                            cpu.availableFreqs.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq) },
                                    onClick = {
                                        val khz = freq.replace(" MHz", "").trim().toLongOrNull()?.times(1000)?.toString() ?: ""
                                        if (khz.isNotEmpty()) vm.setCpuMinFreq(khz)
                                        expandedMin = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sched tunables
        if (cpu.schedTunables.isNotEmpty()) {
            item {
                SectionCard("Kernel Scheduler", Icons.Default.Settings) {
                    cpu.schedTunables.forEach { (k, v) -> InfoRow(k, v) }
                }
            }
        }

        // GPU
        gpu?.let { g ->
            item {
                SectionCard("GPU - Mali G615-MC6", Icons.Default.Videocam) {
                    InfoRow("Governor", g.governor)
                    InfoRow("Mevcut Frekans", g.curFreqMhz)
                    InfoRow("Max Frekans", g.maxFreqMhz)
                    InfoRow("Min Frekans", g.minFreqMhz)
                    InfoRow("Kullanım", g.utilization)
                    if (g.availableGovernors.isNotEmpty()) {
                        SectionDivider()
                        ChipGroup(
                            label = "GPU Governor",
                            items = g.availableGovernors,
                            selected = g.governor,
                            onSelect = { vm.setGpuGovernor(it) }
                        )
                    }
                }
            }
        }
    }
}
