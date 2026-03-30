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
import com.duchamp.control.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

data class BenchmarkResult(
    val name: String,
    val score: Long,
    val unit: String,
    val color: Color
)

@Composable
fun BenchmarkScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var running by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<BenchmarkResult>>(emptyList()) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTest by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Speed, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Sistem Benchmark",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("CPU, RAM ve depolama hız testi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (running) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(currentTest,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                running = true
                                results = emptyList()
                                scope.launch {
                                    val r = mutableListOf<BenchmarkResult>()

                                    currentTest = "CPU Tek Çekirdek Testi..."
                                    progress = 0.1f
                                    val cpuSingle = withContext(Dispatchers.Default) {
                                        var ops = 0L
                                        val ms = measureTimeMillis {
                                            val end = System.currentTimeMillis() + 1000
                                            while (System.currentTimeMillis() < end) {
                                                var x = 1.0
                                                repeat(1000) { x = sqrt(x + it) }
                                                ops++
                                            }
                                        }
                                        ops * 1000 / ms
                                    }
                                    r.add(BenchmarkResult("CPU Tek Çekirdek", cpuSingle, "ops/s", Color(0xFF3B82F6)))
                                    progress = 0.3f

                                    currentTest = "CPU Çok Çekirdek Testi..."
                                    val cpuMulti = withContext(Dispatchers.Default) {
                                        val cores = Runtime.getRuntime().availableProcessors()
                                        var total = 0L
                                        val jobs = (0 until cores).map {
                                            kotlinx.coroutines.async(Dispatchers.Default) {
                                                var ops = 0L
                                                val end = System.currentTimeMillis() + 1000
                                                while (System.currentTimeMillis() < end) {
                                                    var x = 1.0
                                                    repeat(1000) { i -> x = sqrt(x + i) }
                                                    ops++
                                                }
                                                ops
                                            }
                                        }
                                        jobs.forEach { total += it.await() }
                                        total
                                    }
                                    r.add(BenchmarkResult("CPU Çok Çekirdek", cpuMulti, "ops/s", Color(0xFF8B5CF6)))
                                    progress = 0.55f

                                    currentTest = "RAM Bant Genişliği Testi..."
                                    val ramBw = withContext(Dispatchers.Default) {
                                        val size = 16 * 1024 * 1024
                                        val arr = ByteArray(size)
                                        var bytes = 0L
                                        val ms = measureTimeMillis {
                                            val end = System.currentTimeMillis() + 1000
                                            while (System.currentTimeMillis() < end) {
                                                arr.fill(1)
                                                bytes += size
                                            }
                                        }
                                        bytes / ms / 1024
                                    }
                                    r.add(BenchmarkResult("RAM Bant Genişliği", ramBw, "MB/s", Color(0xFF10B981)))
                                    progress = 0.75f

                                    currentTest = "Depolama Okuma Testi..."
                                    val storageRead = withContext(Dispatchers.IO) {
                                        try {
                                            val file = java.io.File("/proc/version")
                                            var bytes = 0L
                                            val ms = measureTimeMillis {
                                                val end = System.currentTimeMillis() + 500
                                                while (System.currentTimeMillis() < end) {
                                                    bytes += file.readBytes().size
                                                }
                                            }
                                            bytes / ms / 1024 * 2
                                        } catch (e: Exception) { 0L }
                                    }
                                    r.add(BenchmarkResult("Depolama Okuma", storageRead, "KB/s", Color(0xFFF59E0B)))
                                    progress = 0.9f

                                    currentTest = "Tamamlandı"
                                    progress = 1f
                                    results = r
                                    running = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Benchmark Başlat")
                        }
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            item {
                SectionCard("Sonuçlar", Icons.Default.Assessment) {
                    results.forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .then(Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = result.color,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(result.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f))
                            Text("${result.score} ${result.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                color = result.color)
                        }
                        if (result != results.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            item {
                // Cihaz bilgisi ile karşılaştırma
                SectionCard("Cihaz Bilgisi", Icons.Default.Info) {
                    InfoRow("SoC", "MediaTek Dimensity 8300 Ultra")
                    InfoRow("CPU", "4x A715 + 4x A510")
                    InfoRow("GPU", "Mali G615-MC6")
                    InfoRow("Çekirdek Sayısı", "${Runtime.getRuntime().availableProcessors()}")
                    state.memInfo?.let {
                        InfoRow("RAM", "${it.totalMb} MB")
                    }
                }
            }
        }

        // Sistem bilgisi
        item {
            SectionCard("Sistem Performans Bilgisi", Icons.Default.Analytics) {
                state.cpuInfo?.let { cpu ->
                    InfoRow("CPU Governor", cpu.governor)
                    InfoRow("Prime Frekans", cpu.clusterPrime.curFreqMhz)
                    InfoRow("Big Frekans", cpu.clusterBig.curFreqMhz)
                    InfoRow("Little Frekans", cpu.clusterLittle.curFreqMhz)
                }
                state.gpuInfo?.let { gpu ->
                    SectionDivider()
                    InfoRow("GPU Governor", gpu.governor)
                    InfoRow("GPU Frekans", gpu.curFreqMhz)
                    InfoRow("GPU Kullanım", gpu.utilization)
                }
                state.memInfo?.let { mem ->
                    SectionDivider()
                    InfoRow("Swappiness", mem.swappiness)
                    InfoRow("ZRAM", mem.zramSizeGb)
                    InfoRow("ZRAM Algo", mem.zramAlgo)
                }
            }
        }
    }
}
