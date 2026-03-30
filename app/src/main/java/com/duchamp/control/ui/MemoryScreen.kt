package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun MemoryScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val mem = state.memInfo ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // RAM temizleme
        item {
            SectionCard("RAM Yönetimi", Icons.Default.CleaningServices) {
                val usedPct = if (mem.totalMb > 0) mem.usedMb.toFloat() / mem.totalMb.toFloat() else 0f
                UsageBar("Kullanım", usedPct,
                    color = when {
                        usedPct > 0.85f -> MaterialTheme.colorScheme.error
                        usedPct > 0.7f  -> Color(0xFFF59E0B)
                        else            -> MaterialTheme.colorScheme.primary
                    })
                Spacer(Modifier.height(10.dp))
                InfoRow("Toplam", "${mem.totalMb} MB")
                InfoRow("Kullanılan", "${mem.usedMb} MB")
                InfoRow("Boş", "${mem.availMb} MB")
                SectionDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.clearRamCache() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cache Temizle")
                    }
                    OutlinedButton(
                        onClick = { vm.dropCaches() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Drop Caches")
                    }
                }
            }
        }

        // Swap yönetimi
        item {
            SectionCard("Swap & ZRAM Yönetimi", Icons.Default.SwapVert) {
                InfoRow("ZRAM Boyutu", mem.zramSizeGb)
                InfoRow("ZRAM Kullanılan", mem.zramUsedMb)
                InfoRow("Algoritma", mem.zramAlgo)
                SectionDivider()
                Text("ZRAM Boyutu Ayarla",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1G" to "1 GB", "2G" to "2 GB", "3G" to "3 GB", "4G" to "4 GB").forEach { (size, label) ->
                        FilterChip(
                            selected = mem.zramSizeGb.contains(size.replace("G", "")),
                            onClick = { vm.setZramSize(size) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                SectionDivider()
                ChipGroup(
                    label = "Sıkıştırma Algoritması",
                    items = listOf("lz4", "lzo", "zstd", "lzo-rle"),
                    selected = mem.zramAlgo.trim('[', ']'),
                    onSelect = { vm.setZramAlgo(it) }
                )
                Spacer(Modifier.height(4.dp))
                Text("Not: Boyut/algoritma değişikliği swap'ı sıfırlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }

        // Swappiness
        item {
            SectionCard("Swappiness", Icons.Default.SwapVert) {
                var swapVal by remember { mutableFloatStateOf(mem.swappiness.toFloatOrNull() ?: 60f) }
                Text("Düşük değer RAM'i tercih eder, yüksek değer swap'ı daha çok kullanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                InfoRow("Mevcut Değer", mem.swappiness)
                SliderRow(
                    label = "Swappiness",
                    value = swapVal,
                    valueRange = 0f..200f,
                    displayValue = swapVal.toInt().toString(),
                    onValueChangeFinished = {
                        swapVal = it
                        vm.setSwappiness(it.toInt())
                    }
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 60, 100, 160, 200).forEach { v ->
                        FilterChip(
                            selected = swapVal.toInt() == v,
                            onClick = { swapVal = v.toFloat(); vm.setSwappiness(v) },
                            label = { Text("$v") }
                        )
                    }
                }
            }
        }

        // I/O Scheduler
        item {
            SectionCard("I/O Scheduler (UFS 4.0)", Icons.Default.Storage) {
                InfoRow("Mevcut", state.ioScheduler)
                Spacer(Modifier.height(8.dp))
                if (state.availableIoSchedulers.isNotEmpty()) {
                    ChipGroup(
                        label = "Scheduler Seç",
                        items = state.availableIoSchedulers,
                        selected = state.ioScheduler.replace(Regex("[\\[\\]]"), ""),
                        onSelect = { vm.setIoScheduler(it) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                val desc = when {
                    state.ioScheduler.contains("mq-deadline") -> "Düşük gecikme, SSD/UFS için ideal"
                    state.ioScheduler.contains("none") -> "Sıralama yok, NVMe/UFS için önerilir"
                    state.ioScheduler.contains("bfq") -> "Adil kuyruk, pil tasarrufu"
                    else -> ""
                }
                if (desc.isNotEmpty()) {
                    Text(desc, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Depolama
        if (state.storageInfo.isNotEmpty()) {
            item {
                SectionCard("Depolama Bölümleri", Icons.Default.Storage) {
                    state.storageInfo.forEachIndexed { i, s ->
                        if (i > 0) SectionDivider()
                        Text(s.partition, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        UsageBar("Kullanım", s.usePct)
                        Spacer(Modifier.height(4.dp))
                        InfoRow("Boyut", s.size)
                        InfoRow("Kullanılan", "${s.used} (${s.usePercent})")
                        InfoRow("Boş", s.avail)
                    }
                }
            }
        }
    }
}
