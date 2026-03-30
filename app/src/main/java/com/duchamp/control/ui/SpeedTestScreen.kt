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

@Composable
fun SpeedTestScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ana test kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NetworkCheck, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    if (state.speedTestRunning) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Test çalışıyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Bu işlem 15-30 saniye sürebilir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        state.speedTestResult?.let { result ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SpeedGauge("İndirme", result.downloadMbps,
                                    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                SpeedGauge("Yükleme", result.uploadMbps,
                                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${result.pingMs} ms",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = when {
                                            result.pingMs < 20  -> MaterialTheme.colorScheme.tertiary
                                            result.pingMs < 50  -> Color(0xFFF59E0B)
                                            else                -> MaterialTheme.colorScheme.error
                                        })
                                    Text("Ping", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(result.server,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Sunucu", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } ?: run {
                            Text("Hız testini başlatmak için butona tıklayın",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { vm.runSpeedTest() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.speedTestRunning
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.speedTestResult != null) "Tekrar Test Et" else "Hız Testi Başlat")
                        }
                    }
                }
            }
        }

        // Ağ bilgisi
        item {
            SectionCard("Ağ Bilgisi", Icons.Default.Wifi) {
                state.networkInfo?.let { net ->
                    InfoRow("IP Adresi", net.wifiIp)
                    InfoRow("Sinyal", net.wifiSignal)
                    InfoRow("Arayüz", net.wifiInterface)
                    InfoRow("TCP Congestion", net.tcpCongestion)
                } ?: Text("Ağ bilgisi yüklenemedi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Ping sonuçları
        if (state.pingResults.isNotEmpty()) {
            item {
                SectionCard("DNS Ping Sonuçları", Icons.Default.Speed) {
                    state.pingResults.forEach { (label, result) ->
                        val isTimeout = result.contains("aşımı")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isTimeout) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                null, modifier = Modifier.size(14.dp),
                                tint = if (isTimeout) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                            StatusBadge(result,
                                if (isTimeout) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedGauge(label: String, mbps: Float, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (mbps >= 1000) "${"%.1f".format(mbps / 1000)} Gbps"
            else "${"%.1f".format(mbps)} Mbps",
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        val pct = (mbps / 1000f).coerceIn(0f, 1f)
        UsageBar("", pct, color, height = 6.dp)
    }
}
