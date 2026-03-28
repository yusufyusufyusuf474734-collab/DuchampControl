package com.duchamp.control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duchamp.control.AppState
import com.duchamp.control.LogcatLine
import com.duchamp.control.MainViewModel

@Composable
fun LogcatScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    var filterLevel by remember { mutableStateOf("ALL") }
    var filterTag by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filtered = state.logcatLines.filter { line ->
        (filterLevel == "ALL" || line.level == filterLevel) &&
        (filterTag.isEmpty() || line.tag.contains(filterTag, ignoreCase = true))
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = filterTag,
                onValueChange = { filterTag = it },
                label = { Text("Tag filtrele") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    if (filterTag.isNotEmpty()) {
                        IconButton(onClick = { filterTag = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
            Button(onClick = { vm.loadLogcat() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }

        // Level filtresi
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "E", "W", "I", "D", "V").forEach { level ->
                FilterChip(
                    selected = filterLevel == level,
                    onClick = { filterLevel = level },
                    label = {
                        Text(
                            when (level) {
                                "E" -> "Hata"; "W" -> "Uyarı"; "I" -> "Bilgi"
                                "D" -> "Debug"; "V" -> "Verbose"; else -> "Tümü"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = logLevelColor(level).copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "${filtered.size} satır",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        if (state.logcatLines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Terminal, contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("Logcat yüklemek için Yenile'ye basın",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.loadLogcat() }) { Text("Logcat Yükle") }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D)),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(filtered) { line ->
                    LogcatRow(line)
                }
            }
        }
    }
}

@Composable
fun LogcatRow(line: LogcatLine) {
    val color = logLevelColor(line.level)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = "[${line.level}]",
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.width(32.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = line.tag.take(20).padEnd(20),
            color = color.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.width(160.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = line.message,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
fun logLevelColor(level: String): Color = when (level) {
    "E" -> Color(0xFFFF5252)
    "W" -> Color(0xFFFFAB40)
    "I" -> Color(0xFF69F0AE)
    "D" -> Color(0xFF40C4FF)
    "V" -> Color(0xFFBDBDBD)
    else -> Color(0xFFEEEEEE)
}
