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
import androidx.compose.ui.text.style.TextAlign
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
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PhoneAndroid, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("DimensityTool",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    Text("MT6897 · Poco X6 Pro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (state.isRooted) {
                                    StatusBadge("Root", MaterialTheme.colorScheme.primary)
                                }
                                state.rootInfo?.let {
                                    StatusBadge(it.rootType, MaterialTheme.colorScheme.secondary)
                                }
                                if (state.liveMonitoringActive) {
                                    StatusBadge("● Canlı", MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    Spacer(Modifier.height(8.dp))

                    // Menü grupları
                    drawerGroups.forEach { group ->
                        Text(
                            group.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                        group.screens.forEach { screen ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(screen.icon, null,
                                        modifier = Modifier.size(18.dp))
                                },
                                label = {
                                    Text(screen.title,
                                        style = MaterialTheme.typography.bodyMedium)
                                },
                                selected = currentScreen == screen,
                                onClick = {
                                    currentScreen = screen
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "DimensityTool v1.0 · by Sinan Aslan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(currentScreen.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu",
                                tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        if (!state.isRooted && !state.isLoading) {
                            Icon(Icons.Default.Warning, null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        val activeRules = state.scheduleRules.count { it.enabled }
                        if (activeRules > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("$activeRules")
                                }
                            }) {
                                IconButton(onClick = { currentScreen = Screen.Scheduler }) {
                                    Icon(Icons.Default.Schedule, "Scheduler",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        IconButton(onClick = { vm.loadAll() }) {
                            Icon(Icons.Default.Refresh, "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, screen.title, modifier = Modifier.size(20.dp)) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Cihaz bilgileri yükleniyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Screen.Benchmark    -> BenchmarkScreen(state, vm, m)
                Screen.AppProfile   -> AppProfileScreen(state, vm, m)
                Screen.BackupRestore-> BackupRestoreScreen(state, vm, m)
                Screen.SleepMode    -> SleepModeScreen(state, vm, m)
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
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Root Erişimi Gerekli",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(
                "Bu uygulama Magisk veya KernelSU ile root edilmiş cihazlarda çalışır.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
