package com.duchamp.control.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.StressLogEntry

@Composable
fun StressTestScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var duration by remember { mutableIntStateOf(60) }
    val isRunning = isRunning

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Kontrol kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isRunning)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Whatshot, null,
                                    tint = if (isRunning) MaterialTheme.colorScheme.error
                                           else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CPU/GPU Stres Testi",
                                style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (isRunning) "Test çalışıyor — sıcaklık izleniyor"
                                else "Maksimum yük altında sıcaklık/frekans takibi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isRunning) {
                            StatusBadge("● Aktif", MaterialTheme.colorScheme.error)
                        }
                    }

                    if (!isRunning) {
                        Spacer(Modifier.height(12.dp))
                        Text("Test Süresi",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(30 to "30s", 60 to "1dk", 120 to "2dk", 300 to "5dk").forEach { (s, l) ->
                                FilterChip(selected = duration == s, onClick = { duration = s },
                                    label = { Text(l, style = MaterialTheme.typography.labelSmall) })
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { vm.startStressTest(duration) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Whatshot, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Stres Testi Başlat ($duration saniye)")
                        }
                    } else {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.stopStressTest() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Durdur")
                        }
                    }
                }
            }
        }

        // Anlık değerler
        if (isRunning || state.stressTestLog.isNotEmpty()) {
            item {
                val last = state.stressTestLog.lastOrNull()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard("CPU Sıcaklık",
                        last?.let { "${it.tempC.toInt()}°C" } ?: "—",
                        Icons.Default.Thermostat,
                        when {
                            (last?.tempC ?: 0f) > 80f -> MaterialTheme.colorScheme.error
                            (last?.tempC ?: 0f) > 65f -> Color(0xFFF59E0B)
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.weight(1f))
                    MetricCard("CPU Frekans",
                        last?.cpuFreqMhz ?: "—",
                        Icons.Default.Memory,
                        MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f))
                    MetricCard("GPU Frekans",
                        last?.gpuFreqMhz ?: "—",
                        Icons.Default.Videocam,
                        Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f))
                }
            }
        }

        // Sıcaklık/Frekans grafiği
        if (state.stressTestLog.size >= 2) {
            item {
                SectionCard("Sıcaklık & Frekans Grafiği", Icons.Default.ShowChart) {
                    val tempColor = MaterialTheme.colorScheme.error
                    val freqColor = MaterialTheme.colorScheme.primary
                    val onSurface = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        val w = size.width
                        val h = size.height
                        val pts = state.stressTestLog.size
                        val maxTemp = state.stressTestLog.maxOf { it.tempC }.coerceAtLeast(50f)
                        val minTemp = state.stressTestLog.minOf { it.tempC }.coerceAtMost(maxTemp - 10f)

                        // Grid
                        listOf(0.25f, 0.5f, 0.75f).forEach { pct ->
                            drawLine(onSurface, Offset(0f, h * pct), Offset(w, h * pct), 1f)
                        }

                        // Sıcaklık çizgisi
                        val tempPath = Path()
                        state.stressTestLog.forEachIndexed { i, entry ->
                            val x = w * i / (pts - 1)
                            val y = h * (1f - (entry.tempC - minTemp) / (maxTemp - minTemp))
                            if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                        }
                        drawPath(tempPath, tempColor, style = Stroke(2.5f))
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(10.dp, 3.dp)) {}
                            Spacer(Modifier.width(4.dp))
                            Text("Sıcaklık", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(10.dp, 3.dp)) {}
                            Spacer(Modifier.width(4.dp))
                            Text("CPU Frekans", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Log tablosu
        if (state.stressTestLog.isNotEmpty()) {
            item {
                SectionCard("Test Logu", Icons.Default.List,
                    badge = "${state.stressTestLog.size} kayıt") {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Süre", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Sıcaklık", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("CPU", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                    state.stressTestLog.takeLast(10).reversed().forEach { entry ->
                        val elapsed = (entry.timeMs - state.stressTestLog.first().timeMs) / 1000
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text("${elapsed}s", style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f))
                            Text("${entry.tempC.toInt()}°C",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.tempC > 80f) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f))
                            Text(entry.cpuFreqMhz, style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
