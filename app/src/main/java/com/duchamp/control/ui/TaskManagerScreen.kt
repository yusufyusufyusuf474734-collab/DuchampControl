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
import com.duchamp.control.ProcessInfo

@Composable
fun TaskManagerScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var sortBy by remember { mutableStateOf("ram") }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadProcessList() }

    val sorted = state.processList
        .filter { search.isBlank() || it.name.contains(search, ignoreCase = true) }
        .let { list ->
            when (sortBy) {
                "ram" -> list.sortedByDescending { it.ramMb }
                "cpu" -> list.sortedByDescending { it.cpuPct }
                "pid" -> list.sortedBy { it.pid }
                else  -> list
            }
        }

    val totalRam = state.memInfo?.totalMb?.toFloat() ?: 1f
    val usedByProcs = sorted.sumOf { it.ramMb.toDouble() }.toFloat()

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Özet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard("Toplam RAM", "${state.memInfo?.totalMb ?: 0} MB",
                    Icons.Default.Memory, MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
                MetricCard("Kullanılan", "${state.memInfo?.usedMb ?: 0} MB",
                    Icons.Default.Storage, MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f))
                MetricCard("Process", "${sorted.size}",
                    Icons.Default.List, MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))

            // RAM temizle
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
            Spacer(Modifier.height(8.dp))

            // Arama + sıralama
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Process ara...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ram" to "RAM", "cpu" to "CPU", "pid" to "PID").forEach { (k, l) ->
                        FilterChip(selected = sortBy == k, onClick = { sortBy = k },
                            label = { Text(l, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                if (state.processLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { vm.loadProcessList() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sorted, key = { it.pid }) { proc ->
                ProcessItem(proc = proc, totalRamMb = totalRam,
                    onKill = { vm.killProcess(proc.pid, proc.name) })
            }
        }
    }
}

@Composable
fun ProcessItem(proc: ProcessInfo, totalRamMb: Float, onKill: () -> Unit) {
    val ramPct = if (totalRamMb > 0) proc.ramMb / totalRamMb else 0f
    val ramColor = when {
        ramPct > 0.1f -> MaterialTheme.colorScheme.error
        ramPct > 0.05f -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(proc.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1)
                Text("PID: ${proc.pid}  ·  ${proc.user}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                UsageBar("", ramPct, ramColor, height = 3.dp)
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${proc.ramMb.toInt()} MB",
                    style = MaterialTheme.typography.labelMedium, color = ramColor)
                Text("CPU ${proc.cpuPct.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onKill, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
