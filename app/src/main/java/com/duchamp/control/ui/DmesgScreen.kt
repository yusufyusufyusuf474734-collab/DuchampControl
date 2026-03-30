package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun DmesgScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var filter by remember { mutableStateOf("") }
    var levelFilter by remember { mutableStateOf("Tümü") }
    val listState = rememberLazyListState()

    val levels = listOf("Tümü", "err", "warn", "info")

    val filtered = state.dmesgLines.filter { line ->
        (levelFilter == "Tümü" ||
         (levelFilter == "err"  && (line.contains("error", ignoreCase = true) || line.contains("fail", ignoreCase = true))) ||
         (levelFilter == "warn" && line.contains("warn", ignoreCase = true)) ||
         (levelFilter == "info" && !line.contains("error", ignoreCase = true) && !line.contains("warn", ignoreCase = true))) &&
        (filter.isBlank() || line.contains(filter, ignoreCase = true))
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filter, onValueChange = { filter = it },
                    placeholder = { Text("Filtrele...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f), singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vm.loadDmesg() }) {
                    Icon(Icons.Default.Refresh, null)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                levels.forEach { l ->
                    FilterChip(selected = levelFilter == l, onClick = { levelFilter = l },
                        label = { Text(l, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (l) {
                                "err"  -> MaterialTheme.colorScheme.errorContainer
                                "warn" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else   -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("${filtered.size} satır",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically))
            }
        }

        if (state.dmesgLines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyState(Icons.Default.Terminal, "Dmesg yüklenmedi", "Yükle butonuna tıklayın")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.loadDmesg() }) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Dmesg Yükle")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(filtered) { line ->
                    val color = when {
                        line.contains("error", ignoreCase = true) || line.contains("fail", ignoreCase = true) ->
                            MaterialTheme.colorScheme.error
                        line.contains("warn", ignoreCase = true) -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        line,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = color,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
