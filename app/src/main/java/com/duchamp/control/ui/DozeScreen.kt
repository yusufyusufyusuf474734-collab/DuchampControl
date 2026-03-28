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
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.DozeManager
import com.duchamp.control.MainViewModel

@Composable
fun DozeScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var customPkg by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadDoze() }

    val filteredWhitelist = state.dozeWhitelist.filter {
        searchQuery.isEmpty() || it.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Doze durumu
        item {
            SectionCard("Doze Modu Durumu", Icons.Default.BatterySaver) {
                InfoRow("Doze Aktif", if (state.dozeEnabled) "Evet" else "Hayır",
                    if (state.dozeEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface)
                InfoRow("İstisna Sayısı", "${state.dozeWhitelist.size} uygulama")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Doze modu, ekran kapalıyken arka plan işlemlerini kısıtlayarak pil ömrünü uzatır. " +
                    "İstisna listesindeki uygulamalar Doze'dan etkilenmez ve bildirim alabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { vm.forceDoze() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Doze Zorla")
                    }
                    OutlinedButton(
                        onClick = { vm.exitDoze() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.WakeUp, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Doze Çık")
                    }
                }
            }
        }

        // Popüler uygulamalar hızlı ekleme
        item {
            SectionCard("Hızlı Ekle - Popüler Uygulamalar", Icons.Default.FlashOn) {
                Text(
                    "Sık kullanılan uygulamaları tek tıkla Doze istisnasına ekle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(10.dp))
                DozeManager.popularApps.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (pkg, name) ->
                            val inList = state.dozeWhitelist.contains(pkg)
                            OutlinedButton(
                                onClick = {
                                    if (inList) vm.removeDozeWhitelist(pkg)
                                    else vm.addDozeWhitelist(pkg)
                                },
                                modifier = Modifier.weight(1f),
                                colors = if (inList) ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Icon(
                                    if (inList) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                    null, modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Tek elemanlı satır için boşluk
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // Manuel paket ekleme
        item {
            SectionCard("Manuel Paket Ekle", Icons.Default.Add) {
                Text(
                    "Paket adını girerek herhangi bir uygulamayı istisna listesine ekle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customPkg,
                        onValueChange = { customPkg = it },
                        label = { Text("Paket adı (örn: com.whatsapp)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (customPkg.isNotBlank()) {
                                vm.addDozeWhitelist(customPkg.trim())
                                customPkg = ""
                            }
                        },
                        enabled = customPkg.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
                // Yüklü uygulamalardan seç
                if (state.installedApps.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Yüklü uygulamalardan seç:", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    val notInList = state.installedApps
                        .filter { !state.dozeWhitelist.contains(it.packageName) }
                        .take(5)
                    notInList.forEach { app ->
                        TextButton(
                            onClick = { vm.addDozeWhitelist(app.packageName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCircleOutline, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${app.label} (${app.packageName})",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        // İstisna listesi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "İstisna Listesi (${state.dozeWhitelist.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = { vm.loadDoze() }) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Arama
        if (state.dozeWhitelist.size > 5) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Listede ara...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    }
                )
            }
        }

        if (state.dozeWhitelist.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.BatterySaver, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "İstisna listesi boş",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                "Yukarıdan uygulama ekleyebilirsiniz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredWhitelist) { pkg ->
                DozeWhitelistItem(
                    pkg = pkg,
                    appLabel = state.installedApps.find { it.packageName == pkg }?.label,
                    popularName = DozeManager.popularApps.find { it.first == pkg }?.second,
                    onRemove = { vm.removeDozeWhitelist(pkg) }
                )
            }
        }
    }
}

@Composable
fun DozeWhitelistItem(
    pkg: String,
    appLabel: String?,
    popularName: String?,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.NotificationsActive, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val displayName = popularName ?: appLabel ?: pkg.substringAfterLast(".")
                Text(displayName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    pkg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.RemoveCircleOutline, null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
