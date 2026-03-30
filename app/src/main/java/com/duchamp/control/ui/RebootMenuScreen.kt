package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

data class RebootOption(
    val label: String,
    val description: String,
    val command: String,
    val icon: ImageVector,
    val color: Color,
    val dangerous: Boolean = false
)

@Composable
fun RebootMenuScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var confirmOption by remember { mutableStateOf<RebootOption?>(null) }

    confirmOption?.let { opt ->
        AlertDialog(
            onDismissRequest = { confirmOption = null },
            icon = { Icon(opt.icon, null, tint = opt.color) },
            title = { Text(opt.label) },
            text = { Text("${opt.description}\n\nDevam etmek istiyor musunuz?") },
            confirmButton = {
                Button(
                    onClick = { vm.reboot(opt.command); confirmOption = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (opt.dangerous) MaterialTheme.colorScheme.error
                                         else MaterialTheme.colorScheme.primary
                    )
                ) { Text(opt.label) }
            },
            dismissButton = { TextButton(onClick = { confirmOption = null }) { Text("İptal") } }
        )
    }

    val options = listOf(
        RebootOption("Normal Yeniden Başlat", "Cihazı normal şekilde yeniden başlatır",
            "reboot", Icons.Default.RestartAlt, MaterialTheme.colorScheme.primary),
        RebootOption("Recovery Modu", "Recovery moduna yeniden başlatır",
            "reboot recovery", Icons.Default.Build, Color(0xFFF59E0B)),
        RebootOption("Fastboot Modu", "Fastboot/Bootloader moduna yeniden başlatır",
            "reboot bootloader", Icons.Default.DeveloperMode, Color(0xFF8B5CF6)),
        RebootOption("EDL Modu", "Emergency Download moduna girer (9008)",
            "reboot edl", Icons.Default.Warning, MaterialTheme.colorScheme.error, dangerous = true),
        RebootOption("Sistem Kapatma", "Cihazı kapatır",
            "poweroff", Icons.Default.PowerSettingsNew, MaterialTheme.colorScheme.error, dangerous = true),
        RebootOption("Sadece Sistem Yeniden Başlat", "Yalnızca Android sistemini yeniden başlatır",
            "setprop ctl.restart zygote", Icons.Default.Refresh, MaterialTheme.colorScheme.secondary)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("EDL modu cihazı kurtarma moduna alır. Yanlış kullanım cihazı brick edebilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        items(options.size) { i ->
            val opt = options[i]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (opt.dangerous)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                onClick = { confirmOption = opt }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = opt.color.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(opt.icon, null, tint = opt.color, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(opt.label, style = MaterialTheme.typography.titleSmall,
                            color = if (opt.dangerous) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface)
                        Text(opt.description, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
