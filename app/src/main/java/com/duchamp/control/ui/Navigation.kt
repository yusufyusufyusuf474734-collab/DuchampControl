package com.duchamp.control.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard     : Screen("dashboard",  "Genel Bakış",     Icons.Default.Dashboard)
    object LiveMonitor   : Screen("live",        "Canlı İzleme",    Icons.Default.ShowChart)
    object Profiles      : Screen("profiles",    "Profiller",       Icons.Default.Tune)
    object Cpu           : Screen("cpu",         "CPU / GPU",       Icons.Default.Memory)
    object Battery       : Screen("battery",     "Batarya",         Icons.Default.BatteryFull)
    object Thermal       : Screen("thermal",     "Termal",          Icons.Default.Thermostat)
    object Memory        : Screen("memory",      "Bellek",          Icons.Default.Storage)
    object Display       : Screen("display",     "Ekran",           Icons.Default.Tv)
    object Audio         : Screen("audio",       "Ses",             Icons.Default.VolumeUp)
    object Network       : Screen("network",     "Ağ",              Icons.Default.Wifi)
    object Touch         : Screen("touch",       "Dokunmatik",      Icons.Default.TouchApp)
    object KernelTweaks  : Screen("kernel",      "Kernel Tweaks",   Icons.Default.Code)
    object Magisk        : Screen("magisk",      "Magisk / KSU",    Icons.Default.Security)
    object AppManager    : Screen("apps",        "Uygulamalar",     Icons.Default.Apps)
    object AppProfile    : Screen("appprofile",  "Uygulama Profil", Icons.Default.AppSettingsAlt)
    object Doze          : Screen("doze",        "Doze / Pil",      Icons.Default.BatterySaver)
    object Security      : Screen("security",    "Güvenlik",        Icons.Default.Shield)
    object Scheduler     : Screen("scheduler",   "Zamanlayıcı",     Icons.Default.Schedule)
    object Benchmark     : Screen("benchmark",   "Benchmark",       Icons.Default.Speed)
    object BackupRestore : Screen("backup",      "Yedek / Geri Yükle", Icons.Default.Backup)
    object SleepMode     : Screen("sleep",       "Uyku Modu",       Icons.Default.Bedtime)
    object GameMode      : Screen("gamemode",    "Oyun Modu",       Icons.Default.SportsEsports)
    object BootScript    : Screen("bootscript",  "Önyükleme Scripti", Icons.Default.Code)
    object Appearance    : Screen("appearance",  "Görünüm",         Icons.Default.Palette)
    object System        : Screen("system",      "Sistem",          Icons.Default.Settings)
    object Hardware      : Screen("hardware",    "Donanım",         Icons.Default.Hardware)
    object Logcat        : Screen("logcat",      "Logcat",          Icons.Default.Terminal)
    object About         : Screen("about",       "Hakkında",        Icons.Default.Info)
}

val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.LiveMonitor,
    Screen.Profiles,
    Screen.Cpu,
    Screen.AppManager
)

data class DrawerGroup(val title: String, val screens: List<Screen>)

val drawerGroups = listOf(
    DrawerGroup("Ana", listOf(
        Screen.Dashboard,
        Screen.LiveMonitor,
        Screen.Profiles,
        Screen.Scheduler,
        Screen.Benchmark
    )),
    DrawerGroup("Donanım", listOf(
        Screen.Cpu,
        Screen.Battery,
        Screen.Thermal,
        Screen.Memory,
        Screen.Display,
        Screen.Audio,
        Screen.Touch
    )),
    DrawerGroup("Uygulamalar", listOf(
        Screen.AppManager,
        Screen.AppProfile,
        Screen.Doze
    )),
    DrawerGroup("Sistem & Güvenlik", listOf(
        Screen.Security,
        Screen.Network,
        Screen.KernelTweaks,
        Screen.Magisk,
        Screen.System,
        Screen.Hardware,
        Screen.Logcat
    )),
    DrawerGroup("Araçlar", listOf(
        Screen.GameMode,
        Screen.BootScript,
        Screen.BackupRestore,
        Screen.SleepMode
    )),
    DrawerGroup("Kişiselleştirme", listOf(
        Screen.Appearance,
        Screen.About
    ))
)
