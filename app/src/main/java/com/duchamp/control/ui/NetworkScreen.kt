package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun NetworkScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val net = state.networkInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wi-Fi durumu
        item {
            SectionCard("Wi-Fi Durumu", Icons.Default.Wifi) {
                InfoRow("IP Adresi", net?.wifiIp ?: "N/A")
                InfoRow("Sinyal", net?.wifiSignal ?: "N/A")
                InfoRow("Arayüz", net?.wifiInterface ?: "N/A")
                InfoRow("Standart", "802.11ax (Wi-Fi 6)")
                InfoRow("Özellikler", "Direct, Passpoint, P2P, NAN")
            }
        }

        // Wi-Fi güç tasarrufu
        item {
            SectionCard("Wi-Fi Güç Tasarrufu", Icons.Default.BatteryChargingFull) {
                Text("Güç tasarrufu modu Wi-Fi gecikmesini artırabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                ControlRow(
                    label = "Wi-Fi Güç Tasarrufu",
                    description = "Pil tasarrufu için Wi-Fi gücünü azalt",
                    checked = net?.wifiPowerSave == true,
                    onCheckedChange = { vm.setWifiPowerSave(it) }
                )
            }
        }

        // TCP Congestion
        item {
            SectionCard("TCP Congestion Algoritması", Icons.Default.NetworkCheck) {
                InfoRow("Mevcut", net?.tcpCongestion ?: "N/A")
                Spacer(Modifier.height(8.dp))
                if (!net?.availableCongestion.isNullOrEmpty()) {
                    ChipGroup(
                        label = "Algoritma Seç",
                        items = net!!.availableCongestion,
                        selected = net.tcpCongestion,
                        onSelect = { vm.setTcpCongestion(it) }
                    )
                    Spacer(Modifier.height(4.dp))
                    val desc = when (net.tcpCongestion) {
                        "bbr"   -> "Google BBR: Yüksek bant genişliği, düşük gecikme"
                        "cubic" -> "CUBIC: Varsayılan Linux, dengeli"
                        "reno"  -> "Reno: Klasik, düşük kayıp ağlar için"
                        else    -> ""
                    }
                    if (desc.isNotEmpty()) {
                        Text(desc, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // DNS
        item {
            SectionCard("DNS Sunucuları", Icons.Default.Dns) {
                var dns1 by remember { mutableStateOf(net?.dnsServers?.getOrNull(0) ?: "8.8.8.8") }
                var dns2 by remember { mutableStateOf(net?.dnsServers?.getOrNull(1) ?: "8.8.4.4") }

                if (!net?.dnsServers.isNullOrEmpty()) {
                    InfoRow("Mevcut DNS 1", net!!.dnsServers.getOrElse(0) { "N/A" })
                    InfoRow("Mevcut DNS 2", net.dnsServers.getOrElse(1) { "N/A" })
                    SectionDivider()
                }

                Text("Hızlı DNS Seçimi", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                val presets = listOf(
                    "Google" to ("8.8.8.8" to "8.8.4.4"),
                    "Cloudflare" to ("1.1.1.1" to "1.0.0.1"),
                    "Quad9" to ("9.9.9.9" to "149.112.112.112"),
                    "AdGuard" to ("94.140.14.14" to "94.140.15.15")
                )
                presets.forEach { (name, pair) ->
                    OutlinedButton(
                        onClick = {
                            dns1 = pair.first; dns2 = pair.second
                            vm.setDns(pair.first, pair.second)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$name (${pair.first})")
                    }
                }

                SectionDivider()
                Text("Manuel DNS", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = dns1, onValueChange = { dns1 = it },
                    label = { Text("DNS 1") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = dns2, onValueChange = { dns2 = it },
                    label = { Text("DNS 2") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.setDns(dns1, dns2) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DNS Uygula")
                }
            }
        }
    }
}
