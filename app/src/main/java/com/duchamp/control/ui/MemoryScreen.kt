package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        // RAM
        item {
            SectionCard("RAM - LPDDR5X", Icons.Default.Memory) {
                val usedPct = if (mem.totalMb > 0) mem.usedMb.toFloat() / mem.totalMb.toFloat() else 0f
                UsageBar("Kullanım", usedPct)
                Spacer(Modifier.height(8.dp))
                InfoRow("Toplam", "${mem.totalMb} MB")
                InfoRow("Kullanılan", "${mem.usedMb} MB")
                InfoRow("Boş", "${mem.availMb} MB")
            }
        }

        // Swappiness
        item {
            SectionCard("Swappiness", Icons.Default.SwapVert) {
                var swapVal by remember {
                    mutableFloatStateOf(mem.swappiness.toFloatOrNull() ?: 60f)
                }
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

        // ZRAM
        item {
            SectionCard("ZRAM", Icons.Default.Compress) {
                InfoRow("Boyut", mem.zramSizeGb)
                InfoRow("Kullanılan", mem.zramUsedMb)
                InfoRow("Algoritma", mem.zramAlgo)
                SectionDivider()
                ChipGroup(
                    label = "Sıkıştırma Algoritması",
                    items = listOf("lz4", "lzo", "zstd", "lzo-rle"),
                    selected = mem.zramAlgo.trim('[', ']'),
                    onSelect = { vm.setZramAlgo(it) }
                )
                Spacer(Modifier.height(4.dp))
                Text("Not: Algoritma değişikliği swap'ı sıfırlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
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
                SectionCard("Depolama Bölümleri", Icons.Default.SdStorage) {
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
