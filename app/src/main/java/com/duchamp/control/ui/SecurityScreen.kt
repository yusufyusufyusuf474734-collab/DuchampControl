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
import com.duchamp.control.RootAccessLog
import com.duchamp.control.SecurityManager

@Composable
fun SecurityScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val sec = state.securityInfo

    LaunchedEffect(Unit) { if (sec == null) vm.loadSecurity() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Güvenlik özeti
        item {
            SectionCard("Güvenlik Durumu", Icons.Default.Shield) {
                if (sec == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Yükleniyor...")
                    }
                } else {
                    SecurityStatusRow("Bootloader", if (sec.bootloaderLocked) "Kilitli" else "Açık",
                        if (sec.bootloaderLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        if (sec.bootloaderLocked) Icons.Default.Lock else Icons.Default.LockOpen)

                    SecurityStatusRow("Verified Boot", sec.verifiedBootState,
                        when (sec.verifiedBootState) {
                            "green"  -> MaterialTheme.colorScheme.primary
                            "orange" -> Color(0xFFFF9800)
                            else     -> MaterialTheme.colorScheme.error
                        }, Icons.Default.Verified)

                    SecurityStatusRow("DM-Verity", sec.dmVerity,
                        MaterialTheme.colorScheme.onSurface, Icons.Default.Shield)

                    SecurityStatusRow("SELinux", sec.selinuxMode,
                        if (sec.selinuxMode == "Enforcing") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        Icons.Default.GppGood)

                    SecurityStatusRow("ADB", if (sec.adbEnabled) "Açık" else "Kapalı",
                        if (sec.adbEnabled) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                        Icons.Default.Cable)

                    SecurityStatusRow("Geliştirici Seçenekleri",
                        if (sec.developerOptions) "Açık" else "Kapalı",
                        if (sec.developerOptions) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                        Icons.Default.Code)

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { vm.loadSecurity() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Yenile")
                    }
                }
            }
        }

        // Root erişim logları
        item {
            SectionCard("Root Erişim Logları", Icons.Default.ManageAccounts) {
                if (sec?.rootAccessLogs.isNullOrEmpty()) {
                    Text("Log bulunamadı veya Magisk/KSU log dosyası erişilemiyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    sec!!.rootAccessLogs.take(20).forEach { log ->
                        RootLogRow(log)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }
        }

        // Kullanıcı sertifikaları
        item {
            SectionCard("Kullanıcı CA Sertifikaları", Icons.Default.VpnKey) {
                Text(
                    "Sisteme yüklenmiş kullanıcı sertifikaları. Şüpheli sertifikaları kaldırın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                if (sec?.installedCerts.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Kullanıcı sertifikası yok", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    sec!!.installedCerts.forEach { hash ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null,
                                tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(hash, style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { vm.removeUserCert(hash) }) {
                                Icon(Icons.Default.Delete, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Hosts dosyası editörü
        item {
            HostsEditorCard(vm)
        }
    }
}

@Composable
fun SecurityStatusRow(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f)) {
            Text(value, style = MaterialTheme.typography.labelSmall, color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
        }
    }
}

@Composable
fun RootLogRow(log: RootAccessLog) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (log.granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            null,
            tint = if (log.granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(log.appLabel, style = MaterialTheme.typography.bodyMedium)
            Text(log.packageName, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text(
            if (log.granted) "İzin Verildi" else "Reddedildi",
            style = MaterialTheme.typography.labelSmall,
            color = if (log.granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun HostsEditorCard(vm: MainViewModel) {
    var domain by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("0.0.0.0") }
    var hostsContent by remember { mutableStateOf("") }
    var showHosts by remember { mutableStateOf(false) }

    SectionCard("Hosts Dosyası Editörü", Icons.Default.Block) {
        Text(
            "Hosts dosyasına kural ekleyerek reklam ve izleme sunucularını engelleyin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(10.dp))

        // Hızlı engelleme presetleri
        Text("Hızlı Engelleme", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(6.dp))
        val adDomains = listOf(
            "ads.google.com",
            "doubleclick.net",
            "googleadservices.com",
            "facebook.com/ads",
            "analytics.google.com"
        )
        adDomains.forEach { d ->
            OutlinedButton(
                onClick = { vm.addHostsEntry(d, "0.0.0.0") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Icon(Icons.Default.Block, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(d, style = MaterialTheme.typography.bodySmall)
            }
        }

        SectionDivider()

        // Manuel ekleme
        Text("Manuel Kural Ekle", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = domain, onValueChange = { domain = it },
            label = { Text("Domain (örn: ads.example.com)") },
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
                onClick = {
                    if (domain.isNotBlank()) {
                        vm.addHostsEntry(domain.trim(), ip.trim())
                        domain = ""
                    }
                },
                enabled = domain.isNotBlank()
            ) { Text("Ekle") }
        }

        SectionDivider()

        // Hosts içeriğini göster
        TextButton(
            onClick = {
                if (!showHosts) hostsContent = SecurityManager.getHostsFile()
                showHosts = !showHosts
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                if (showHosts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(if (showHosts) "Hosts Dosyasını Gizle" else "Hosts Dosyasını Göster")
        }

        if (showHosts && hostsContent.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    hostsContent,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(10.dp).fillMaxWidth()
                )
            }
        }
    }
}
