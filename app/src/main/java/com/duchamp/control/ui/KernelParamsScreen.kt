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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.KernelParam
import com.duchamp.control.MainViewModel

@Composable
fun KernelParamsScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("Tümü") }

    LaunchedEffect(Unit) { vm.loadKernelParams() }

    val groups = listOf("Tümü", "vm", "kernel", "net", "fs")

    val filtered = state.kernelParams.filter { p ->
        (selectedGroup == "Tümü" || p.path.contains("/$selectedGroup/")) &&
        (search.isBlank() || p.name.contains(search, ignoreCase = true) ||
         p.value.contains(search, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Parametre ara...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (state.kernelParams.isEmpty()) {
                        IconButton(onClick = { vm.loadKernelParams() }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        item {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(groups) { g ->
                    FilterChip(selected = selectedGroup == g, onClick = { selectedGroup = g },
                        label = { Text(g, style = MaterialTheme.typography.labelSmall) })
                }
            }
        }

        if (state.kernelParams.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kernel parametrelerini yüklemek için butona tıklayın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { vm.loadKernelParams() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Parametreleri Yükle")
                        }
                    }
                }
            }
        } else {
            item {
                Text("${filtered.size} parametre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(filtered, key = { it.path }) { param ->
                KernelParamItem(param = param, onEdit = { newVal ->
                    vm.setKernelTweak(param.path, newVal, param.name)
                })
            }
        }
    }
}

@Composable
fun KernelParamItem(param: KernelParam, onEdit: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var editVal by remember { mutableStateOf(param.value) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(param.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(param.path,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!editing) {
                    Surface(shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(param.value,
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = { editing = true; editVal = param.value },
                        modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (editing) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = editVal, onValueChange = { editVal = it },
                        modifier = Modifier.weight(1f), singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    IconButton(onClick = { onEdit(editVal); editing = false },
                        modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { editing = false },
                        modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
