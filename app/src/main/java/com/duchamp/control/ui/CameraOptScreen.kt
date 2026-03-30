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
import com.duchamp.control.RootUtils

@Composable
fun CameraOptScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionCard("Kamera Kalite Optimizasyonu", Icons.Default.CameraAlt) {
                Text("Kamera prop'larını değiştirerek görüntü kalitesini artırın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))

                val props = listOf(
                    Triple("persist.vendor.camera.preview.display_60fps", "1",
                        "60fps önizleme"),
                    Triple("persist.vendor.camera.isp.tuning.enable", "1",
                        "ISP tuning aktif"),
                    Triple("persist.vendor.camera.ae.face.fd.enable", "1",
                        "Yüz algılama AE"),
                    Triple("persist.vendor.camera.video.disable.eis", "0",
                        "EIS video stabilizasyon"),
                    Triple("persist.vendor.camera.hdr.enable", "1",
                        "HDR modu"),
                    Triple("persist.vendor.camera.night.mode.enable", "1",
                        "Gece modu iyileştirme")
                )

                props.forEach { (key, value, desc) ->
                    var current by remember {
                        mutableStateOf(RootUtils.runCommand("getprop $key").ifBlank { "N/A" })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(desc, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(key, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Mevcut: $current", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = {
                            vm.setProp(key, value)
                            current = value
                        }) { Text("Uygula") }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }
        }

        item {
            SectionCard("Video Kalitesi", Icons.Default.Videocam) {
                val videoProps = listOf(
                    Triple("persist.vendor.camera.video.4k.enable", "1", "4K video aktif"),
                    Triple("persist.vendor.camera.video.hdr.enable", "1", "Video HDR"),
                    Triple("persist.vendor.camera.video.fps.60", "1", "60fps video"),
                    Triple("persist.vendor.camera.video.fps.120", "1", "120fps slow-mo")
                )
                videoProps.forEach { (key, value, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                            Text(key, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { vm.setProp(key, value) }) { Text("Uygula") }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }
        }

        item {
            SectionCard("Hızlı Preset'ler", Icons.Default.AutoAwesome) {
                Text("Tüm kamera optimizasyonlarını tek tıkla uygulayın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { vm.applyCameraPreset("quality") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.HighQuality, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Maksimum Kalite Preset'i Uygula")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { vm.applyCameraPreset("reset") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Varsayılana Sıfırla")
                }
            }
        }
    }
}
