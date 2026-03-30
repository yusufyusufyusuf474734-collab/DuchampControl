package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.BootScriptManager
import com.duchamp.control.MainViewModel
import com.duchamp.control.PerformanceProfiles

@Composable
fun BootScriptScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var scriptContent by remember { mutableStateOf(state.bootScriptContent) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProfileId by remember { mutableStateOf("balanced") }

    LaunchedEffect(Unit) { vm.loadBootScript() }
    LaunchedEffect(state.bootScriptContent) { scriptContent = state.bootScriptContent }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Scripti Sil") },
            text = { Text("Önyükleme scripti silinecek. Bir sonraki açılışta ayarlar uygulanmayacak.") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteBootScript(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") } }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Durum kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.bootScriptExists)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
                        color = if (state.bootScriptExists)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.bootScriptExists) Icons.Default.CheckCircle
                                else Icons.Default.Code,
                                null,
                                tint = if (state.bootScriptExists) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Önyükleme Scripti",
                            style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (state.bootScriptExists) "Aktif — ${BootScriptManager.getScriptPath()}"
                            else "Script bulunamadı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.bootScriptExists) {
                        StatusBadge("Aktif", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Profil bazlı otomatik oluştur
        item {
            SectionCard("Profilden Oluştur", Icons.Default.AutoAwesome) {
                Text("Seçili performans profilinin ayarlarından otomatik script oluşturun.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerformanceProfiles.presets.forEach { p ->
                        FilterChip(
                            selected = selectedProfileId == p.id,
                            onClick = { selectedProfileId = p.id },
                            label = { Text(p.name, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(profileIcon(p.id), null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { vm.generateBootScriptFromProfile(selectedProfileId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Script Oluştur")
                }
            }
        }

        // Script editörü
        item {
            SectionCard("Script Editörü", Icons.Default.Code) {
                Text("Her açılışta çalıştırılacak shell komutlarını düzenleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = scriptContent,
                    onValueChange = { scriptContent = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    placeholder = {
                        Text(
                            "#!/system/bin/sh\n# Komutlarınızı buraya yazın\necho performance > /sys/devices/system/cpu/cpufreq/policy7/scaling_governor",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.saveBootScript(scriptContent) },
                        modifier = Modifier.weight(1f),
                        enabled = scriptContent.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kaydet & Uygula")
                    }
                    if (state.bootScriptExists) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Hazır komutlar
        item {
            SectionCard("Hazır Komutlar", Icons.Default.Lightbulb) {
                Text("Tıklayarak editöre ekleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                val snippets = listOf(
                    "CPU Performance" to "echo performance > /sys/devices/system/cpu/cpufreq/policy7/scaling_governor",
                    "CPU Schedutil"   to "for p in 0 4 7; do echo schedutil > /sys/devices/system/cpu/cpufreq/policy\$p/scaling_governor; done",
                    "TCP BBR"         to "echo bbr > /proc/sys/net/ipv4/tcp_congestion_control",
                    "Swappiness 60"   to "echo 60 > /proc/sys/vm/swappiness",
                    "GPU Performance" to "echo performance > /sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali/governor",
                    "Sched Boost Off" to "echo 0 > /proc/sys/kernel/sched_boost",
                    "Touch 240Hz"     to "echo 240 > /sys/devices/platform/goodix_ts.0/switch_report_rate"
                )
                snippets.forEach { (label, cmd) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(cmd, style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = {
                                scriptContent = if (scriptContent.isBlank()) cmd
                                else "$scriptContent\n$cmd"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15.dp.value),
                        thickness = 0.5.dp)
                }
            }
        }

        // Bilgi
        item {
            SectionCard("Script Konumu", Icons.Default.FolderOpen) {
                val hasMagisk = true // runtime'da kontrol edilir
                InfoRow("Magisk (post-fs-data)", "/data/adb/post-fs-data.d/dimensitytool.sh")
                InfoRow("init.d (fallback)", "/system/etc/init.d/99dimensitytool")
                SectionDivider()
                Text("Script her önyüklemede otomatik çalıştırılır. Magisk kuruluysa Magisk yolu tercih edilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
