package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.duchamp.control.PerformanceProfiles
import com.duchamp.control.ScheduleRule
import com.duchamp.control.ScheduleTrigger
import java.util.UUID

@Composable
fun SchedulerScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { rule ->
                vm.addScheduleRule(rule)
                showAddDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Açıklama
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Otomatik Profil Zamanlayıcı",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Belirli saat, şarj durumu veya sıcaklık eşiğinde profil otomatik uygulanır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Kural ekle butonu
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Yeni Kural Ekle")
            }
        }

        // Kurallar
        if (state.scheduleRules.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Schedule, null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                            Spacer(Modifier.height(10.dp))
                            Text("Henüz kural yok",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("Yukarıdan yeni kural ekleyin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                        }
                    }
                }
            }
        } else {
            items(state.scheduleRules, key = { it.id }) { rule ->
                ScheduleRuleCard(
                    rule = rule,
                    onToggle = { vm.toggleScheduleRule(rule.id, it) },
                    onDelete = { vm.deleteScheduleRule(rule.id) }
                )
            }
        }

        // Örnek kurallar
        item {
            SectionCard("Örnek Kurallar", Icons.Default.Lightbulb) {
                Text("Hızlı başlangıç için hazır kurallar:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                val examples = listOf(
                    Triple("Gece Pil Tasarrufu", ScheduleTrigger.TIME,
                        ScheduleRule(UUID.randomUUID().toString(), "Gece Pil Tasarrufu",
                            true, ScheduleTrigger.TIME, 23, 0, profileId = "powersave")),
                    Triple("Sabah Dengeli", ScheduleTrigger.TIME,
                        ScheduleRule(UUID.randomUUID().toString(), "Sabah Dengeli",
                            true, ScheduleTrigger.TIME, 7, 0, profileId = "balanced")),
                    Triple("Şarjda Performans", ScheduleTrigger.CHARGING,
                        ScheduleRule(UUID.randomUUID().toString(), "Şarjda Performans",
                            true, ScheduleTrigger.CHARGING, onCharging = true, profileId = "performance")),
                    Triple("Sıcaklık Koruması", ScheduleTrigger.TEMPERATURE,
                        ScheduleRule(UUID.randomUUID().toString(), "Sıcaklık Koruması",
                            true, ScheduleTrigger.TEMPERATURE, tempThresholdC = 70, profileId = "powersave"))
                )

                examples.forEach { (name, trigger, rule) ->
                    val alreadyExists = state.scheduleRules.any { it.name == name }
                    OutlinedButton(
                        onClick = { if (!alreadyExists) vm.addScheduleRule(rule) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        enabled = !alreadyExists
                    ) {
                        Icon(triggerIcon(trigger), null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodySmall)
                        if (alreadyExists) {
                            Spacer(Modifier.weight(1f))
                            Text("Eklendi", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleRuleCard(rule: ScheduleRule, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val profile = PerformanceProfiles.presets.find { it.id == rule.profileId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    triggerIcon(rule.trigger), null,
                    tint = if (rule.enabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.bodyMedium,
                        color = if (rule.enabled) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        triggerDescription(rule),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(checked = rule.enabled, onCheckedChange = onToggle)
            }

            if (profile != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(profileIcon(profile.id), null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("→ ${profile.name}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddRuleDialog(onDismiss: () -> Unit, onAdd: (ScheduleRule) -> Unit) {
    var name by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf(ScheduleTrigger.TIME) }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var onCharging by remember { mutableStateOf(true) }
    var tempThreshold by remember { mutableIntStateOf(70) }
    var profileId by remember { mutableStateOf("balanced") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Kural") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Kural Adı") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                // Tetikleyici seçimi
                Text("Tetikleyici", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScheduleTrigger.values().forEach { t ->
                        FilterChip(
                            selected = trigger == t,
                            onClick = { trigger = t },
                            label = { Text(triggerLabel(t), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(triggerIcon(t), null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                // Tetikleyici parametreleri
                when (trigger) {
                    ScheduleTrigger.TIME -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hour.toString(),
                                onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                                label = { Text("Saat (0-23)") },
                                modifier = Modifier.weight(1f), singleLine = true
                            )
                            OutlinedTextField(
                                value = minute.toString(),
                                onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                                label = { Text("Dakika (0-59)") },
                                modifier = Modifier.weight(1f), singleLine = true
                            )
                        }
                    }
                    ScheduleTrigger.CHARGING -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = onCharging, onClick = { onCharging = true },
                                label = { Text("Şarj Başlayınca") })
                            FilterChip(selected = !onCharging, onClick = { onCharging = false },
                                label = { Text("Şarj Bitince") })
                        }
                    }
                    ScheduleTrigger.TEMPERATURE -> {
                        OutlinedTextField(
                            value = tempThreshold.toString(),
                            onValueChange = { tempThreshold = it.toIntOrNull()?.coerceIn(40, 100) ?: tempThreshold },
                            label = { Text("Sıcaklık Eşiği (°C)") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    }
                }

                // Profil seçimi
                Text("Uygulanacak Profil", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerformanceProfiles.presets.forEach { p ->
                        FilterChip(
                            selected = profileId == p.id,
                            onClick = { profileId = p.id },
                            label = { Text(p.name, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(profileIcon(p.id), null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(ScheduleRule(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            enabled = true,
                            trigger = trigger,
                            hour = hour,
                            minute = minute,
                            onCharging = onCharging,
                            tempThresholdC = tempThreshold,
                            profileId = profileId
                        ))
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

fun triggerIcon(trigger: ScheduleTrigger) = when (trigger) {
    ScheduleTrigger.TIME        -> Icons.Default.AccessTime
    ScheduleTrigger.CHARGING    -> Icons.Default.BatteryChargingFull
    ScheduleTrigger.TEMPERATURE -> Icons.Default.Thermostat
}

fun triggerLabel(trigger: ScheduleTrigger) = when (trigger) {
    ScheduleTrigger.TIME        -> "Saat"
    ScheduleTrigger.CHARGING    -> "Şarj"
    ScheduleTrigger.TEMPERATURE -> "Sıcaklık"
}

fun triggerDescription(rule: ScheduleRule) = when (rule.trigger) {
    ScheduleTrigger.TIME ->
        "Her gün %02d:%02d".format(rule.hour, rule.minute)
    ScheduleTrigger.CHARGING ->
        if (rule.onCharging) "Şarj başlayınca" else "Şarj bitince"
    ScheduleTrigger.TEMPERATURE ->
        "CPU ≥ ${rule.tempThresholdC}°C olunca"
}
