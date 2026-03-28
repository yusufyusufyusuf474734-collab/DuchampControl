package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.ClusterInfo
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
        // KernelKit FreqUnlock modül kartı
        item {
            KernelKitModuleCard(state, vm)
        }

        // MT6897 Cluster Özeti
        item {
            SectionCard("MT6897 Cluster Yapısı", Icons.Default.Memory) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClusterCard(cpu.clusterLittle, Color(0xFF4CAF50), Modifier.weight(1f))
                    ClusterCard(cpu.clusterBig, Color(0xFF2196F3), Modifier.weight(1f))
                    ClusterCard(cpu.clusterPrime, Color(0xFFFF5722), Modifier.weight(1f))
                }
            }
        }

        // Çekirdek grid
        item {
            SectionCard("Çekirdek Durumu", Icons.Default.Speed) {
                cpu.cores.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { core ->
                            val clusterColor = when (core.cluster) {
                                "Little" -> Color(0xFF4CAF50)
                                "Big"    -> Color(0xFF2196F3)
                                "Prime"  -> Color(0xFFFF5722)
                                else     -> MaterialTheme.colorScheme.primary
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (core.online)
                                        clusterColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("CPU${core.id}",
                                        style = MaterialTheme.typography.labelMedium)
                                    Text(core.freqMhz.replace(" MHz", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (core.online) clusterColor
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    Text(core.cluster,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Cluster bazlı frekans kilitleme
        item {
            SectionCard("Cluster Frekans Kontrolü", Icons.Default.Lock) {
                Text(
                    "Her cluster için bağımsız max frekans ayarı yapabilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))

                // Little cluster
                ClusterFreqControl(
                    label = "Little (cpu0-3) — Cortex-A510",
                    color = Color(0xFF4CAF50),
                    currentMax = cpu.clusterLittle.maxFreqMhz,
                    presets = listOf("768000" to "768 MHz", "1100000" to "1.1 GHz",
                        "1600000" to "1.6 GHz", "2000000" to "2.0 GHz", "2200000" to "2.2 GHz (Max)"),
                    onSelect = { khz ->
                        for (i in 0..3) vm.setCpuMaxFreqSingle(i, khz)
                    }
                )
                SectionDivider()

                // Big cluster
                ClusterFreqControl(
                    label = "Big (cpu4-6) — Cortex-A715",
                    color = Color(0xFF2196F3),
                    currentMax = cpu.clusterBig.maxFreqMhz,
                    presets = listOf("1100000" to "1.1 GHz", "2000000" to "2.0 GHz",
                        "2600000" to "2.6 GHz", "3000000" to "3.0 GHz", "3200000" to "3.2 GHz (Max)"),
                    onSelect = { khz ->
                        for (i in 4..6) vm.setCpuMaxFreqSingle(i, khz)
                    }
                )
                SectionDivider()

                // Prime cluster
                ClusterFreqControl(
                    label = "Prime (cpu7) — Cortex-A715",
                    color = Color(0xFFFF5722),
                    currentMax = cpu.clusterPrime.maxFreqMhz,
                    presets = listOf("1100000" to "1.1 GHz", "2000000" to "2.0 GHz",
                        "2600000" to "2.6 GHz", "3000000" to "3.0 GHz",
                        "3200000" to "3.2 GHz", "3350000" to "3.35 GHz (Max)"),
                    onSelect = { khz -> vm.setCpuMaxFreqSingle(7, khz) }
                )
            }
        }

        // Governor seçimi
        if (cpu.availableGovernors.isNotEmpty()) {
            item {
                SectionCard("CPU Governor", Icons.Default.Tune) {
                    Text("Tüm cluster'lara aynı governor uygulanır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(8.dp))
                    ChipGroup(
                        label = "Governor Seç",
                        items = cpu.availableGovernors,
                        selected = cpu.governor,
                        onSelect = { vm.setCpuGovernor(it) }
                    )
                    Spacer(Modifier.height(6.dp))
                    val desc = when (cpu.governor) {
                        "performance" -> "Maksimum performans, yüksek güç tüketimi"
                        "powersave"   -> "Minimum frekans, maksimum pil tasarrufu"
                        "schedutil"   -> "Kernel scheduler tabanlı, dengeli (önerilen)"
                        "interactive" -> "Etkileşim odaklı, hızlı tepki"
                        "ondemand"    -> "Yüke göre dinamik frekans"
                        else          -> ""
                    }
                    if (desc.isNotEmpty()) {
                        Text(desc, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
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
                SectionCard("GPU — Mali G615-MC6", Icons.Default.Videocam) {
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

@Composable
fun KernelKitModuleCard(state: AppState, vm: MainViewModel) {
    val installed = state.kernelKitInstalled
    val enabled   = state.kernelKitEnabled

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (enabled) Icons.Default.CheckCircle else Icons.Default.Extension,
                    null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "KernelKit FreqUnlock v0.1",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (enabled) "Aktif — CPU/GPU tam frekans kilidi açık"
                        else if (installed) "Kurulu ama aktif değil"
                        else "CPU/GPU frekans kısıtlamasını kaldırır",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Özellikler listesi
            if (!installed) {
                val features = listOf(
                    "Prime (cpu7) → 3350 MHz tam frekans",
                    "Big (cpu4-6) → 3200 MHz tam frekans",
                    "GPU → 1400 MHz tam frekans",
                    "MTK thermal kısıtlaması kaldırılır",
                    "Otomatik senaryo: Pil / Dengeli / Performans / Oyun",
                    "Geliştirici: Sinan Aslan"
                )
                features.forEach { f ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("•", color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp))
                        Text(f, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Uyarı
            if (!installed) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Modül kurulumu yeniden başlatma gerektirir. Kurulum sonrası cihazı yeniden başlatın.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Butonlar
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!installed) {
                    Button(
                        onClick = { vm.installKernelKit() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Modülü Kur")
                    }
                } else {
                    OutlinedButton(
                        onClick = { vm.uninstallKernelKit() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kaldır")
                    }
                }
                OutlinedButton(onClick = { vm.refreshKernelKitStatus() }) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ClusterCard(cluster: ClusterInfo, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cluster.name, style = MaterialTheme.typography.labelLarge, color = color)
            Text(cluster.coreType, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(cluster.coreRange, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            SectionDivider()
            Text(cluster.curFreqMhz, style = MaterialTheme.typography.bodySmall, color = color)
            Text("/ ${cluster.maxFreqMhz}", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ClusterFreqControl(
    label: String,
    color: Color,
    currentMax: String,
    presets: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp, 10.dp).let {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(10.dp)
                        .then(Modifier)
                )
                it
            })
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Spacer(Modifier.weight(1f))
            Text("Mevcut: $currentMax", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Spacer(Modifier.height(6.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(presets.size) { i ->
                val (khz, label2) = presets[i]
                val mhzVal = khz.toLongOrNull()?.let { "${it / 1000} MHz" } ?: label2
                FilterChip(
                    selected = currentMax.replace(" MHz", "000") == khz ||
                               currentMax == mhzVal,
                    onClick = { onSelect(khz) },
                    label = { Text(label2, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        selectedLabelColor = color
                    )
                )
            }
        }
    }
}
