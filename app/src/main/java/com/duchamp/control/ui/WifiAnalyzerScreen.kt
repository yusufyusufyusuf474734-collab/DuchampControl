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
import com.duchamp.control.WifiNetwork

@Composable
fun WifiAnalyzerScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var bandFilter by remember { mutableStateOf("Tümü") }

    LaunchedEffect(Unit) { vm.scanWifiNetworks() }

    val filtered = state.wifiNetworks.filter { net ->
        when (bandFilter) {
            "2.4 GHz" -> net.frequency in 2400..2500
            "5 GHz"   -> net.frequency in 5000..5900
            else      -> true
        }
    }.sortedByDescending { it.level }

    // Kanal yoğunluğu
    val channelDensity = filtered.groupBy { it.channel }
        .mapValues { it.value.size }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tümü", "2.4 GHz", "5 GHz").forEach { b ->
                        FilterChip(selected = bandFilter == b, onClick = { bandFilter = b },
                            label = { Text(b, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                if (state.wifiScanRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { vm.scanWifiNetworks() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Kanal yoğunluğu özeti
        if (channelDensity.isNotEmpty()) {
            item {
                SectionCard("Kanal Yoğunluğu", Icons.Default.BarChart,
                    badge = "${filtered.size} ağ") {
                    val maxDensity = channelDensity.values.maxOrNull() ?: 1
                    channelDensity.entries.sortedBy { it.key }.forEach { (ch, count) ->
                        val pct = count.toFloat() / maxDensity
                        val color = when {
                            pct > 0.7f -> MaterialTheme.colorScheme.error
                            pct > 0.4f -> Color(0xFFF59E0B)
                            else       -> MaterialTheme.colorScheme.tertiary
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ch $ch",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                UsageBar("", pct, color, height = 8.dp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("$count ağ",
                                style = MaterialTheme.typography.labelSmall,
                                color = color)
                        }
                    }
                }
            }
        }

        // Ağ listesi
        if (filtered.isEmpty()) {
            item {
                EmptyState(Icons.Default.WifiOff, "Ağ bulunamadı",
                    "Tarama için yenile butonuna tıklayın")
            }
        } else {
            items(filtered, key = { it.bssid }) { net ->
                WifiNetworkItem(net)
            }
        }
    }
}

@Composable
fun WifiNetworkItem(net: WifiNetwork) {
    val signalPct = ((net.level + 100) / 100f).coerceIn(0f, 1f)
    val signalColor = when {
        net.level > -50 -> MaterialTheme.colorScheme.tertiary
        net.level > -70 -> Color(0xFFF59E0B)
        else            -> MaterialTheme.colorScheme.error
    }
    val band = if (net.frequency > 4000) "5 GHz" else "2.4 GHz"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = signalColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Wifi, null, tint = signalColor,
                        modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(net.ssid.ifBlank { "<Gizli>" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text("${net.bssid}  ·  Ch ${net.channel}  ·  $band  ·  ${net.security}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                UsageBar("", signalPct, signalColor, height = 3.dp)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${net.level} dBm",
                    style = MaterialTheme.typography.labelMedium, color = signalColor)
                Text("${net.frequency} MHz",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
