package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duchamp.control.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuchampApp(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        if (state.statusMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(state.statusMessage, duration = SnackbarDuration.Short)
            vm.clearStatus()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(290.dp)) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhoneAndroid, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("DimensityTool", style = MaterialTheme.typography.titleMedium)
                            Text("by Sinan Aslan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                            Text("Poco X6 Pro / Redmi K70E",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (state.isRooted) {
                            Surface(shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, null, Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Root Aktif", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                        state.rootInfo?.let { ri ->
                            Surface(shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(ri.rootType, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        if (state.liveMonitoringActive) {
                            Surface(shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Circle, null, Modifier.size(8.dp),
                                        tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Canlı", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    drawerGroups.forEach { group ->
                        Text(group.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        group.screens.forEach { screen ->
                            NavigationDrawerItem(
                                icon = { Icon(screen.icon, null, modifier = Modifier.size(20.dp)) },
                                label = { Text(screen.title, style = MaterialTheme.typography.bodyMedium) },
                                selected = currentScreen == screen,
                                onClick = {
                                    currentScreen = screen
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "© Sinan Aslan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Text(
                        "DimensityTool v1.0 · MT6897",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        if (!state.isRooted && !state.isLoading) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(4.dp))
                        }
                        val activeRules = state.scheduleRules.count { it.enabled }
                        if (activeRules > 0) {
                            BadgedBox(badge = { Badge { Text("$activeRules") } }) {
                                IconButton(onClick = { currentScreen = Screen.Scheduler }) {
                                    Icon(Icons.Default.Schedule, "Scheduler")
                                }
                            }
                        }
                        IconButton(onClick = { vm.loadAll() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, screen.title, modifier = Modifier.size(22.dp)) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cihaz bilgileri yükleniyor...")
                    }
                }
                return@Scaffold
            }
            if (!state.isRooted) {
                NoRootScreen(Modifier.padding(padding))
                return@Scaffold
            }
            val m = Modifier.padding(padding)
            when (currentScreen) {
                Screen.Dashboard    -> DashboardScreen(state, vm, m)
                Screen.LiveMonitor  -> LiveMonitorScreen(state, vm, m)
                Screen.Profiles     -> ProfilesScreen(state, vm, m)
                Screen.Cpu          -> CpuScreen(state, vm, m)
                Screen.Battery      -> BatteryScreen(state, vm, m)
                Screen.Thermal      -> ThermalScreen(state, vm, m)
                Screen.Memory       -> MemoryScreen(state, vm, m)
                Screen.Display      -> DisplayScreen(state, vm, m)
                Screen.Audio        -> AudioScreen(state, vm, m)
                Screen.Network      -> NetworkScreen(state, vm, m)
                Screen.Touch        -> TouchScreen(state, vm, m)
                Screen.KernelTweaks -> KernelTweaksScreen(state, vm, m)
                Screen.Magisk       -> MagiskScreen(state, vm, m)
                Screen.AppManager   -> AppManagerScreen(state, vm, m)
                Screen.Doze         -> DozeScreen(state, vm, m)
                Screen.Security     -> SecurityScreen(state, vm, m)
                Screen.Scheduler    -> SchedulerScreen(state, vm, m)
                Screen.Appearance   -> AppearanceScreen(state, vm, m)
                Screen.System       -> SystemScreen(state, vm, m)
                Screen.Hardware     -> HardwareScreen(state, vm, m)
                Screen.Logcat       -> LogcatScreen(state, vm, m)
                Screen.About        -> AboutScreen(m)
                else                -> DashboardScreen(state, vm, m)
            }
        }
    }
}

@Composable
fun NoRootScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text("Root Erişimi Gerekli", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Bu uygulama Magisk veya KernelSU ile root edilmiş cihazlarda çalışır.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
