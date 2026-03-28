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
import com.duchamp.control.MagiskModule
import com.duchamp.control.MainViewModel

@Composable
fun MagiskScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val rootInfo = state.rootInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Root bilgisi
        item {
            SectionCard("Root Bilgisi", Icons.Default.Security) {
                if (rootInfo == null) {
                    Text("Root bilgisi yüklenemedi")
                } else {
                    InfoRow("Root Tipi", rootInfo.rootType,
                        if (rootInfo.rootType != "Bilinmiyor") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                    InfoRow("Versiyon", rootInfo.version.ifEmpty { "N/A" })
                    InfoRow("Versiyon Kodu", rootInfo.versionCode.ifEmpty { "N/A" })
                }
            }
        }

        // Güvenlik durumu
        item {
            SectionCard("Güvenlik Durumu", Icons.Default.Lock) {
                if (rootInfo == null) {
                    Text("Bilgi alınamadı")
                } else {
                    val blState = rootInfo.bootloaderState
                    val vbState = rootInfo.verifiedBootState
                    val dmState = rootInfo.dmVerity

                    InfoRow(
                        "Bootloader",
                        if (blState == "0") "Unlocked" else if (blState == "1") "Locked" else blState,
                        if (blState == "1") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    InfoRow(
                        "Verified Boot",
                        vbState.ifEmpty { "N/A" },
                        if (vbState == "green") MaterialTheme.colorScheme.primary
                        else if (vbState == "orange") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    InfoRow("DM-Verity", dmState.ifEmpty { "N/A" })
                }
            }
        }

        // Modüller
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${rootInfo?.modules?.size ?: 0} modül yüklü",
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedButton(onClick = { vm.refreshRootInfo() }) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Yenile")
                }
            }
        }

        if (rootInfo?.modules.isNullOrEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Extension, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("Modül bulunamadı",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        } else {
            items(rootInfo!!.modules) { module ->
                ModuleCard(
                    module = module,
                    onToggle = { vm.setModuleEnabled(module.id, it) }
                )
            }
        }
    }
}

@Composable
fun ModuleCard(module: MagiskModule, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.Extension,
                        contentDescription = null,
                        tint = if (module.enabled) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(module.name, style = MaterialTheme.typography.bodyMedium)
                        if (module.version.isNotEmpty()) {
                            Text(
                                "v${module.version}" + if (module.author.isNotEmpty()) " · ${module.author}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                Switch(
                    checked = module.enabled,
                    onCheckedChange = onToggle
                )
            }
            if (module.description.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (!module.enabled) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "Devre dışı — Yeniden başlatmada etkili olur",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
