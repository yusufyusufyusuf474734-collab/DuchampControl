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
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import kotlin.math.abs

@Composable
fun BatteryScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val bat = state.batteryInfo ?: return

    // Pil tüketim tahmini
    val currentMa = bat.currentNow.replace(" mA", "").toFloatOrNull()?.let { abs(it) } ?: 0f
    val capacityMah = 5110f // Poco X6 Pro batarya kapasitesi
    val remainingMah = capacityMah * bat.capacity / 100f
    val hoursLeft = if (currentMa > 0) remainingMah / currentMa else 0f
    val isCharging = bat.status.contains("Charging", ignoreCase = true)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pil durumu + tahmin
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    UsageBar(
                        "Şarj Seviyesi",
                        bat.capacity / 100f,
                        color = when {
                            bat.capacity > 50 -> MaterialTheme.colorScheme.tertiary
                            bat.capacity > 20 -> Color(0xFFF59E0B)
                            else              -> MaterialTheme.colorScheme.error
                        },
                        height = 8.dp
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = if (isCharging) "Şarj Süresi" else "Kalan Süre",
                            value = if (hoursLeft > 0) {
                                val h = hoursLeft.toInt()
                                val m = ((hoursLeft - h) * 60).toInt()
                                "${h}s ${m}dk"
                            } else "—",
                            icon = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Akım",
                            value = bat.currentNow,
                            icon = Icons.Default.ElectricBolt,
                            color = if (isCharging) MaterialTheme.colorScheme.tertiary else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Sıcaklık",
                            value = bat.tempC,
                            icon = Icons.Default.Thermostat,
                            color = if ((bat.tempC.replace("°C","").toFloatOrNull() ?: 0f) > 40f)
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Şarj akımı grafiği
        if (state.chargeHistory.isNotEmpty()) {
            item {
                SectionCard("Şarj Akımı Geçmişi", Icons.Default.ShowChart) {
                    MetricChart(
                        title = "Akım (mA)",
                        data = state.chargeHistory,
                        color = if (isCharging) MaterialTheme.colorScheme.tertiary else Color(0xFFF59E0B),
                        unit = "mA",
                        maxValue = 6000f
                    )
                }
            }
        }

        // Voltaj grafiği
        if (state.voltageHistory.isNotEmpty()) {
            item {
                SectionCard("Voltaj Geçmişi", Icons.Default.Power) {
                    MetricChart(
                        title = "Voltaj (mV)",
                        data = state.voltageHistory,
                        color = MaterialTheme.colorScheme.secondary,
                        unit = "mV",
                        maxValue = 5000f
                    )
                }
            }
        }

        // Elektriksel değerler
        item {
            SectionCard("Elektriksel Değerler", Icons.Default.Power) {
                InfoRow("Durum", bat.status)
                InfoRow("Sağlık", bat.health)
                InfoRow("Teknoloji", bat.technology)
                InfoRow("Şarj Döngüsü", bat.cycleCount)
                SectionDivider()
                InfoRow("Sıcaklık", bat.tempC)
                InfoRow("Voltaj", bat.voltage)
                InfoRow("Akım", bat.currentNow)
                InfoRow("Giriş Akımı Limiti", bat.inputCurrentLimit)
            }
        }

        // Pil tüketim tahmini detayı
        item {
            SectionCard("Pil Tüketim Analizi", Icons.Default.Analytics) {
                InfoRow("Batarya Kapasitesi", "${capacityMah.toInt()} mAh")
                InfoRow("Kalan Kapasite", "${remainingMah.toInt()} mAh")
                InfoRow("Anlık Tüketim", "${currentMa.toInt()} mA")
                if (hoursLeft > 0 && !isCharging) {
                    SectionDivider()
                    val scenarios = listOf(
                        "Bekleme" to (remainingMah / 150f),
                        "Normal Kullanım" to (remainingMah / currentMa),
                        "Yoğun Kullanım" to (remainingMah / (currentMa * 1.8f))
                    )
                    scenarios.forEach { (label, hours) ->
                        val h = hours.toInt()
                        val m = ((hours - h) * 60).toInt()
                        InfoRow(label, "${h}s ${m}dk")
                    }
                }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                SliderRow(
                    label = "Şarj Limiti",
                    value = limitVal,
                    valueRange = 60f..100f,
                    steps = 7,
                    displayValue = "${limitVal.toInt()}%",
                    onValueChangeFinished = { limitVal = it; vm.setChargeLimit(it.toInt()) }
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

        // Şarj ayarları
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

        // Pil bildirimi
        item {
            SectionCard("Şarj Bildirimi", Icons.Default.NotificationsActive) {
                var notifyPct by remember { mutableFloatStateOf(state.chargeNotifyPct.toFloat()) }
                Text("Belirtilen şarj seviyesine ulaşınca bildirim gönderir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                ControlRow(
                    label = "Şarj Bildirimi",
                    description = "Hedef şarj seviyesine ulaşınca uyar",
                    checked = state.chargeNotifyEnabled,
                    onCheckedChange = { vm.setChargeNotify(it, notifyPct.toInt()) }
                )
                Spacer(Modifier.height(8.dp))
                SliderRow(
                    label = "Bildirim Eşiği",
                    value = notifyPct,
                    valueRange = 50f..100f,
                    steps = 9,
                    displayValue = "${notifyPct.toInt()}%",
                    onValueChangeFinished = {
                        notifyPct = it
                        vm.setChargeNotify(state.chargeNotifyEnabled, it.toInt())
                    }
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(70, 80, 85, 90, 100).forEach { pct ->
                        FilterChip(
                            selected = notifyPct.toInt() == pct,
                            onClick = { notifyPct = pct.toFloat(); vm.setChargeNotify(state.chargeNotifyEnabled, pct) },
                            label = { Text("$pct%", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Gece şarj modu
        item {
            SectionCard("Gece Şarj Modu", Icons.Default.Bedtime) {
                var startHour by remember { mutableIntStateOf(state.nightChargeStartHour) }
                var endHour by remember { mutableIntStateOf(state.nightChargeEndHour) }
                var limitPct by remember { mutableFloatStateOf(state.nightChargeLimitPct.toFloat()) }

                Text("Belirtilen saatler arasında şarj limitini düşürerek batarya ömrünü korur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                ControlRow(
                    label = "Gece Şarj Modu",
                    description = "%02d:00 - %02d:00 arası %%%d ile sınırla".format(startHour, endHour, limitPct.toInt()),
                    checked = state.nightChargeEnabled,
                    onCheckedChange = { vm.setNightCharge(it, startHour, endHour, limitPct.toInt()) }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Başlangıç Saati",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(21, 22, 23).forEach { h ->
                                FilterChip(
                                    selected = startHour == h,
                                    onClick = { startHour = h; vm.setNightCharge(state.nightChargeEnabled, h, endHour, limitPct.toInt()) },
                                    label = { Text("$h:00", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bitiş Saati",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(6, 7, 8).forEach { h ->
                                FilterChip(
                                    selected = endHour == h,
                                    onClick = { endHour = h; vm.setNightCharge(state.nightChargeEnabled, startHour, h, limitPct.toInt()) },
                                    label = { Text("$h:00", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SliderRow(
                    label = "Gece Şarj Limiti",
                    value = limitPct,
                    valueRange = 60f..90f,
                    steps = 5,
                    displayValue = "${limitPct.toInt()}%",
                    onValueChangeFinished = {
                        limitPct = it
                        vm.setNightCharge(state.nightChargeEnabled, startHour, endHour, it.toInt())
                    }
                )
            }
        }
    }
}
