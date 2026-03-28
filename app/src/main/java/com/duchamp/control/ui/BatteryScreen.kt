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
fun BatteryScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val bat = state.batteryInfo ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Genel durum
        item {
            SectionCard("Batarya Durumu", Icons.Default.BatteryFull) {
                UsageBar("Şarj Seviyesi", bat.capacity.toFloat() / 100f)
                Spacer(Modifier.height(12.dp))
                InfoRow("Kapasite", "${bat.capacity}%")
                InfoRow("Durum", bat.status)
                InfoRow("Sağlık", bat.health)
                InfoRow("Teknoloji", bat.technology)
                InfoRow("Şarj Döngüsü", bat.cycleCount)
            }
        }

        // Elektriksel değerler
        item {
            SectionCard("Elektriksel Değerler", Icons.Default.ElectricBolt) {
                InfoRow("Sıcaklık", bat.tempC)
                InfoRow("Voltaj", bat.voltage)
                InfoRow("Akım", bat.currentNow)
                InfoRow("Giriş Akımı Limiti", bat.inputCurrentLimit)
            }
        }

        // Şarj limiti
        item {
            SectionCard("Şarj Limiti", Icons.Default.BatteryAlert) {
                var limitVal by remember { mutableFloatStateOf(
                    bat.chargeLimit.replace("%", "").toFloatOrNull() ?: 100f
                ) }
                Text("Batarya ömrünü uzatmak için şarj limitini düşürebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                SliderRow(
                    label = "Şarj Limiti",
                    value = limitVal,
                    valueRange = 60f..100f,
                    steps = 7,
                    displayValue = "${limitVal.toInt()}%",
                    onValueChangeFinished = {
                        limitVal = it
                        vm.setChargeLimit(it.toInt())
                    }
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(80, 85, 90, 95, 100).forEach { pct ->
                        FilterChip(
                            selected = limitVal.toInt() == pct,
                            onClick = { limitVal = pct.toFloat(); vm.setChargeLimit(pct) },
                            label = { Text("$pct%") }
                        )
                    }
                }
            }
        }

        // Hızlı şarj
        item {
            SectionCard("Şarj Ayarları", Icons.Default.FlashOn) {
                ControlRow(
                    label = "Hızlı Şarj",
                    description = "Hypercharge / Turbo Charge",
                    checked = bat.fastChargeEnabled,
                    onCheckedChange = { vm.setFastCharge(it) }
                )
            }
        }

        // Yenile butonu
        item {
            OutlinedButton(
                onClick = { vm.refreshBattery() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Batarya Bilgisini Yenile")
            }
        }
    }
}
