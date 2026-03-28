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
import com.duchamp.control.LiveMetric
import com.duchamp.control.MainViewModel

@Composable
fun LiveMonitorScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { vm.startLiveMonitoring() }
    DisposableEffect(Unit) { onDispose { /* izlemeyi durdurmuyoruz, kullanıcı karar versin */ } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Kontrol butonu
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Canlı İzleme", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.liveMonitoringActive) "Her 2 saniyede güncelleniyor"
                            else "Durduruldu",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.liveMonitoringActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = state.liveMonitoringActive,
                        onCheckedChange = { if (it) vm.startLiveMonitoring() else vm.stopLiveMonitoring() }
                    )
                }
            }
        }

        // Anlık değerler
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val lastCpu = state.cpuHistory.lastOrNull()?.value ?: 0f
                val lastGpu = state.gpuHistory.lastOrNull()?.value ?: 0f
                val lastRam = state.ramHistory.lastOrNull()?.value ?: 0f
                val lastTemp = state.cpuTempHistory.lastOrNull()?.value ?: 0f

                StatChip("CPU", "${lastCpu.toInt()}%", MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
                StatChip("GPU", "${lastGpu.toInt()}%", MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f))
                StatChip("RAM", "${lastRam.toInt()}%", MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f))
                StatChip("Sıcaklık", "${lastTemp.toInt()}°C",
                    if (lastTemp > 70) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
            }
        }

        // CPU Grafiği
        item {
            MetricChart(
                title = "CPU Kullanımı",
                data = state.cpuHistory,
                color = MaterialTheme.colorScheme.primary,
                unit = "%",
                maxValue = 100f
            )
        }

        // GPU Grafiği
        item {
            MetricChart(
                title = "GPU Kullanımı",
                data = state.gpuHistory,
                color = MaterialTheme.colorScheme.secondary,
                unit = "%",
                maxValue = 100f
            )
        }

        // RAM Grafiği
        item {
            MetricChart(
                title = "RAM Kullanımı",
                data = state.ramHistory,
                color = MaterialTheme.colorScheme.tertiary,
                unit = "%",
                maxValue = 100f
            )
        }

        // CPU Sıcaklık Grafiği
        item {
            MetricChart(
                title = "CPU Sıcaklığı",
                data = state.cpuTempHistory,
                color = Color(0xFFFF7043),
                unit = "°C",
                maxValue = 100f,
                dangerThreshold = 70f
            )
        }

        // Batarya Grafiği
        item {
            MetricChart(
                title = "Batarya",
                data = state.batteryHistory,
                color = Color(0xFF66BB6A),
                unit = "%",
                maxValue = 100f
            )
        }
    }
}

@Composable
fun MetricChart(
    title: String,
    data: List<LiveMetric>,
    color: Color,
    unit: String,
    maxValue: Float = 100f,
    dangerThreshold: Float? = null,
    modifier: Modifier = Modifier
) {
    val lastVal = data.lastOrNull()?.value ?: 0f
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val errorColor = MaterialTheme.colorScheme.error

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${lastVal.toInt()}$unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (dangerThreshold != null && lastVal > dangerThreshold) errorColor else color
                )
            }
            Spacer(Modifier.height(8.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Veri bekleniyor...", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                Canvas(
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val pts = data.size

                    // Grid çizgileri
                    listOf(0.25f, 0.5f, 0.75f).forEach { pct ->
                        drawLine(
                            color = onSurface,
                            start = Offset(0f, h * (1f - pct)),
                            end = Offset(w, h * (1f - pct)),
                            strokeWidth = 1f
                        )
                    }

                    // Danger threshold
                    dangerThreshold?.let { thr ->
                        val y = h * (1f - thr / maxValue)
                        drawLine(
                            color = errorColor.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.5f
                        )
                    }

                    if (pts < 2) return@Canvas

                    // Dolgu alanı
                    val fillPath = Path()
                    data.forEachIndexed { i, metric ->
                        val x = w * i / (pts - 1)
                        val y = h * (1f - (metric.value / maxValue).coerceIn(0f, 1f))
                        if (i == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
                    }
                    fillPath.lineTo(w, h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()
                    drawPath(fillPath, color = color.copy(alpha = 0.15f))

                    // Çizgi
                    val linePath = Path()
                    data.forEachIndexed { i, metric ->
                        val x = w * i / (pts - 1)
                        val y = h * (1f - (metric.value / maxValue).coerceIn(0f, 1f))
                        if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                    }
                    drawPath(linePath, color = color, style = Stroke(width = 2.5f))

                    // Son nokta
                    val lastX = w
                    val lastY = h * (1f - (lastVal / maxValue).coerceIn(0f, 1f))
                    drawCircle(color = color, radius = 4f, center = Offset(lastX, lastY))
                }
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}
