package com.duchamp.control.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun DisplayScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val disp = state.displayInfo

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ekran bilgisi
        item {
            SectionCard("Ekran Bilgisi", Icons.Default.Tv) {
                InfoRow("Tip", "AMOLED 6.67\"")
                InfoRow("Çözünürlük", "1220 x 2712 (446 ppi)")
                InfoRow("Renk", "68B renk, HDR10+, Dolby Vision")
                InfoRow("Parlaklık", "500 nit (typ) / 1800 nit (peak)")
                InfoRow("Mevcut Yenileme Hızı", disp?.refreshRate ?: "N/A")
            }
        }

        // Yenileme hızı
        item {
            SectionCard("Yenileme Hızı", Icons.Default.Speed) {
                Text("Düşük yenileme hızı pil ömrünü uzatır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60, 90, 120).forEach { hz ->
                        val selected = disp?.refreshRate?.contains(hz.toString()) == true
                        FilterChip(
                            selected = selected,
                            onClick = { vm.setRefreshRate(hz) },
                            label = { Text("${hz}Hz") }
                        )
                    }
                }
            }
        }

        // HBM
        item {
            SectionCard("Yüksek Parlaklık Modu (HBM)", Icons.Default.WbSunny) {
                Text("Dış mekanda maksimum parlaklık için HBM'yi etkinleştirin. Pil tüketimini artırır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                ControlRow(
                    label = "HBM (1800 nit)",
                    description = "Maksimum parlaklık modu",
                    checked = disp?.hbmEnabled == true,
                    onCheckedChange = { vm.setHbm(it) }
                )
            }
        }

        // DC Dimming
        item {
            SectionCard("DC Dimming", Icons.Default.BrightnessLow) {
                Text("DC Dimming, düşük parlaklıkta PWM titreşimini azaltır. Göz yorgunluğunu azaltabilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                ControlRow(
                    label = "DC Dimming",
                    description = "PWM titreşimini azalt",
                    checked = disp?.dcDimmingEnabled == true,
                    onCheckedChange = { vm.setDcDimming(it) }
                )
            }
        }

        // Renk profili bilgisi
        item {
            SectionCard("Renk Profili", Icons.Default.Palette) {
                InfoRow("Mevcut Profil", disp?.colorProfile ?: "N/A")
                Spacer(Modifier.height(4.dp))
                Text("Renk profili değişikliği için Ekran Ayarları > Renk Modu kullanın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}
