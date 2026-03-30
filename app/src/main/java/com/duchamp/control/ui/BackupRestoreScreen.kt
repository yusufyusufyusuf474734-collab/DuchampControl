package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun BackupRestoreScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var backupDone by remember { mutableStateOf(false) }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Geri Yükle") },
            text = {
                Text("Mevcut tüm ayarlar silinip yedekten geri yüklenecek. Devam etmek istiyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.restoreSettings()
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Geri Yükle") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("İptal") }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionCard("Ayarları Yedekle", Icons.Default.Backup) {
                Text(
                    "Tüm profil, zamanlayıcı, uygulama profili ve tema ayarlarınızı JSON formatında dışa aktarın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                // Yedek kapsamı
                val items = listOf(
                    Icons.Default.Tune        to "Performans Profilleri",
                    Icons.Default.Schedule    to "Zamanlayıcı Kuralları",
                    Icons.Default.AppSettingsAlt to "Uygulama Profilleri",
                    Icons.Default.Palette     to "Tema & Görünüm Ayarları",
                    Icons.Default.Bedtime     to "Uyku Modu Ayarları"
                )
                items.forEach { (icon, label) ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        vm.backupSettings()
                        backupDone = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Yedek Al")
                }

                if (backupDone) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Yedek alındı: /sdcard/DimensityTool/backup.json",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }

        item {
            SectionCard("Ayarları Geri Yükle", Icons.Default.Restore) {
                Text(
                    "Daha önce alınan yedekten ayarları geri yükleyin. Mevcut ayarlar silinir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                val backupPath = vm.getBackupPath()
                if (backupPath != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.InsertDriveFile, null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("backup.json",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(backupPath,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Geri Yükle")
                    }
                } else {
                    EmptyState(
                        icon = Icons.Default.FolderOff,
                        title = "Yedek bulunamadı",
                        subtitle = "Önce yedek alın"
                    )
                }
            }
        }

        item {
            SectionCard("Fabrika Sıfırlama", Icons.Default.RestartAlt) {
                Text(
                    "Tüm uygulama ayarlarını varsayılana döndürür. Sistem ayarlarına dokunulmaz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { vm.resetAllSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tüm Ayarları Sıfırla")
                }
            }
        }
    }
}
