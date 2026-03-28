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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppInfo
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var showSystem by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    LaunchedEffect(showSystem) {
        if (state.installedApps.isEmpty() || showSystem) vm.loadApps(showSystem)
    }

    val filtered = state.installedApps.filter { app ->
        searchQuery.isEmpty() ||
        app.packageName.contains(searchQuery, ignoreCase = true) ||
        app.label.contains(searchQuery, ignoreCase = true)
    }

    // Detay dialog
    selectedApp?.let { app ->
        AppDetailDialog(
            app = app,
            onDismiss = { selectedApp = null },
            onForceStop = { vm.forceStopApp(app.packageName); selectedApp = null },
            onClearData = { vm.clearAppData(app.packageName); selectedApp = null },
            onDisable = { vm.disableApp(app.packageName); selectedApp = null },
            onEnable = { vm.enableApp(app.packageName); selectedApp = null },
            onUninstall = { vm.uninstallApp(app.packageName); selectedApp = null },
            onFreeze = { vm.freezeApp(app.packageName); selectedApp = null },
            onUnfreeze = { vm.unfreezeApp(app.packageName); selectedApp = null }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Arama + filtre toolbar
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Uygulama veya paket adı ara...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${filtered.size} uygulama",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.width(12.dp))
                    FilterChip(
                        selected = showSystem,
                        onClick = { showSystem = !showSystem },
                        label = { Text("Sistem Uygulamaları") },
                        leadingIcon = {
                            Icon(
                                if (showSystem) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                null, modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                if (state.appsLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { vm.loadApps(showSystem) }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (state.appsLoading && state.installedApps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Uygulamalar yükleniyor...")
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppListItem(app = app, onClick = { selectedApp = app })
            }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (!app.isEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon placeholder
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (app.isSystem)
                    MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (app.isSystem) Icons.Default.Android else Icons.Default.Apps,
                        null,
                        modifier = Modifier.size(22.dp),
                        tint = if (app.isSystem)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (!app.isEnabled)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (app.isSystem) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Sistem",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!app.isEnabled) {
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            "Devre Dışı",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onForceStop: () -> Unit,
    onClearData: () -> Unit,
    onDisable: () -> Unit,
    onEnable: () -> Unit,
    onUninstall: () -> Unit,
    onFreeze: () -> Unit,
    onUnfreeze: () -> Unit
) {
    var showUninstallConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showUninstallConfirm) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirm = false },
            title = { Text("Uygulamayı Kaldır") },
            text = { Text("${app.label} uygulaması kaldırılacak. Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = { showUninstallConfirm = false; onUninstall() }) {
                    Text("Kaldır", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirm = false }) { Text("İptal") }
            }
        )
        return
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Veriyi Temizle") },
            text = { Text("${app.label} uygulamasının tüm verisi silinecek. Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; onClearData() }) {
                    Text("Temizle", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("İptal") }
            }
        )
        return
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            // Başlık
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (app.isSystem) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (app.isSystem) Icons.Default.Android else Icons.Default.Apps,
                            null, modifier = Modifier.size(28.dp),
                            tint = if (app.isSystem) MaterialTheme.colorScheme.onSecondaryContainer
                                   else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Bilgiler
            InfoRow("Versiyon", app.versionName)
            InfoRow("Target SDK", app.targetSdk)
            InfoRow("Tür", if (app.isSystem) "Sistem" else "Kullanıcı")
            InfoRow("Durum", if (app.isEnabled) "Aktif" else "Devre Dışı")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Aksiyonlar - 2 sütun grid
            Text("İşlemler", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppActionButton(
                    icon = Icons.Default.Stop,
                    label = "Durdur",
                    onClick = onForceStop,
                    modifier = Modifier.weight(1f)
                )
                AppActionButton(
                    icon = Icons.Default.DeleteSweep,
                    label = "Veri Temizle",
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.weight(1f),
                    isDestructive = true
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (app.isEnabled) {
                    AppActionButton(
                        icon = Icons.Default.Block,
                        label = "Devre Dışı",
                        onClick = onDisable,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AppActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = "Etkinleştir",
                        onClick = onEnable,
                        modifier = Modifier.weight(1f)
                    )
                }
                AppActionButton(
                    icon = Icons.Default.AcUnit,
                    label = "Dondur",
                    onClick = onFreeze,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            if (!app.isSystem) {
                OutlinedButton(
                    onClick = { showUninstallConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Uygulamayı Kaldır")
                }
            }
        }
    }
}

@Composable
fun AppActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = if (isDestructive) ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ) else ButtonDefaults.outlinedButtonColors()
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
