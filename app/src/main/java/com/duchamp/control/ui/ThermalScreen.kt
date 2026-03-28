package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.ThermalInfo

@Composable
fun ThermalScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${state.thermals.size} termal bölge bulundu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                OutlinedButton(onClick = { vm.refreshThermals() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Yenile")
                }
            }
        }

        if (state.thermals.isEmpty()) {
            item { Text("Termal bilgi alınamadı") }
        } else {
            items(state.thermals) { t ->
                ThermalZoneCard(t)
            }
        }
    }
}

@Composable
fun ThermalZoneCard(t: ThermalInfo) {
    val (color, label) = when {
        t.tempRaw > 80000 -> MaterialTheme.colorScheme.error to "Kritik"
        t.tempRaw > 60000 -> Color(0xFFFF9800) to "Yüksek"
        t.tempRaw > 40000 -> Color(0xFFFFEB3B) to "Normal"
        else               -> MaterialTheme.colorScheme.primary to "Soğuk"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Thermostat, contentDescription = null,
                tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(t.zone, style = MaterialTheme.typography.bodyMedium)
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            }
            Text(t.tempC, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}
