package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.CustomProfile
import com.duchamp.control.MainViewModel
import java.util.UUID

@Composable
fun CustomProfilesScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { profile ->
                vm.addCustomProfile(profile)
                showCreateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Özel Profiller", style = MaterialTheme.typography.titleSmall)
                        Text("Kendi performans profilinizi oluşturun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yeni")
                    }
                }
            }
        }

        // Hazır preset'ler
        item {
            SectionCard("Hazır Profiller", Icons.Default.Tune) {
                Text("Mevcut 4 preset profil — bunları temel alarak özel profil oluşturabilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                com.duchamp.control.PerformanceProfiles.presets.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(profileIcon(p.id), null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.bodyMedium)
                            Text("${p.cpuGovernor} · ${p.gpuGovernor} · ${p.touchPollingRate}Hz",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (state.activeProfileId == p.id) {
                            StatusBadge("Aktif", MaterialTheme.colorScheme.primary)
                        } else {
                            TextButton(onClick = { vm.applyProfile(p) }) { Text("Uygula") }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }
        }

        // Özel profiller
        if (state.customProfiles.isNotEmpty()) {
            item {
                SectionCard("Özel Profiller", Icons.Default.Star,
                    badge = "${state.customProfiles.size}") {
                    state.customProfiles.forEach { profile ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${profile.cpuGovernor} · ${profile.gpuGovernor} · ${profile.touchPollingRate}Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (profile.description.isNotBlank()) {
                                    Text(profile.description, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                            TextButton(onClick = { vm.applyCustomProfile(profile) }) { Text("Uygula") }
                            IconButton(onClick = { vm.deleteCustomProfile(profile.id) },
                                modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                    }
                }
            }
        } else {
            item {
                EmptyState(Icons.Default.AddCircle, "Henüz özel profil yok",
                    "Yukarıdan yeni profil oluşturun")
            }
        }

        // Profil istatistikleri
        if (state.profileStats.isNotEmpty()) {
            item {
                SectionCard("Profil Kullanım İstatistikleri", Icons.Default.BarChart) {
                    val total = state.profileStats.values.sum().coerceAtLeast(1)
                    state.profileStats.entries.sortedByDescending { it.value }.forEach { (id, count) ->
                        val name = com.duchamp.control.PerformanceProfiles.presets.find { it.id == id }?.name
                            ?: state.customProfiles.find { it.id == id }?.name ?: id
                        val pct = count.toFloat() / total
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, style = MaterialTheme.typography.bodySmall)
                                Text("$count kez · ${(pct * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            UsageBar("", pct, MaterialTheme.colorScheme.primary, height = 4.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateProfileDialog(onDismiss: () -> Unit, onCreate: (CustomProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cpuGov by remember { mutableStateOf("schedutil") }
    var gpuGov by remember { mutableStateOf("simple_ondemand") }
    var polling by remember { mutableIntStateOf(120) }
    var swappiness by remember { mutableIntStateOf(60) }
    var tcp by remember { mutableStateOf("cubic") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Profil Oluştur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Profil Adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it },
                    label = { Text("Açıklama (isteğe bağlı)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("CPU Governor", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("schedutil", "performance", "powersave", "ondemand").forEach { g ->
                        FilterChip(selected = cpuGov == g, onClick = { cpuGov = g },
                            label = { Text(g, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Text("GPU Governor", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("simple_ondemand", "performance", "powersave").forEach { g ->
                        FilterChip(selected = gpuGov == g, onClick = { gpuGov = g },
                            label = { Text(g, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Text("Touch Polling Rate", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(60, 120, 240, 360).forEach { r ->
                        FilterChip(selected = polling == r, onClick = { polling = r },
                            label = { Text("${r}Hz", style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Text("TCP: $tcp · Swappiness: $swappiness",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onCreate(CustomProfile(UUID.randomUUID().toString(), name, cpuGov, gpuGov,
                        polling, swappiness, tcp, desc))
                }
            }, enabled = name.isNotBlank()) { Text("Oluştur") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
