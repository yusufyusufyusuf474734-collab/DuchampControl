package com.duchamp.control.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
fun TouchScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Polling rate
        item {
            SectionCard("Dokunmatik Polling Rate (Goodix)", Icons.Default.TouchApp) {
                InfoRow("Mevcut Polling Rate", "${state.touchPollingRate} Hz")
                Spacer(Modifier.height(8.dp))
                Text("Yüksek polling rate oyun deneyimini iyileştirir ancak pil tüketimini artırır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                val rates = listOf(
                    60 to "Pil Tasarrufu",
                    120 to "Normal",
                    240 to "Oyun",
                    360 to "Maksimum"
                )
                rates.forEach { (rate, label) ->
                    val selected = state.touchPollingRate == rate.toString()
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                        ),
                        onClick = { vm.setTouchPollingRate(rate) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${rate}Hz", style = MaterialTheme.typography.bodyMedium)
                                Text(label, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Dokunmatik bilgisi
        item {
            SectionCard("Dokunmatik Bilgisi", Icons.Default.Info) {
                InfoRow("Sürücü", "Goodix Touchscreen")
                InfoRow("Tip", "Ekran Altı Parmak İzi + Dokunmatik")
                InfoRow("Çoklu Dokunma", "Jazzhand (10 parmak)")
                InfoRow("Sysfs Yolu", "/sys/devices/platform/goodix_ts.0/")
            }
        }

        // IR Blaster
        item {
            SectionCard("IR Blaster Test", Icons.Default.Sensors) {
                var freq by remember { mutableStateOf("38000") }
                var pattern by remember { mutableStateOf("9000 4500 560 560") }

                Text("IR Blaster ile kızılötesi sinyal gönderin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                Text("Hızlı Test", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "38000" to "38kHz",
                        "36000" to "36kHz",
                        "56000" to "56kHz"
                    ).forEach { (f, label) ->
                        FilterChip(
                            selected = freq == f,
                            onClick = { freq = f },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = freq, onValueChange = { freq = it },
                    label = { Text("Frekans (Hz)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = pattern, onValueChange = { pattern = it },
                    label = { Text("Sinyal Deseni") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.sendIr(freq.toIntOrNull() ?: 38000, pattern) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("IR Sinyali Gönder")
                }
            }
        }
    }
}
