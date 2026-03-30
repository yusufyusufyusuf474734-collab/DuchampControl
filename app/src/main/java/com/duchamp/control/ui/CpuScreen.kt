package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.duchamp.control.CoreInfo
import com.duchamp.control.MainViewModel

@Composable
fun CpuScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val cpu = state.cpuInfo ?: return
    val gpu = state.gpuInfo

    val littleColor = Color(0xFF10B981)
    val bigColor    = MaterialTheme.colorScheme.primary
    val primeColor  = Color(0xFFF59E0B)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { KernelKitModuleCard(state, vm) }

        // Cluster özet kartları
        item {
            SectionCard("MT6897 Cluster Yapısı", Icons.Default.Memory) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClusterSummaryCard(cpu.clusterLittle, littleColor, Modifier.weight(1f))
                    ClusterSummaryCard(cpu.clusterBig,    bigColor,    Modifier.weight(1f))
                    ClusterSummaryCard(cpu.clusterPrime,  primeColor,  Modifier.weight(1f))
                }
            }
        }

        // Çekirdek grid — tıklanabilir, online/offline toggle
        item {
            SectionCard("Çekirdek Kontrolü", Icons.Default.Speed,
                badge = "${cpu.cores.count { it.online }}/${cpu.cores.size} Aktif") {
                Text("Çekirdeğe tıklayarak online/offline yapabilirsiniz. CPU0 her zaman aktif kalır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                cpu.cores.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { core ->
                            val clColor = when (core.cluster) {
                                "Little" -> littleColor
                                "Big"    -> bigColor
                                "Prime"  -> primeColor
                                else     -> MaterialTheme.colorScheme.primary
                            }
                            CoreControlCard(
                                core = core,
                                color = clColor,
                                modifier = Modifier.weight(1f),
                                onToggle = { if (core.id != 0) vm.setCpuCoreOnline(core.id, it) }
                            )
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Cluster Max Frekans
        item {
            SectionCard("Max Frekans Kontrolü", Icons.Default.KeyboardArrowUp) {
                Text("Her cluster için bağımsız maksimum frekans ayarı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                ClusterFreqRow(
                    label = "Little (cpu0-3) · A510",
                    color = littleColor,
                    currentFreq = cpu.clusterLittle.maxFreqMhz,
                    presets = listOf(
                        "768000" to "768M", "1100000" to "1.1G",
                        "1600000" to "1.6G", "2000000" to "2.0G", "2200000" to "2.2G ★"
                    ),
                    onSelect = { khz -> for (i in 0..3) vm.setCpuMaxFreqSingle(i, khz) }
                )
                SectionDivider()
                ClusterFreqRow(
                    label = "Big (cpu4-6) · A715",
                    color = bigColor,
                    currentFreq = cpu.clusterBig.maxFreqMhz,
                    presets = listOf(
                        "1100000" to "1.1G", "2000000" to "2.0G",
                        "2600000" to "2.6G", "3000000" to "3.0G", "3200000" to "3.2G ★"
                    ),
                    onSelect = { khz -> for (i in 4..6) vm.setCpuMaxFreqSingle(i, khz) }
                )
                SectionDivider()
                ClusterFreqRow(
                    label = "Prime (cpu7) · A715",
                    color = primeColor,
                    currentFreq = cpu.clusterPrime.maxFreqMhz,
                    presets = listOf(
                        "1100000" to "1.1G", "2000000" to "2.0G",
                        "2600000" to "2.6G", "3000000" to "3.0G",
                        "3200000" to "3.2G", "3350000" to "3.35G ★"
                    ),
                    onSelect = { khz -> vm.setCpuMaxFreqSingle(7, khz) }
                )
            }
        }

        // Cluster Min Frekans
        item {
            SectionCard("Min Frekans Kontrolü", Icons.Default.KeyboardArrowDown) {
                Text("Minimum frekans düşürülürse pil tasarrufu artar, yükseltilirse tepki hızı iyileşir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                ClusterFreqRow(
                    label = "Little (cpu0-3) · A510",
                    color = littleColor,
                    currentFreq = cpu.clusterLittle.minFreqMhz,
                    presets = listOf(
                        "300000" to "300M", "500000" to "500M",
                        "768000" to "768M", "1100000" to "1.1G"
                    ),
                    onSelect = { khz -> vm.setCpuMinFreqCluster("little", khz) }
                )
                SectionDivider()
                ClusterFreqRow(
                    label = "Big (cpu4-6) · A715",
                    color = bigColor,
                    currentFreq = cpu.clusterBig.minFreqMhz,
                    presets = listOf(
                        "300000" to "300M", "500000" to "500M",
                        "1100000" to "1.1G", "1600000" to "1.6G"
                    ),
                    onSelect = { khz -> vm.setCpuMinFreqCluster("big", khz) }
                )
                SectionDivider()
                ClusterFreqRow(
                    label = "Prime (cpu7) · A715",
                    color = primeColor,
                    currentFreq = cpu.clusterPrime.minFreqMhz,
                    presets = listOf(
                        "300000" to "300M", "500000" to "500M",
                        "1100000" to "1.1G", "1600000" to "1.6G"
                    ),
                    onSelect = { khz -> vm.setCpuMinFreqCluster("prime", khz) }
                )
            }
        }

        // Governor
        if (cpu.availableGovernors.isNotEmpty()) {
            item {
                SectionCard("CPU Governor", Icons.Default.Tune) {
                    ChipGroup(
                        label = "Tüm cluster'lara uygulanır",
                        items = cpu.availableGovernors,
                        selected = cpu.governor,
                        onSelect = { vm.setCpuGovernor(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                    val desc = when (cpu.governor) {
                        "performance" -> "Maksimum performans, yüksek güç tüketimi"
                        "powersave"   -> "Minimum frekans, maksimum pil tasarrufu"
                        "schedutil"   -> "Kernel scheduler tabanlı, dengeli (önerilen)"
                        "interactive" -> "Etkileşim odaklı, hızlı tepki"
                        "ondemand"    -> "Yüke göre dinamik frekans"
                        else          -> ""
                    }
                    if (desc.isNotEmpty()) {
                        StatusBadge(desc, MaterialTheme.colorScheme.primary)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard("Frekans", g.curFreqMhz, Icons.Default.Speed,
                            Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        MetricCard("Kullanım", g.utilization, Icons.Default.BarChart,
                            MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Max Frekans", g.maxFreqMhz)
                    InfoRow("Min Frekans", g.minFreqMhz)
                    if (g.availableGovernors.isNotEmpty()) {
                        SectionDivider()
                        ChipGroup("GPU Governor", g.availableGovernors, g.governor) {
                            vm.setGpuGovernor(it)
                        }
                    }
                    SectionDivider()
                    // GPU Max Frekans preset
                    ClusterFreqRow(
                        label = "GPU Max Frekans",
                        color = Color(0xFFF59E0B),
                        currentFreq = g.maxFreqMhz,
                        presets = listOf(
                            "400000000" to "400M", "700000000" to "700M",
                            "1000000000" to "1.0G", "1200000000" to "1.2G",
                            "1400000000" to "1.4G ★"
                        ),
                        onSelect = { hz -> vm.setGpuMaxFreq(hz) }
                    )
                    Spacer(Modifier.height(8.dp))
                    ClusterFreqRow(
                        label = "GPU Min Frekans",
                        color = Color(0xFFF59E0B).copy(alpha = 0.7f),
                        currentFreq = g.minFreqMhz,
                        presets = listOf(
                            "100000000" to "100M", "200000000" to "200M",
                            "400000000" to "400M", "700000000" to "700M"
                        ),
                        onSelect = { hz -> vm.setGpuMinFreq(hz) }
                    )
                }
            }
        }
    }
}

// ── Bileşenler ────────────────────────────────────────────────────────────────

@Composable
fun CoreControlCard(
    core: CoreInfo,
    color: Color,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {
    val canToggle = core.id != 0
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (core.online) color.copy(alpha = 0.12f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = { if (canToggle) onToggle(!core.online) }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("CPU${core.id}",
                style = MaterialTheme.typography.labelMedium,
                color = if (core.online) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(2.dp))
            Text(
                if (core.online) core.freqMhz.replace(" MHz", "") else "OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (core.online) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Text(core.cluster,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            if (!canToggle) {
                Text("★", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun ClusterSummaryCard(cluster: ClusterInfo, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cluster.name,
                style = MaterialTheme.typography.labelLarge, color = color)
            Text(cluster.coreType,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(cluster.coreRange,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            SectionDivider()
            Text(cluster.curFreqMhz,
                style = MaterialTheme.typography.bodySmall, color = color)
            Text("max ${cluster.maxFreqMhz}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ClusterFreqRow(
    label: String,
    color: Color,
    currentFreq: String,
    presets: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                modifier = Modifier.weight(1f))
            StatusBadge(currentFreq, color)
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presets.size) { i ->
                val (khz, lbl) = presets[i]
                val mhzVal = khz.toLongOrNull()?.let {
                    if (it > 1_000_000L) "${it / 1_000_000} MHz" else "${it / 1000} MHz"
                } ?: lbl
                FilterChip(
                    selected = currentFreq.replace(" MHz", "000") == khz ||
                               currentFreq == mhzVal ||
                               currentFreq.replace(" MHz", "") == khz.dropLast(3),
                    onClick = { onSelect(khz) },
                    label = { Text(lbl, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
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
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (enabled) Icons.Default.CheckCircle else Icons.Default.Extension,
                            null,
                            tint = if (enabled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("KernelKit FreqUnlock v0.1",
                        style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (enabled) "Aktif — CPU/GPU tam frekans kilidi açık"
                        else if (installed) "Kurulu, aktif değil"
                        else "CPU/GPU frekans kısıtlamasını kaldırır",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (enabled) StatusBadge("Aktif", MaterialTheme.colorScheme.tertiary)
            }

            if (!installed) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ) {
                    Row(modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kurulum sonrası cihazı yeniden başlatın.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!installed) {
                    Button(onClick = { vm.installKernelKit() }, modifier = Modifier.weight(1f)) {
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
