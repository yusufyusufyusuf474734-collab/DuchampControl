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
fun SystemScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val sys = state.systemInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sistem bilgisi
        item {
            SectionCard("Sistem Bilgisi", Icons.Default.Info) {
                InfoRow("Kernel", sys?.kernelVersion ?: "N/A")
                InfoRow("Uptime", sys?.uptime ?: "N/A")
                InfoRow("Load Average", sys?.loadAvg ?: "N/A")
                InfoRow("SELinux", sys?.selinuxMode ?: "N/A",
                    if (sys?.selinuxMode == "Enforcing") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
            }
        }

        // SELinux kontrolü
        item {
            SectionCard("SELinux", Icons.Default.Security) {
                Text("SELinux'u Permissive moda almak bazı root işlemlerini kolaylaştırır ancak güvenliği azaltır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                ControlRow(
                    label = "SELinux Enforcing",
                    description = "Kapatmak = Permissive mod",
                    checked = sys?.selinuxMode == "Enforcing",
                    onCheckedChange = { vm.setSELinux(it) }
                )
            }
        }

        // Build Props
        if (!sys?.buildProps.isNullOrEmpty()) {
            item {
                SectionCard("Build Props", Icons.Default.Code) {
                    sys!!.buildProps.forEach { (k, v) ->
                        InfoRow(k.substringAfterLast("."), v)
                    }
                }
            }
        }

        // Prop editörü
        item {
            SectionCard("Prop Editörü", Icons.Default.Edit) {
                var propKey by remember { mutableStateOf("") }
                var propVal by remember { mutableStateOf("") }

                Text("Sistem özelliklerini (prop) okuyun ve yazın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                Text("Hızlı Prop Seçimi", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                val quickProps = listOf(
                    "ro.debuggable" to "1",
                    "persist.sys.usb.config" to "adb",
                    "ro.build.type" to "userdebug"
                )
                quickProps.forEach { (k, v) ->
                    OutlinedButton(
                        onClick = { propKey = k; propVal = v },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$k = $v", style = MaterialTheme.typography.bodySmall)
                    }
                }

                SectionDivider()
                OutlinedTextField(
                    value = propKey, onValueChange = { propKey = it },
                    label = { Text("Prop Anahtarı") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = propVal, onValueChange = { propVal = it },
                    label = { Text("Değer") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (propKey.isNotBlank()) {
                                propVal = com.duchamp.control.RootUtils.runCommand("getprop $propKey")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Oku") }
                    Button(
                        onClick = {
                            if (propKey.isNotBlank() && propVal.isNotBlank()) {
                                vm.setProp(propKey, propVal)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Yaz") }
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
