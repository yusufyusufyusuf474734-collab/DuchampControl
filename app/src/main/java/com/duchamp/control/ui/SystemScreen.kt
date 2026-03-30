package com.duchamp.control.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.RootUtils

@Composable
fun SystemScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val sys = state.systemInfo
    val context = LocalContext.current
    var propSearch by remember { mutableStateOf("") }
    var propKey by remember { mutableStateOf("") }
    var propVal by remember { mutableStateOf("") }
    var showAllProps by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sistem bilgisi + paylaşım
        item {
            SectionCard("Sistem Bilgisi", Icons.Default.Info) {
                InfoRow("Kernel", sys?.kernelVersion ?: "N/A")
                InfoRow("Uptime", sys?.uptime ?: "N/A")
                InfoRow("Load Average", sys?.loadAvg ?: "N/A")
                InfoRow("SELinux", sys?.selinuxMode ?: "N/A",
                    if (sys?.selinuxMode == "Enforcing") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
                SectionDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val text = vm.buildSystemShareText()
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Sistem Raporu", text))
                            vm.clearStatus()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kopyala")
                    }
                    OutlinedButton(
                        onClick = {
                            val text = vm.buildSystemShareText()
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Sistem Raporu Paylaş"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Paylaş")
                    }
                }
            }
        }

        // SELinux
        item {
            SectionCard("SELinux", Icons.Default.Security) {
                Text("SELinux'u Permissive moda almak bazı root işlemlerini kolaylaştırır ancak güvenliği azaltır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                ControlRow(
                    label = "SELinux Enforcing",
                    description = "Kapatmak = Permissive mod",
                    checked = sys?.selinuxMode == "Enforcing",
                    onCheckedChange = { vm.setSELinux(it) }
                )
            }
        }

        // Build Props özet
        if (!sys?.buildProps.isNullOrEmpty()) {
            item {
                SectionCard("Build Props", Icons.Default.Code) {
                    sys!!.buildProps.forEach { (k, v) ->
                        InfoRow(k.substringAfterLast("."), v)
                    }
                }
            }
        }

        // Prop Editörü
        item {
            SectionCard("Prop Editörü", Icons.Default.Edit) {
                Text("Sistem özelliklerini (prop) okuyun ve yazın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))

                // Hızlı preset'ler
                Text("Hızlı Preset'ler",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                val quickProps = listOf(
                    "ro.debuggable"           to "1",
                    "persist.sys.usb.config"  to "adb",
                    "ro.build.type"           to "userdebug",
                    "persist.sys.timezone"    to "Europe/Istanbul"
                )
                quickProps.forEach { (k, v) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(k, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                            Text(v, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { propKey = k; propVal = v },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("Seç", style = MaterialTheme.typography.labelSmall) }
                    }
                }

                SectionDivider()

                OutlinedTextField(
                    value = propKey, onValueChange = { propKey = it },
                    label = { Text("Prop Anahtarı") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = propVal, onValueChange = { propVal = it },
                    label = { Text("Değer") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (propKey.isNotBlank())
                                propVal = RootUtils.runCommand("getprop $propKey")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Oku")
                    }
                    Button(
                        onClick = {
                            if (propKey.isNotBlank() && propVal.isNotBlank())
                                vm.setProp(propKey, propVal)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = propKey.isNotBlank() && propVal.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yaz")
                    }
                }

                SectionDivider()

                // Tüm prop'ları listele
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = propSearch,
                        onValueChange = { propSearch = it },
                        placeholder = { Text("Prop ara...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { vm.loadAllProps(); showAllProps = true }) {
                        Text("Listele")
                    }
                }

                if (showAllProps && state.allProps.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    val filtered = if (propSearch.isBlank()) state.allProps.entries.take(50)
                                   else state.allProps.entries.filter {
                                       it.key.contains(propSearch, ignoreCase = true) ||
                                       it.value.contains(propSearch, ignoreCase = true)
                                   }.take(50)
                    Text("${filtered.size} sonuç",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    filtered.forEach { (k, v) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(k, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.primary)
                                Text(v, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { propKey = k; propVal = v }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                    }
                }
            }
        }

        // NFC
        item {
            SectionCard("NFC", Icons.Default.Contactless) {
                InfoRow("Çip", "NXP NFC")
                InfoRow("Özellikler", "HCE, HCEF, ESE, UICC, Mifare")
                SectionDivider()
                ControlRow(
                    label = "NFC",
                    description = "NFC donanımını aç/kapat",
                    checked = state.nfcEnabled,
                    onCheckedChange = { vm.setNfc(it) }
                )
            }
        }
    }
}
