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
    val fpsOverlayEnabled: Boolean = false
)
