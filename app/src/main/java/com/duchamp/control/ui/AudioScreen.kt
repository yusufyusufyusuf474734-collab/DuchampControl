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
fun AudioScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val audio = state.audioInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dolby Atmos
        item {
            SectionCard("Dolby Atmos", Icons.Default.GraphicEq) {
                ControlRow(
                    label = "Dolby Atmos",
                    description = "Xiaomi Dolby ses işleme",
                    checked = audio?.dolbyEnabled == true,
                    onCheckedChange = { vm.setDolbyEnabled(it) }
                )
                if (audio?.dolbyEnabled == true) {
                    SectionDivider()
                    val profiles = listOf("0" to "Akıllı", "1" to "Film", "2" to "Müzik", "3" to "Oyun", "4" to "Ses")
                    Text("Dolby Profili", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        profiles.forEach { (id, name) ->
                            FilterChip(
                                selected = audio.dolbyProfile == id,
                                onClick = { vm.setDolbyProfile(id) },
                                label = { Text(name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    val profileDesc = when (audio.dolbyProfile) {
                        "0" -> "İçeriğe göre otomatik profil seçer"
                        "1" -> "Film ve dizi için optimize edilmiş"
                        "2" -> "Müzik dinleme için optimize edilmiş"
                        "3" -> "Oyun için düşük gecikme ve güçlü bas"
                        "4" -> "Ses/podcast için netlik odaklı"
                        else -> ""
                    }
                    if (profileDesc.isNotEmpty()) {
                        Text(profileDesc, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Hoparlör Gain
        item {
            SectionCard("Hoparlör Gain", Icons.Default.VolumeUp) {
                var spkGain by remember {
                    mutableFloatStateOf(audio?.speakerGain?.toFloatOrNull() ?: 0f)
                }
                Text("Hoparlör ses seviyesini fine-tune edin. Çok yüksek değerler bozulmaya yol açabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                InfoRow("Mevcut Değer", audio?.speakerGain ?: "N/A")
                SliderRow(
                    label = "Hoparlör Gain",
                    value = spkGain,
                    valueRange = -10f..10f,
                    steps = 19,
                    displayValue = spkGain.toInt().toString(),
                    onValueChangeFinished = {
                        spkGain = it
                        vm.setSpeakerGain(it.toInt())
                    }
                )
            }
        }

        // Mikrofon Gain
        item {
            SectionCard("Mikrofon Gain", Icons.Default.Mic) {
                var micGain by remember {
                    mutableFloatStateOf(audio?.micGain?.toFloatOrNull() ?: 0f)
                }
                InfoRow("Mevcut Değer", audio?.micGain ?: "N/A")
                SliderRow(
                    label = "Mikrofon Gain",
                    value = micGain,
                    valueRange = -5f..5f,
                    steps = 9,
                    displayValue = micGain.toInt().toString(),
                    onValueChangeFinished = {
                        micGain = it
                        vm.setMicGain(it.toInt())
                    }
                )
            }
        }

        // Ses donanımı bilgisi
        item {
            SectionCard("Ses Donanımı", Icons.Default.Speaker) {
                InfoRow("Hoparlör", "Stereo (2x)")
                InfoRow("Ses Codec", "MediaTek Audio HAL")
                InfoRow("Bluetooth Ses", "AIDL (aptX, LDAC)")
                InfoRow("USB Ses", "USB Audio Accessory")
                InfoRow("Ses Efektleri", "Dolby Atmos + Xiaomi Dolby")
                InfoRow("Düşük Gecikme", "android.hardware.audio.low_latency")
            }
        }
    }
}
