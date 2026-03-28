package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun KernelTweaksScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    val kt = state.kernelTweaks

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Warning, null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Yanlış değerler sistem kararsızlığına yol açabilir. Değiştirmeden önce araştırın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // VM Tweaks
        item {
            SectionCard("VM (Sanal Bellek)", Icons.Default.Memory) {
                TweakRow(
                    label = "dirty_ratio",
                    description = "Kirli sayfa yazma eşiği (%)",
                    value = kt?.dirtyRatio ?: "N/A",
                    sysfsPath = "/proc/sys/vm/dirty_ratio",
                    presets = listOf("10", "20", "30", "40"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "dirty_background_ratio",
                    description = "Arka plan yazma eşiği (%)",
                    value = kt?.dirtyBgRatio ?: "N/A",
                    sysfsPath = "/proc/sys/vm/dirty_background_ratio",
                    presets = listOf("5", "10", "15", "20"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "overcommit_memory",
                    description = "0=heuristic, 1=always, 2=never",
                    value = kt?.vmOvercommit ?: "N/A",
                    sysfsPath = "/proc/sys/vm/overcommit_memory",
                    presets = listOf("0", "1", "2"),
                    vm = vm
                )
            }
        }

        // Ağ Tweaks
        item {
            SectionCard("Ağ Tamponu", Icons.Default.NetworkCheck) {
                TweakRow(
                    label = "rmem_max",
                    description = "Maksimum alım tamponu (byte)",
                    value = kt?.rmemMax ?: "N/A",
                    sysfsPath = "/proc/sys/net/core/rmem_max",
                    presets = listOf("4194304", "8388608", "16777216", "33554432"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "wmem_max",
                    description = "Maksimum gönderim tamponu (byte)",
                    value = kt?.wmemMax ?: "N/A",
                    sysfsPath = "/proc/sys/net/core/wmem_max",
                    presets = listOf("4194304", "8388608", "16777216", "33554432"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "tcp_fastopen",
                    description = "0=kapalı, 1=istemci, 2=sunucu, 3=her ikisi",
                    value = kt?.tcpFastOpen ?: "N/A",
                    sysfsPath = "/proc/sys/net/ipv4/tcp_fastopen",
                    presets = listOf("0", "1", "2", "3"),
                    vm = vm
                )
            }
        }

        // Kernel Tweaks
        item {
            SectionCard("Kernel", Icons.Default.Code) {
                TweakRow(
                    label = "perf_event_paranoid",
                    description = "-1=hepsi, 0=root, 1=kısıtlı, 2=yok",
                    value = kt?.perfEventParanoid ?: "N/A",
                    sysfsPath = "/proc/sys/kernel/perf_event_paranoid",
                    presets = listOf("-1", "0", "1", "2"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "sched_child_runs_first",
                    description = "Fork sonrası çocuk önce çalışsın (0/1)",
                    value = kt?.schedChildRunsFirst ?: "N/A",
                    sysfsPath = "/proc/sys/kernel/sched_child_runs_first",
                    presets = listOf("0", "1"),
                    vm = vm
                )
                SectionDivider()
                TweakRow(
                    label = "randomize_va_space",
                    description = "ASLR: 0=kapalı, 1=kısmi, 2=tam",
                    value = kt?.randomizeVaSpace ?: "N/A",
                    sysfsPath = "/proc/sys/kernel/randomize_va_space",
                    presets = listOf("0", "1", "2"),
                    vm = vm
                )
            }
        }

        // Dosya Sistemi
        item {
            SectionCard("Dosya Sistemi", Icons.Default.Folder) {
                TweakRow(
                    label = "inotify max_user_watches",
                    description = "İzlenebilecek maksimum dosya sayısı",
                    value = kt?.inotifyMaxWatches ?: "N/A",
                    sysfsPath = "/proc/sys/fs/inotify/max_user_watches",
                    presets = listOf("8192", "16384", "65536", "131072"),
                    vm = vm
                )
            }
        }

        // Hızlı uygulama presetleri
        item {
            SectionCard("Hızlı Presetler", Icons.Default.FlashOn) {
                Text("Önceden tanımlı tweak setlerini tek tıkla uygula.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        vm.setKernelTweak("/proc/sys/vm/dirty_ratio", "10", "dirty_ratio")
                        vm.setKernelTweak("/proc/sys/vm/dirty_background_ratio", "5", "dirty_background_ratio")
                        vm.setKernelTweak("/proc/sys/net/ipv4/tcp_fastopen", "3", "tcp_fastopen")
                        vm.setKernelTweak("/proc/sys/net/core/rmem_max", "16777216", "rmem_max")
                        vm.setKernelTweak("/proc/sys/net/core/wmem_max", "16777216", "wmem_max")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Speed, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Performans Seti Uygula")
                }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        vm.setKernelTweak("/proc/sys/vm/dirty_ratio", "40", "dirty_ratio")
                        vm.setKernelTweak("/proc/sys/vm/dirty_background_ratio", "20", "dirty_background_ratio")
                        vm.setKernelTweak("/proc/sys/net/ipv4/tcp_fastopen", "1", "tcp_fastopen")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BatterySaver, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pil Tasarrufu Seti Uygula")
                }
            }
        }
    }
}

@Composable
fun TweakRow(
    label: String,
    description: String,
    value: String,
    sysfsPath: String,
    presets: List<String>,
    vm: MainViewModel
) {
    var customVal by remember(value) { mutableStateOf(value) }
    var showCustom by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(value, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.forEach { preset ->
                FilterChip(
                    selected = value == preset,
                    onClick = { vm.setKernelTweak(sysfsPath, preset, label) },
                    label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                )
            }
            FilterChip(
                selected = showCustom,
                onClick = { showCustom = !showCustom },
                label = { Text("Özel", style = MaterialTheme.typography.labelSmall) }
            )
        }
        if (showCustom) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customVal,
                    onValueChange = { customVal = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Değer") }
                )
                Button(onClick = {
                    vm.setKernelTweak(sysfsPath, customVal, label)
                    showCustom = false
                }) { Text("Uygula") }
            }
        }
    }
}
