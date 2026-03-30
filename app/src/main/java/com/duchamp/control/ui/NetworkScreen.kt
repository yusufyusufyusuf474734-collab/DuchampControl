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
import com.duchamp.control.SecurityManager

@Composable
fun NetworkScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val net = state.networkInfo

    LaunchedEffect(Unit) {
        vm.loadVpnInfo()
        vm.loadFirewallRules()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // VPN durumu
        item {
            val vpn = state.vpnInfo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (vpn?.active == true)
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (vpn?.active == true)
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VpnKey, null,
                                tint = if (vpn?.active == true) MaterialTheme.colorScheme.tertiary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VPN Durumu", style = MaterialTheme.typography.titleSmall)
                        if (vpn?.active == true) {
                            Text("${vpn.protocol} · ${vpn.interfaceName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (vpn.localIp.isNotEmpty()) {
                                Text(vpn.localIp, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary)
                            }
                        } else {
                            Text("VPN bağlantısı yok",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    StatusBadge(
                        if (vpn?.active == true) "Bağlı" else "Kapalı",
                        if (vpn?.active == true) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Wi-Fi durumu
        item {
            SectionCard("Wi-Fi", Icons.Default.Wifi) {
                InfoRow("IP Adresi", net?.wifiIp ?: "N/A")
                InfoRow("Sinyal", net?.wifiSignal ?: "N/A")
                InfoRow("Arayüz", net?.wifiInterface ?: "N/A")
                InfoRow("Standart", "802.11ax (Wi-Fi 6)")
                SectionDivider()
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
            SectionCard("TCP Congestion", Icons.Default.NetworkCheck) {
                InfoRow("Mevcut", net?.tcpCongestion ?: "N/A")
                Spacer(Modifier.height(8.dp))
                if (!net?.availableCongestion.isNullOrEmpty()) {
                    ChipGroup("Algoritma Seç", net!!.availableCongestion, net.tcpCongestion) {
                        vm.setTcpCongestion(it)
                    }
                    Spacer(Modifier.height(6.dp))
                    val desc = when (net?.tcpCongestion) {
                        "bbr"   -> "Google BBR: Yüksek bant genişliği, düşük gecikme"
                        "cubic" -> "CUBIC: Varsayılan Linux, dengeli"
                        "reno"  -> "Reno: Klasik, düşük kayıp ağlar için"
                        else    -> ""
                    }
                    if (desc.isNotEmpty()) StatusBadge(desc, MaterialTheme.colorScheme.primary)
                }
            }
        }

        // DNS
        item {
            SectionCard("DNS Sunucuları", Icons.Default.Dns) {
                var dns1 by remember { mutableStateOf(net?.dnsServers?.getOrNull(0) ?: "8.8.8.8") }
                var dns2 by remember { mutableStateOf(net?.dnsServers?.getOrNull(1) ?: "8.8.4.4") }

                net?.dnsServers?.let {
                    InfoRow("DNS 1", it.getOrElse(0) { "N/A" })
                    InfoRow("DNS 2", it.getOrElse(1) { "N/A" })
                    SectionDivider()
                }

                val presets = listOf(
                    "Google"     to ("8.8.8.8"       to "8.8.4.4"),
                    "Cloudflare" to ("1.1.1.1"        to "1.0.0.1"),
                    "Quad9"      to ("9.9.9.9"        to "149.112.112.112"),
                    "AdGuard"    to ("94.140.14.14"   to "94.140.15.15")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { (name, pair) ->
                        FilterChip(
                            selected = dns1 == pair.first,
                            onClick = { dns1 = pair.first; dns2 = pair.second; vm.setDns(pair.first, pair.second) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dns1, onValueChange = { dns1 = it },
                        label = { Text("DNS 1") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = dns2, onValueChange = { dns2 = it },
                        label = { Text("DNS 2") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.setDns(dns1, dns2) }, modifier = Modifier.fillMaxWidth()) {
                    Text("DNS Uygula")
                }
            }
        }

        // Firewall
        item {
            SectionCard("Firewall (iptables)", Icons.Default.Security,
                badge = "${state.firewallRules.size} kural") {
                Text("Uygulama bazlı ağ erişimini engelleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))

                if (state.firewallRules.isEmpty()) {
                    EmptyState(Icons.Default.Security, "Aktif firewall kuralı yok")
                } else {
                    state.firewallRules.forEach { rule ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Block, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.appLabel, style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (rule.blockWifi) StatusBadge("Wi-Fi", MaterialTheme.colorScheme.error)
                                    if (rule.blockData) StatusBadge("Data", MaterialTheme.colorScheme.error)
                                }
                            }
                            IconButton(onClick = { vm.removeFirewallRule(rule.id) },
                                modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { vm.loadFirewallRules() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kuralları Yenile")
                }
            }
        }

        // Hosts editörü
        item {
            HostsEditorCard(vm)
        }

        // Ping testi
        item {
            SectionCard("Ping Testi", Icons.Default.NetworkCheck,
                badge = if (state.pingResults.isNotEmpty()) "Tamamlandı" else null) {
                Text("Popüler DNS sunucularına gecikme ölçümü yapın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { vm.runPingTest() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.pingRunning
                ) {
                    if (state.pingRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Test Çalışıyor...")
                    } else {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ping Testi Başlat")
                    }
                }
                if (state.pingResults.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    state.pingResults.forEach { (label, result) ->
                        val isTimeout = result.contains("aşımı")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isTimeout) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isTimeout) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StatusBadge(result,
                                if (isTimeout) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun HostsEditorCard(vm: MainViewModel) {
    var domain by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("0.0.0.0") }
    var hostsContent by remember { mutableStateOf("") }
    var showHosts by remember { mutableStateOf(false) }
    var hostsLines by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(showHosts) {
        if (showHosts) {
            hostsContent = SecurityManager.getHostsFile()
            hostsLines = hostsContent.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        }
    }

    SectionCard("Hosts Dosyası Editörü", Icons.Default.Block,
        badge = if (hostsLines.isNotEmpty()) "${hostsLines.size} kural" else null) {
        Text("Hosts dosyasına kural ekleyerek reklam ve izleme sunucularını engelleyin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))

        // Hızlı preset'ler
        Text("Hızlı Engelleme", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        val adDomains = listOf(
            "ads.google.com", "doubleclick.net",
            "googleadservices.com", "analytics.google.com", "facebook.com"
        )
        adDomains.forEach { d ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Block, null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(d, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = { vm.addHostsEntry(d, "0.0.0.0") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) { Text("Engelle", style = MaterialTheme.typography.labelSmall) }
            }
        }

        SectionDivider()

        // Manuel ekleme
        Text("Manuel Kural", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = domain, onValueChange = { domain = it },
            label = { Text("Domain") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ip, onValueChange = { ip = it },
                label = { Text("IP") },
                modifier = Modifier.weight(1f), singleLine = true
            )
            Button(
                onClick = { if (domain.isNotBlank()) { vm.addHostsEntry(domain.trim(), ip.trim()); domain = "" } },
                enabled = domain.isNotBlank()
            ) { Text("Ekle") }
        }

        SectionDivider()

        // Mevcut kurallar
        TextButton(
            onClick = { showHosts = !showHosts },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(if (showHosts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (showHosts) "Kuralları Gizle" else "Mevcut Kuralları Göster")
        }

        if (showHosts && hostsLines.isNotEmpty()) {
            hostsLines.take(20).forEach { line ->
                val parts = line.trim().split(Regex("\\s+"))
                val lineIp = parts.getOrNull(0) ?: ""
                val lineDomain = parts.getOrNull(1) ?: ""
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(lineDomain,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f))
                    Text(lineIp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = { vm.removeHostsEntry(lineDomain) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp)
            }
        }
    }
}
