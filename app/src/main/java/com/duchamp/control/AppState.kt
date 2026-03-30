package com.duchamp.control

data class AppState(
    val isRooted: Boolean = false,
    val isLoading: Boolean = true,
    val deviceBasic: Map<String, String> = emptyMap(),
    val cpuInfo: CpuInfo? = null,
    val gpuInfo: GpuInfo? = null,
    val batteryInfo: BatteryInfo? = null,
    val thermals: List<ThermalInfo> = emptyList(),
    val memInfo: MemInfo? = null,
    val storageInfo: List<StorageInfo> = emptyList(),
    val networkInfo: NetworkInfo? = null,
    val displayInfo: DisplayInfo? = null,
    val audioInfo: AudioInfo? = null,
    val systemInfo: SystemInfo? = null,
    val touchPollingRate: String = "N/A",
    val ioScheduler: String = "N/A",
    val availableIoSchedulers: List<String> = emptyList(),
    val kernelModules: List<String> = emptyList(),
    val logcatLines: List<LogcatLine> = emptyList(),
    val nfcEnabled: Boolean = false,
    val rootInfo: RootInfo? = null,
    val kernelTweaks: KernelTweaks? = null,
    val activeProfileId: String = "balanced",
    val cpuHistory: List<LiveMetric> = emptyList(),
    val gpuHistory: List<LiveMetric> = emptyList(),
    val ramHistory: List<LiveMetric> = emptyList(),
    val batteryHistory: List<LiveMetric> = emptyList(),
    val cpuTempHistory: List<LiveMetric> = emptyList(),
    val liveMonitoringActive: Boolean = false,
    val installedApps: List<AppInfo> = emptyList(),
    val appsLoading: Boolean = false,
    val dozeWhitelist: List<String> = emptyList(),
    val dozeEnabled: Boolean = false,
    // Güvenlik
    val securityInfo: SecurityInfo? = null,
    // Tema & Kişiselleştirme
    val appTheme: AppTheme = AppTheme.DARK,
    val accentColorIndex: Int = 0,
    val dashboardCompact: Boolean = false,
    // Zamanlayıcı
    val scheduleRules: List<ScheduleRule> = emptyList(),
    // KernelKit modül durumu
    val kernelKitInstalled: Boolean = false,
    val kernelKitEnabled: Boolean = false,
    val statusMessage: String = "",
    // Uygulama başına profil
    val appProfiles: Map<String, String> = emptyMap(),
    // Uyku modu
    val sleepModeEnabled: Boolean = false,
    val sleepProfileId: String = "powersave",
    val wakeProfileId: String = "balanced",
    // Termal throttle
    val thermalThrottleEnabled: Boolean = false,
    val thermalThrottleTempC: Int = 75,
    val thermalThrottleProfileId: String = "powersave",
    // MTK EAS/HMP
    val mtkEasInfo: MtkEasInfo? = null,
    // Şarj geçmişi
    val chargeHistory: List<LiveMetric> = emptyList(),
    val voltageHistory: List<LiveMetric> = emptyList(),
    // Ağ hız geçmişi
    val netRxHistory: List<LiveMetric> = emptyList(),
    val netTxHistory: List<LiveMetric> = emptyList(),
    // Firewall
    val firewallRules: List<FirewallRule> = emptyList(),
    // VPN
    val vpnInfo: VpnInfo? = null,
    // FPS overlay
    val fpsOverlayEnabled: Boolean = false,
    // Oyun modu
    val gameModeEnabled: Boolean = false,
    val gameApps: Set<String> = emptySet(),
    // Termal bildirim
    val thermalAlertEnabled: Boolean = false,
    val thermalAlertTempC: Int = 75,
    // Önyükleme scripti
    val bootScriptExists: Boolean = false,
    val bootScriptContent: String = "",
    // Prop editörü
    val allProps: Map<String, String> = emptyMap(),
    // Sistem bilgisi paylaşımı
    val systemShareText: String = "",
    // Pil bildirimi
    val chargeNotifyEnabled: Boolean = false,
    val chargeNotifyPct: Int = 80,
    // Gece şarj modu
    val nightChargeEnabled: Boolean = false,
    val nightChargeStartHour: Int = 23,
    val nightChargeEndHour: Int = 7,
    val nightChargeLimitPct: Int = 80,
    // Ağ hız testi
    val pingResults: Map<String, String> = emptyMap(),
    val pingRunning: Boolean = false,
    // APK yedekleme
    val apkBackupStatus: String = "",
    // Uygulama boyut analizi
    val appSizeList: List<Pair<String, Long>> = emptyList(),
    // Özel profiller
    val customProfiles: List<CustomProfile> = emptyList(),
    // Profil istatistikleri
    val profileStats: Map<String, Int> = emptyMap(),
    // Debloat
    val debloatList: List<DebloatApp> = emptyList(),
    // Kernel parametreleri
    val kernelParams: List<KernelParam> = emptyList(),
    // Dmesg
    val dmesgLines: List<String> = emptyList(),
    // Reboot menüsü
    val rebootMenuVisible: Boolean = false,
    // Quick Tile
    val quickTileProfileId: String = "balanced",
    // Görev yöneticisi
    val processList: List<ProcessInfo> = emptyList(),
    val processLoading: Boolean = false,
    // Hız testi
    val speedTestResult: SpeedTestResult? = null,
    val speedTestRunning: Boolean = false,
    // Wi-Fi analizi
    val wifiNetworks: List<WifiNetwork> = emptyList(),
    val wifiScanRunning: Boolean = false,
    // Pil sağlığı skoru
    val batteryHealthScore: BatteryHealthScore? = null,
    // Stres testi
    val stressTestRunning: Boolean = false,
    val stressTestLog: List<StressLogEntry> = emptyList()
)
