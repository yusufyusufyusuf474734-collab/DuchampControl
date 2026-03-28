package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel
import com.duchamp.control.PerfProfile
import com.duchamp.control.PerformanceProfiles

@Composable
fun ProfilesScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Tek tıkla tüm performans parametrelerini ayarla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Aktif profil banner
        item {
            val active = PerformanceProfiles.presets.find { it.id == state.activeProfileId }
            if (active != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(profileIcon(active.id), null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Aktif Profil", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(active.name, style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(active.description, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // Profil kartları
        items(PerformanceProfiles.presets.size) { i ->
            val profile = PerformanceProfiles.presets[i]
            ProfileCard(
                profile = profile,
                isActive = profile.id == state.activeProfileId,
                onApply = { vm.applyProfile(profile) }
            )
        }

        // Mevcut ayarlar özeti
        item {
            SectionCard("Mevcut Ayarlar", Icons.Default.Info) {
                state.cpuInfo?.let { InfoRow("CPU Governor", it.governor) }
                state.gpuInfo?.let { InfoRow("GPU Governor", it.governor) }
                InfoRow("Touch Polling", "${state.touchPollingRate} Hz")
                state.memInfo?.let { InfoRow("Swappiness", it.swappiness) }
                state.networkInfo?.let { InfoRow("TCP Congestion", it.tcpCongestion) }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: PerfProfile, isActive: Boolean, onApply: () -> Unit) {
    val (bgColor, contentColor) = if (isActive)
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onApply
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(profileIcon(profile.id), null,
                        tint = contentColor, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(profile.name, style = MaterialTheme.typography.titleMedium, color = contentColor)
                        Text(profile.description, style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f))
                    }
                }
                if (isActive) {
                    Icon(Icons.Default.CheckCircle, null, tint = contentColor)
                } else {
                    OutlinedButton(
                        onClick = onApply,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
                    ) { Text("Uygula", color = contentColor) }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))

            // Parametre özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileParam("CPU", profile.cpuGovernor, contentColor, Modifier.weight(1f))
                ProfileParam("GPU", profile.gpuGovernor, contentColor, Modifier.weight(1f))
                ProfileParam("Touch", "${profile.touchPollingRate}Hz", contentColor, Modifier.weight(1f))
                ProfileParam("TCP", profile.tcpCongestion, contentColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ProfileParam(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = color,
            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@Composable
fun profileIcon(id: String): ImageVector = when (id) {
    "powersave"   -> Icons.Default.BatterySaver
    "balanced"    -> Icons.Default.Balance
    "performance" -> Icons.Default.Speed
    "gaming"      -> Icons.Default.SportsEsports
    else          -> Icons.Default.Tune
}
