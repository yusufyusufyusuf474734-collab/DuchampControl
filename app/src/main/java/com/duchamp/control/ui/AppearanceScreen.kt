package com.duchamp.control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.AppTheme
import com.duchamp.control.MainViewModel
import com.duchamp.control.accentColors

@Composable
fun AppearanceScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tema seçimi
        item {
            SectionCard("Tema", Icons.Default.DarkMode) {
                val themes = listOf(
                    AppTheme.DARK   to ("Koyu"   to Icons.Default.DarkMode),
                    AppTheme.LIGHT  to ("Açık"   to Icons.Default.LightMode),
                    AppTheme.SYSTEM to ("Sistem" to Icons.Default.SettingsBrightness)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (theme, pair) ->
                        val (label, icon) = pair
                        val selected = state.appTheme == theme
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            onClick = { vm.setTheme(theme) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    icon, null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                if (selected) {
                                    Spacer(Modifier.height(4.dp))
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Accent rengi
        item {
            SectionCard("Vurgu Rengi", Icons.Default.Palette) {
                Text(
                    "Seçilen renk tüm arayüzde kullanılır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(accentColors.size) { i ->
                        val accent = accentColors[i]
                        val selected = state.accentColorIndex == i
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accent.color)
                                .then(
                                    if (selected) Modifier.border(
                                        3.dp, MaterialTheme.colorScheme.onSurface, CircleShape
                                    ) else Modifier
                                )
                                .clickable { vm.setAccentColor(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(Icons.Default.Check, null,
                                    tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accentColors[state.accentColorIndex].color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        accentColors[state.accentColorIndex].name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Dashboard ayarları
        item {
            SectionCard("Dashboard", Icons.Default.Dashboard) {
                ControlRow(
                    label = "Kompakt Görünüm",
                    description = "Kartları daha küçük göster",
                    checked = state.dashboardCompact,
                    onCheckedChange = { vm.setDashboardCompact(it) }
                )
            }
        }

        // Önizleme
        item {
            SectionCard("Önizleme", Icons.Default.Preview) {
                Text(
                    "Mevcut tema ve renk kombinasyonu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}) { Text("Birincil") }
                    OutlinedButton(onClick = {}) { Text("İkincil") }
                    TextButton(onClick = {}) { Text("Metin") }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Seçili") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Normal") })
                    SuggestionChip(onClick = {}, label = { Text("Öneri") })
                }
            }
        }
    }
}
