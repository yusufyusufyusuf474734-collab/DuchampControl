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
import com.duchamp.control.DebloatApp
import com.duchamp.control.MainViewModel

// MIUI/HyperOS için önerilen debloat listesi
val miuiDebloatList = listOf(
    DebloatApp("com.miui.analytics",           "MIUI Analytics",         "Telemetri",  safe = true),
    DebloatApp("com.xiaomi.mipicks",            "GetApps Önerileri",      "Reklam",     safe = true),
    DebloatApp("com.miui.msa.global",           "MSA (Reklam Servisi)",   "Reklam",     safe = true),
    DebloatApp("com.miui.daemon",               "MIUI Daemon",            "Telemetri",  safe = true),
    DebloatApp("com.miui.bugreport",            "Hata Raporu",            "Sistem",     safe = true),
    DebloatApp("com.miui.cloudservice",         "Mi Cloud",               "Bulut",      safe = false),
    DebloatApp("com.miui.cloudbackup",          "Mi Cloud Yedek",         "Bulut",      safe = false),
    DebloatApp("com.xiaomi.finddevice",         "Cihazımı Bul",           "Güvenlik",   safe = false),
    DebloatApp("com.miui.videoplayer",          "Mi Video",               "Medya",      safe = true),
    DebloatApp("com.miui.player",               "Mi Müzik",               "Medya",      safe = true),
    DebloatApp("com.miui.fm",                   "FM Radyo",               "Medya",      safe = true),
    DebloatApp("com.miui.notes",                "Mi Notlar",              "Uygulama",   safe = true),
    DebloatApp("com.miui.calculator",           "Mi Hesap Makinesi",      "Uygulama",   safe = true),
    DebloatApp("com.miui.compass",              "Pusula",                 "Uygulama",   safe = true),
    DebloatApp("com.miui.weather",              "Mi Hava Durumu",         "Uygulama",   safe = true),
    DebloatApp("com.miui.cleanmaster",          "Temizleyici",            "Sistem",     safe = true),
    DebloatApp("com.miui.securitycenter",       "Güvenlik",               "Sistem",     safe = false),
    DebloatApp("com.miui.yellowpage",           "Sarı Sayfalar",          "Uygulama",   safe = true),
    DebloatApp("com.miui.contentextension",     "İçerik Uzantısı",        "Sistem",     safe = true),
    DebloatApp("com.miui.systemAdSolution",     "Sistem Reklam Çözümü",   "Reklam",     safe = true),
    DebloatApp("com.miui.global.GenericService","Global Servis",          "Telemetri",  safe = true),
    DebloatApp("com.miui.personalassistant",    "Kişisel Asistan",        "Sistem",     safe = true),
    DebloatApp("com.miui.hybrid",               "Hybrid Servisi",         "Sistem",     safe = true),
    DebloatApp("com.miui.translation",          "Çeviri",                 "Uygulama",   safe = true),
    DebloatApp("com.miui.screenrecorder",       "Ekran Kaydedici",        "Sistem",     safe = true)
)

@Composable
fun DebloatScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var showOnlySafe by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadDebloatStatus() }

    val categories = listOf("Tümü", "Reklam", "Telemetri", "Medya", "Uygulama", "Bulut", "Sistem")

    val filtered = miuiDebloatList.filter { app ->
        (selectedCategory == "Tümü" || app.category == selectedCategory) &&
        (!showOnlySafe || app.safe) &&
        (search.isBlank() || app.label.contains(search, ignoreCase = true) ||
         app.packageName.contains(search, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Güvensiz olarak işaretli uygulamaları devre dışı bırakmak sistemi bozabilir. " +
                         "Yalnızca 'Güvenli' olanları kaldırmanız önerilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Uygulama ara...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { cat ->
                        FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = showOnlySafe, onClick = { showOnlySafe = !showOnlySafe },
                    label = { Text("Güvenli", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp)) })
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${filtered.size} uygulama",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { vm.disableAllDebloat(filtered.filter { it.safe }) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Block, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tümünü Devre Dışı", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        items(filtered, key = { it.packageName }) { app ->
            val isDisabled = state.debloatList.find { it.packageName == app.packageName }?.disabled
                ?: false

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDisabled)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(app.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(6.dp))
                            StatusBadge(app.category,
                                if (app.category == "Reklam" || app.category == "Telemetri")
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.secondary)
                            if (!app.safe) {
                                Spacer(Modifier.width(4.dp))
                                StatusBadge("Dikkat", Color(0xFFF59E0B))
                            }
                        }
                        Text(app.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isDisabled) {
                        OutlinedButton(
                            onClick = { vm.enableDebloatApp(app.packageName) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary)
                        ) { Text("Etkinleştir", style = MaterialTheme.typography.labelSmall) }
                    } else {
                        OutlinedButton(
                            onClick = { vm.disableDebloatApp(app.packageName) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Devre Dışı", style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}
