package com.duchamp.control

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state
    private var monitorJob: Job? = null
    private var schedulerJob: Job? = null
    private val maxHistory = 60

    init {
        AppPrefs.init(context)
        _state.value = _state.value.copy(
            appTheme = AppPrefs.theme,
            accentColorIndex = AppPrefs.accentColorIndex,
            dashboardCompact = AppPrefs.dashboardCompact,
            scheduleRules = AppPrefs.loadScheduleRules(),
            appProfiles = AppPrefs.loadAppProfiles(),
            sleepModeEnabled = AppPrefs.sleepModeEnabled,
            sleepProfileId = AppPrefs.sleepProfileId,
            wakeProfileId = AppPrefs.wakeProfileId,
            gameModeEnabled = AppPrefs.gameModeEnabled,
            gameApps = AppPrefs.loadGameApps(),
            thermalAlertEnabled = AppPrefs.thermalAlertEnabled,
            thermalAlertTempC = AppPrefs.thermalAlertTempC,
            kernelKitInstalled = KernelKitInstaller.isInstalled(),
            kernelKitEnabled = KernelKitInstaller.isEnabled()
        )
        loadAll()
        startScheduler()
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(context.applicationContext) as T
        }
    }

    fun loadAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoading = true)
            val rooted = RootUtils.isRooted()
            if (!rooted) {
                _state.value = AppState(isRooted = false, isLoading = false,
                    deviceBasic = DeviceInfo.getDeviceBasic())
                return@launch
            }
            _state.value = AppState(
                isRooted = true,
                isLoading = false,
                deviceBasic = DeviceInfo.getDeviceBasic(),
                cpuInfo = DeviceInfo.getCpuInfo(),
                gpuInfo = DeviceInfo.getGpuInfo(),
                batteryInfo = DeviceInfo.getBatteryInfo(),
                thermals = DeviceInfo.getThermals(),
                memInfo = DeviceInfo.getMemInfo(),
                storageInfo = DeviceInfo.getStorageInfo(),
                networkInfo = DeviceInfo.getNetworkInfo(),
                displayInfo = DeviceInfo.getDisplayInfo(),
                audioInfo = DeviceInfo.getAudioInfo(),
                systemInfo = DeviceInfo.getSystemInfo(),
                touchPollingRate = DeviceInfo.getTouchPollingRate(),
                ioScheduler = DeviceInfo.getIoScheduler(),
                availableIoSchedulers = DeviceInfo.getAvailableIoSchedulers(),
                kernelModules = DeviceInfo.getKernelModules(),
                nfcEnabled = DeviceInfo.getNfcEnabled(),
                rootInfo = MagiskInfo.getRootInfo(),
                kernelTweaks = KernelTweakInfo.get(),
                appTheme = AppPrefs.theme,
                accentColorIndex = AppPrefs.accentColorIndex,
                dashboardCompact = AppPrefs.dashboardCompact,
                scheduleRules = AppPrefs.loadScheduleRules()
            )
        }
    }

    private fun update(block: suspend () -> AppState) {
        viewModelScope.launch(Dispatchers.IO) { _state.value = block() }
    }

    // CPU / GPU
    fun setCpuGovernor(gov: String) = update {
        DeviceInfo.setCpuGovernor(gov)
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), statusMessage = "CPU governor: $gov")
    }
    fun setCpuMaxFreq(freqKhz: String) = update {
        DeviceInfo.setCpuMaxFreq(freqKhz)
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), statusMessage = "CPU max frekans ayarlandı")
    }
    fun setCpuMinFreq(freqKhz: String) = update {
        DeviceInfo.setCpuMinFreq(freqKhz)
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), statusMessage = "CPU min frekans ayarlandı")
    }
    fun setCpuMinFreqCluster(cluster: String, freqKhz: String) = update {
        when (cluster) {
            "little" -> DeviceInfo.setCpuMinFreqCluster(freqKhz, _state.value.cpuInfo?.clusterBig?.maxFreqKhz ?: freqKhz, _state.value.cpuInfo?.clusterPrime?.maxFreqKhz ?: freqKhz)
            "big"    -> DeviceInfo.setCpuMinFreqCluster(_state.value.cpuInfo?.clusterLittle?.maxFreqKhz ?: freqKhz, freqKhz, _state.value.cpuInfo?.clusterPrime?.maxFreqKhz ?: freqKhz)
            "prime"  -> DeviceInfo.setCpuMinFreqCluster(_state.value.cpuInfo?.clusterLittle?.maxFreqKhz ?: freqKhz, _state.value.cpuInfo?.clusterBig?.maxFreqKhz ?: freqKhz, freqKhz)
        }
        val mhz = freqKhz.toLongOrNull()?.let { "${it / 1000} MHz" } ?: freqKhz
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), statusMessage = "$cluster min: $mhz")
    }
    fun setCpuCoreOnline(core: Int, online: Boolean) = update {
        DeviceInfo.setCpuCoreOnline(core, online)
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(),
            statusMessage = "cpu$core: ${if (online) "online" else "offline"}")
    }
    // Tek çekirdek max frekans
    fun setCpuMaxFreqSingle(core: Int, freqKhz: String) = update {
        RootUtils.writeFile("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_max_freq", freqKhz)
        val mhz = freqKhz.toLongOrNull()?.let { "${it / 1000} MHz" } ?: freqKhz
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(),
            statusMessage = "cpu$core max: $mhz")
    }
    // Prime çekirdeği tam frekansına aç
    fun unlockPrimeCore() = update {
        DeviceInfo.unlockPrimeCore()
        _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(),
            statusMessage = "Prime (cpu7) 3350 MHz'e açıldı")
    }
    fun setGpuGovernor(gov: String) = update {
        DeviceInfo.setGpuGovernor(gov)
        _state.value.copy(gpuInfo = DeviceInfo.getGpuInfo(), statusMessage = "GPU governor: $gov")
    }
    fun setGpuMaxFreq(hz: String) = update {
        DeviceInfo.setGpuMaxFreq(hz)
        val mhz = hz.toLongOrNull()?.let { "${it / 1_000_000} MHz" } ?: hz
        _state.value.copy(gpuInfo = DeviceInfo.getGpuInfo(), statusMessage = "GPU max: $mhz")
    }
    fun setGpuMinFreq(hz: String) = update {
        DeviceInfo.setGpuMinFreq(hz)
        val mhz = hz.toLongOrNull()?.let { "${it / 1_000_000} MHz" } ?: hz
        _state.value.copy(gpuInfo = DeviceInfo.getGpuInfo(), statusMessage = "GPU min: $mhz")
    }

    // Batarya
    fun setTouchPollingRate(rate: Int) = update {
        DeviceInfo.setTouchPollingRate(rate)
        _state.value.copy(touchPollingRate = DeviceInfo.getTouchPollingRate(), statusMessage = "Polling rate: ${rate}Hz")
    }
    fun setChargeLimit(pct: Int) = update {
        DeviceInfo.setChargeLimit(pct)
        _state.value.copy(batteryInfo = DeviceInfo.getBatteryInfo(), statusMessage = "Şarj limiti: %$pct")
    }
    fun setFastCharge(enabled: Boolean) = update {
        DeviceInfo.setFastCharge(enabled)
        _state.value.copy(batteryInfo = DeviceInfo.getBatteryInfo(),
            statusMessage = if (enabled) "Hızlı şarj açıldı" else "Hızlı şarj kapatıldı")
    }

    // Bellek
    fun setSwappiness(value: Int) = update {
        DeviceInfo.setSwappiness(value)
        _state.value.copy(memInfo = DeviceInfo.getMemInfo(), statusMessage = "Swappiness: $value")
    }
    fun setZramAlgo(algo: String) = update {
        DeviceInfo.setZramAlgo(algo)
        _state.value.copy(memInfo = DeviceInfo.getMemInfo(), statusMessage = "ZRAM algoritması: $algo")
    }
    fun setIoScheduler(sched: String) = update {
        DeviceInfo.setIoScheduler(sched)
        _state.value.copy(ioScheduler = DeviceInfo.getIoScheduler(), statusMessage = "I/O scheduler: $sched")
    }

    // Ekran
    fun setHbm(enabled: Boolean) = update {
        DeviceInfo.setHbm(enabled)
        _state.value.copy(displayInfo = DeviceInfo.getDisplayInfo(),
            statusMessage = if (enabled) "HBM açıldı" else "HBM kapatıldı")
    }
    fun setDcDimming(enabled: Boolean) = update {
        DeviceInfo.setDcDimming(enabled)
        _state.value.copy(displayInfo = DeviceInfo.getDisplayInfo(),
            statusMessage = if (enabled) "DC Dimming açıldı" else "DC Dimming kapatıldı")
    }
    fun setRefreshRate(hz: Int) = update {
        DeviceInfo.setRefreshRate(hz)
        _state.value.copy(displayInfo = DeviceInfo.getDisplayInfo(), statusMessage = "Yenileme hızı: ${hz}Hz")
    }

    // Ağ
    fun setTcpCongestion(algo: String) = update {
        DeviceInfo.setTcpCongestion(algo)
        _state.value.copy(networkInfo = DeviceInfo.getNetworkInfo(), statusMessage = "TCP: $algo")
    }
    fun setDns(dns1: String, dns2: String) = update {
        DeviceInfo.setDns(dns1, dns2)
        _state.value.copy(networkInfo = DeviceInfo.getNetworkInfo(), statusMessage = "DNS güncellendi")
    }
    fun setWifiPowerSave(enabled: Boolean) = update {
        DeviceInfo.setWifiPowerSave(enabled)
        _state.value.copy(networkInfo = DeviceInfo.getNetworkInfo(),
            statusMessage = if (enabled) "Wi-Fi güç tasarrufu açıldı" else "Wi-Fi güç tasarrufu kapatıldı")
    }

    // Ses
    fun setDolbyEnabled(enabled: Boolean) = update {
        DeviceInfo.setDolbyEnabled(enabled)
        _state.value.copy(audioInfo = DeviceInfo.getAudioInfo(),
            statusMessage = if (enabled) "Dolby açıldı" else "Dolby kapatıldı")
    }
    fun setDolbyProfile(profile: String) = update {
        DeviceInfo.setDolbyProfile(profile)
        _state.value.copy(audioInfo = DeviceInfo.getAudioInfo(), statusMessage = "Dolby profili: $profile")
    }
    fun setSpeakerGain(gain: Int) = update {
        DeviceInfo.setSpeakerGain(gain)
        _state.value.copy(audioInfo = DeviceInfo.getAudioInfo(), statusMessage = "Hoparlör gain: $gain")
    }
    fun setMicGain(gain: Int) = update {
        DeviceInfo.setMicGain(gain)
        _state.value.copy(audioInfo = DeviceInfo.getAudioInfo(), statusMessage = "Mikrofon gain: $gain")
    }

    // Sistem
    fun setSELinux(enforcing: Boolean) = update {
        DeviceInfo.setSELinux(enforcing)
        _state.value.copy(systemInfo = DeviceInfo.getSystemInfo(),
            statusMessage = if (enforcing) "SELinux: Enforcing" else "SELinux: Permissive")
    }
    fun setProp(key: String, value: String) = update {
        DeviceInfo.setProp(key, value)
        _state.value.copy(systemInfo = DeviceInfo.getSystemInfo(), statusMessage = "Prop ayarlandı: $key")
    }
    fun setNfc(enabled: Boolean) = update {
        DeviceInfo.setNfcEnabled(enabled)
        _state.value.copy(nfcEnabled = enabled,
            statusMessage = if (enabled) "NFC açıldı" else "NFC kapatıldı")
    }
    fun loadLogcat() = update { _state.value.copy(logcatLines = DeviceInfo.getLogcat(300)) }
    fun sendIr(freq: Int, pattern: String) = update {
        DeviceInfo.sendIrSignal(freq, pattern)
        _state.value.copy(statusMessage = "IR sinyali gönderildi: ${freq}Hz")
    }

    // Magisk / KernelSU
    fun setModuleEnabled(id: String, enabled: Boolean) = update {
        val rootType = _state.value.rootInfo?.rootType ?: "Magisk"
        MagiskInfo.setModuleEnabled(id, enabled, rootType)
        _state.value.copy(rootInfo = MagiskInfo.getRootInfo(),
            statusMessage = if (enabled) "Modül etkinleştirildi: $id" else "Modül devre dışı: $id")
    }
    fun refreshRootInfo() = update { _state.value.copy(rootInfo = MagiskInfo.getRootInfo()) }

    // Kernel Tweaks
    fun setKernelTweak(sysfsPath: String, value: String, label: String) = update {
        KernelTweakInfo.set(sysfsPath, value)
        _state.value.copy(kernelTweaks = KernelTweakInfo.get(), statusMessage = "$label: $value")
    }

    // Performans Profilleri
    fun applyProfile(profile: PerfProfile) = update {
        PerformanceProfiles.apply(profile)
        _state.value.copy(
            cpuInfo = DeviceInfo.getCpuInfo(), gpuInfo = DeviceInfo.getGpuInfo(),
            touchPollingRate = DeviceInfo.getTouchPollingRate(),
            memInfo = DeviceInfo.getMemInfo(), networkInfo = DeviceInfo.getNetworkInfo(),
            activeProfileId = profile.id, statusMessage = "Profil uygulandı: ${profile.name}"
        )
    }

    // Canlı İzleme
    fun startLiveMonitoring() {
        if (monitorJob?.isActive == true) return
        _state.value = _state.value.copy(liveMonitoringActive = true)
        var lastRx = 0L; var lastTx = 0L
        monitorJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val now = System.currentTimeMillis()
                val (rx, tx) = DeviceInfo.getNetworkSpeed()
                val rxSpeed = if (lastRx > 0) ((rx - lastRx) / 2f / 1024f) else 0f
                val txSpeed = if (lastTx > 0) ((tx - lastTx) / 2f / 1024f) else 0f
                lastRx = rx; lastTx = tx
                val bat = DeviceInfo.getBatteryInfo()
                val currentMa = bat.currentNow.replace(" mA", "").toFloatOrNull() ?: 0f
                val voltageMv = bat.voltage.replace(" mV", "").toFloatOrNull() ?: 0f
                _state.value = _state.value.copy(
                    cpuHistory      = (_state.value.cpuHistory      + LiveMetric(now, LiveMetrics.getCpuUsage())).takeLast(maxHistory),
                    gpuHistory      = (_state.value.gpuHistory      + LiveMetric(now, LiveMetrics.getGpuUsage())).takeLast(maxHistory),
                    ramHistory      = (_state.value.ramHistory      + LiveMetric(now, LiveMetrics.getRamUsagePct())).takeLast(maxHistory),
                    batteryHistory  = (_state.value.batteryHistory  + LiveMetric(now, LiveMetrics.getBatteryPct())).takeLast(maxHistory),
                    cpuTempHistory  = (_state.value.cpuTempHistory  + LiveMetric(now, LiveMetrics.getCpuTemp())).takeLast(maxHistory),
                    netRxHistory    = (_state.value.netRxHistory    + LiveMetric(now, rxSpeed.coerceAtLeast(0f))).takeLast(maxHistory),
                    netTxHistory    = (_state.value.netTxHistory    + LiveMetric(now, txSpeed.coerceAtLeast(0f))).takeLast(maxHistory),
                    chargeHistory   = (_state.value.chargeHistory   + LiveMetric(now, currentMa)).takeLast(maxHistory),
                    voltageHistory  = (_state.value.voltageHistory  + LiveMetric(now, voltageMv)).takeLast(maxHistory)
                )
                // Termal throttle kontrolü
                val temp = _state.value.cpuTempHistory.lastOrNull()?.value ?: 0f
                if (_state.value.thermalThrottleEnabled && temp >= _state.value.thermalThrottleTempC) {
                    PerformanceProfiles.presets.find { it.id == _state.value.thermalThrottleProfileId }?.let { p ->
                        PerformanceProfiles.apply(p)
                        _state.value = _state.value.copy(activeProfileId = p.id,
                            statusMessage = "Termal throttle: ${temp.toInt()}°C → ${p.name}")
                    }
                }
                delay(2000)
            }
        }
    }
    fun stopLiveMonitoring() {
        monitorJob?.cancel(); monitorJob = null
        _state.value = _state.value.copy(liveMonitoringActive = false)
    }

    // Uygulama Yöneticisi
    fun loadApps(includeSystem: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(appsLoading = true)
            _state.value = _state.value.copy(installedApps = AppManager.getInstalledApps(includeSystem), appsLoading = false)
        }
    }
    fun forceStopApp(pkg: String) = update { AppManager.forceStop(pkg); _state.value.copy(statusMessage = "Durduruldu: $pkg") }
    fun clearAppData(pkg: String) = update { AppManager.clearData(pkg); _state.value.copy(statusMessage = "Veri temizlendi: $pkg") }
    fun disableApp(pkg: String) = update {
        AppManager.disableApp(pkg)
        _state.value.copy(installedApps = AppManager.getInstalledApps(), statusMessage = "Devre dışı: $pkg")
    }
    fun enableApp(pkg: String) = update {
        AppManager.enableApp(pkg)
        _state.value.copy(installedApps = AppManager.getInstalledApps(), statusMessage = "Etkinleştirildi: $pkg")
    }
    fun uninstallApp(pkg: String) = update {
        AppManager.uninstallApp(pkg)
        _state.value.copy(installedApps = AppManager.getInstalledApps(), statusMessage = "Kaldırıldı: $pkg")
    }
    fun freezeApp(pkg: String) = update { AppManager.freezeApp(pkg); _state.value.copy(statusMessage = "Donduruldu: $pkg") }
    fun unfreezeApp(pkg: String) = update { AppManager.unfreezeApp(pkg); _state.value.copy(statusMessage = "Çözüldü: $pkg") }

    // Doze
    fun loadDoze() = update {
        _state.value.copy(dozeWhitelist = DozeManager.getWhitelist(), dozeEnabled = DozeManager.isDozeEnabled())
    }
    fun addDozeWhitelist(pkg: String) = update {
        DozeManager.addToWhitelist(pkg)
        _state.value.copy(dozeWhitelist = DozeManager.getWhitelist(), statusMessage = "Doze istisnasına eklendi: $pkg")
    }
    fun removeDozeWhitelist(pkg: String) = update {
        DozeManager.removeFromWhitelist(pkg)
        _state.value.copy(dozeWhitelist = DozeManager.getWhitelist(), statusMessage = "Doze istisnasından çıkarıldı: $pkg")
    }
    fun forceDoze() = update { DozeManager.forceDoze(); _state.value.copy(statusMessage = "Doze modu zorlandı") }
    fun exitDoze() = update { DozeManager.exitDoze(); _state.value.copy(statusMessage = "Doze modundan çıkıldı") }

    // Güvenlik
    fun loadSecurity() = update { _state.value.copy(securityInfo = SecurityManager.getSecurityInfo()) }
    fun addHostsEntry(domain: String, ip: String) = update {
        SecurityManager.addHostsEntry(domain, ip); _state.value.copy(statusMessage = "Hosts eklendi: $domain")
    }
    fun removeHostsEntry(domain: String) = update {
        SecurityManager.removeHostsEntry(domain); _state.value.copy(statusMessage = "Hosts silindi: $domain")
    }
    fun removeUserCert(hash: String) = update {
        SecurityManager.removeUserCert(hash)
        _state.value.copy(securityInfo = SecurityManager.getSecurityInfo(), statusMessage = "Sertifika kaldırıldı")
    }

    // Tema & Kişiselleştirme
    fun setTheme(theme: AppTheme) { AppPrefs.theme = theme; _state.value = _state.value.copy(appTheme = theme) }
    fun setAccentColor(index: Int) { AppPrefs.accentColorIndex = index; _state.value = _state.value.copy(accentColorIndex = index) }
    fun setDashboardCompact(compact: Boolean) { AppPrefs.dashboardCompact = compact; _state.value = _state.value.copy(dashboardCompact = compact) }

    // Zamanlayıcı
    fun addScheduleRule(rule: ScheduleRule) {
        val rules = _state.value.scheduleRules + rule
        AppPrefs.saveScheduleRules(rules)
        _state.value = _state.value.copy(scheduleRules = rules, statusMessage = "Kural eklendi: ${rule.name}")
    }
    fun deleteScheduleRule(id: String) {
        val rules = _state.value.scheduleRules.filter { it.id != id }
        AppPrefs.saveScheduleRules(rules)
        _state.value = _state.value.copy(scheduleRules = rules, statusMessage = "Kural silindi")
    }
    fun toggleScheduleRule(id: String, enabled: Boolean) {
        val rules = _state.value.scheduleRules.map { if (it.id == id) it.copy(enabled = enabled) else it }
        AppPrefs.saveScheduleRules(rules)
        _state.value = _state.value.copy(scheduleRules = rules)
    }

    private fun startScheduler() {
        schedulerJob?.cancel()
        schedulerJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val rules = _state.value.scheduleRules.filter { it.enabled }
                val cal = java.util.Calendar.getInstance()
                val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
                val m = cal.get(java.util.Calendar.MINUTE)
                val charging = _state.value.batteryInfo?.status?.contains("Charging", ignoreCase = true) == true
                val temp = _state.value.cpuTempHistory.lastOrNull()?.value ?: 0f
                rules.forEach { rule ->
                    val fire = when (rule.trigger) {
                        ScheduleTrigger.TIME -> h == rule.hour && m == rule.minute
                        ScheduleTrigger.CHARGING -> charging == rule.onCharging
                        ScheduleTrigger.TEMPERATURE -> temp >= rule.tempThresholdC
                    }
                    if (fire) {
                        PerformanceProfiles.presets.find { it.id == rule.profileId }?.let { p ->
                            PerformanceProfiles.apply(p)
                            _state.value = _state.value.copy(activeProfileId = p.id,
                                statusMessage = "Zamanlayıcı: ${rule.name} → ${p.name}")
                        }
                    }
                }
                delay(60_000)
            }
        }
    }

    // KernelKit Modül
    fun installKernelKit() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(statusMessage = "Modül kuruluyor...")
            val (ok, msg) = KernelKitInstaller.install(context)
            _state.value = _state.value.copy(
                kernelKitInstalled = KernelKitInstaller.isInstalled(),
                kernelKitEnabled = KernelKitInstaller.isEnabled(),
                statusMessage = msg
            )
        }
    }
    fun uninstallKernelKit() = update {
        KernelKitInstaller.uninstall()
        _state.value.copy(
            kernelKitInstalled = false,
            kernelKitEnabled = false,
            statusMessage = "KernelKit modülü kaldırıldı. Yeniden başlatın."
        )
    }
    fun refreshKernelKitStatus() = update {
        _state.value.copy(
            kernelKitInstalled = KernelKitInstaller.isInstalled(),
            kernelKitEnabled = KernelKitInstaller.isEnabled()
        )
    }

    // Yardımcılar
    fun clearStatus() { _state.value = _state.value.copy(statusMessage = "") }
    fun refreshBattery() = update { _state.value.copy(batteryInfo = DeviceInfo.getBatteryInfo()) }
    fun refreshThermals() = update { _state.value.copy(thermals = DeviceInfo.getThermals()) }
    fun refreshCpu() = update { _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), gpuInfo = DeviceInfo.getGpuInfo()) }

    // MTK EAS/HMP
    fun loadMtkEas() = update { _state.value.copy(mtkEasInfo = DeviceInfo.getMtkEasInfo()) }
    fun setMtkParam(path: String, value: String, label: String) = update {
        DeviceInfo.setMtkParam(path, value)
        _state.value.copy(mtkEasInfo = DeviceInfo.getMtkEasInfo(), statusMessage = "$label: $value")
    }

    // Termal throttle
    fun setThermalThrottle(enabled: Boolean, tempC: Int, profileId: String) {
        _state.value = _state.value.copy(
            thermalThrottleEnabled = enabled,
            thermalThrottleTempC = tempC,
            thermalThrottleProfileId = profileId,
            statusMessage = if (enabled) "Termal throttle aktif: ≥${tempC}°C → $profileId" else "Termal throttle kapatıldı"
        )
    }

    // VPN
    fun loadVpnInfo() = update { _state.value.copy(vpnInfo = DeviceInfo.getVpnInfo()) }

    // Firewall
    fun loadFirewallRules() = update { _state.value.copy(firewallRules = DeviceInfo.getFirewallRules()) }
    fun addFirewallRule(packageName: String, appLabel: String, uid: Int, blockWifi: Boolean, blockData: Boolean) = update {
        DeviceInfo.addFirewallRule(uid, blockWifi, blockData)
        val rule = FirewallRule(uid.toString(), packageName, appLabel, blockWifi, blockData)
        _state.value.copy(firewallRules = _state.value.firewallRules + rule,
            statusMessage = "Firewall kuralı eklendi: $appLabel")
    }
    fun removeFirewallRule(uid: String) = update {
        DeviceInfo.removeFirewallRule(uid.toIntOrNull() ?: 0)
        _state.value.copy(firewallRules = _state.value.firewallRules.filter { it.id != uid },
            statusMessage = "Firewall kuralı kaldırıldı")
    }

    // FPS overlay
    fun setFpsOverlay(enabled: Boolean) {
        _state.value = _state.value.copy(fpsOverlayEnabled = enabled,
            statusMessage = if (enabled) "FPS overlay açıldı" else "FPS overlay kapatıldı")
    }

    // Oyun modu
    fun setGameModeEnabled(context: android.content.Context, enabled: Boolean) {
        AppPrefs.gameModeEnabled = enabled
        _state.value = _state.value.copy(gameModeEnabled = enabled,
            statusMessage = if (enabled) "Oyun modu servisi başlatıldı" else "Oyun modu durduruldu")
        if (enabled) GameModeService.start(context)
        else GameModeService.stop(context)
    }
    fun addGameApp(packageName: String) {
        val apps = _state.value.gameApps.toMutableSet().also { it.add(packageName) }
        AppPrefs.saveGameApps(apps)
        _state.value = _state.value.copy(gameApps = apps, statusMessage = "Oyun uygulaması eklendi: $packageName")
    }
    fun removeGameApp(packageName: String) {
        val apps = _state.value.gameApps.toMutableSet().also { it.remove(packageName) }
        AppPrefs.saveGameApps(apps)
        _state.value = _state.value.copy(gameApps = apps)
    }

    // Termal bildirim
    fun setThermalAlert(context: android.content.Context, enabled: Boolean, tempC: Int) {
        AppPrefs.thermalAlertEnabled = enabled
        AppPrefs.thermalAlertTempC = tempC
        _state.value = _state.value.copy(thermalAlertEnabled = enabled, thermalAlertTempC = tempC,
            statusMessage = if (enabled) "Termal uyarı aktif: ≥${tempC}°C" else "Termal uyarı kapatıldı")
        if (enabled) ThermalNotificationService.start(context)
        else ThermalNotificationService.stop(context)
    }

    // Önyükleme scripti
    fun loadBootScript() = update {
        _state.value.copy(
            bootScriptExists = BootScriptManager.isScriptExists(),
            bootScriptContent = BootScriptManager.getScriptContent()
        )
    }
    fun saveBootScript(content: String) = update {
        BootScriptManager.saveScript(content)
        _state.value.copy(bootScriptExists = true, bootScriptContent = content,
            statusMessage = "Önyükleme scripti kaydedildi: ${BootScriptManager.getScriptPath()}")
    }
    fun deleteBootScript() = update {
        BootScriptManager.deleteScript()
        _state.value.copy(bootScriptExists = false, bootScriptContent = "",
            statusMessage = "Önyükleme scripti silindi")
    }
    fun generateBootScriptFromProfile(profileId: String) = update {
        val profile = PerformanceProfiles.presets.find { it.id == profileId }
            ?: PerformanceProfiles.presets[1]
        val content = BootScriptManager.generateFromProfile(profile)
        _state.value.copy(bootScriptContent = content)
    }

    // Uygulama başına profil
    fun setAppProfile(packageName: String, profileId: String) {
        val map = _state.value.appProfiles.toMutableMap()
        map[packageName] = profileId
        AppPrefs.saveAppProfiles(map)
        _state.value = _state.value.copy(appProfiles = map, statusMessage = "Profil atandı: $packageName → $profileId")
    }
    fun clearAppProfile(packageName: String) {
        val map = _state.value.appProfiles.toMutableMap()
        map.remove(packageName)
        AppPrefs.saveAppProfiles(map)
        _state.value = _state.value.copy(appProfiles = map)
    }

    // Uyku modu
    fun setSleepModeEnabled(enabled: Boolean) {
        AppPrefs.sleepModeEnabled = enabled
        _state.value = _state.value.copy(sleepModeEnabled = enabled,
            statusMessage = if (enabled) "Uyku modu aktif" else "Uyku modu kapatıldı")
    }
    fun setSleepProfile(profileId: String) {
        AppPrefs.sleepProfileId = profileId
        _state.value = _state.value.copy(sleepProfileId = profileId)
    }
    fun setWakeProfile(profileId: String) {
        AppPrefs.wakeProfileId = profileId
        _state.value = _state.value.copy(wakeProfileId = profileId)
    }

    // Yedek / Geri Yükle
    fun backupSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = AppPrefs.getBackupJson(
                    scheduleRules = _state.value.scheduleRules,
                    appProfiles   = _state.value.appProfiles,
                    theme         = _state.value.appTheme,
                    accentIndex   = _state.value.accentColorIndex,
                    sleepEnabled  = _state.value.sleepModeEnabled,
                    sleepProfile  = _state.value.sleepProfileId,
                    wakeProfile   = _state.value.wakeProfileId
                )
                val dir = java.io.File("/sdcard/DimensityTool")
                dir.mkdirs()
                java.io.File(dir, "backup.json").writeText(json)
                _state.value = _state.value.copy(statusMessage = "Yedek alındı: /sdcard/DimensityTool/backup.json")
            } catch (e: Exception) {
                _state.value = _state.value.copy(statusMessage = "Yedek alınamadı: ${e.message}")
            }
        }
    }
    fun restoreSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = java.io.File("/sdcard/DimensityTool/backup.json")
                if (!file.exists()) {
                    _state.value = _state.value.copy(statusMessage = "Yedek dosyası bulunamadı")
                    return@launch
                }
                // Basit restore: schedule rules ve app profiles
                val json = file.readText()
                val rules = AppPrefs.loadScheduleRules()
                AppPrefs.saveScheduleRules(rules)
                _state.value = _state.value.copy(
                    scheduleRules = rules,
                    statusMessage = "Ayarlar geri yüklendi"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(statusMessage = "Geri yükleme başarısız: ${e.message}")
            }
        }
    }
    fun resetAllSettings() {
        AppPrefs.saveScheduleRules(emptyList())
        AppPrefs.saveAppProfiles(emptyMap())
        AppPrefs.theme = AppTheme.DARK
        AppPrefs.accentColorIndex = 0
        AppPrefs.sleepModeEnabled = false
        AppPrefs.sleepProfileId = "powersave"
        AppPrefs.wakeProfileId = "balanced"
        _state.value = _state.value.copy(
            scheduleRules = emptyList(),
            appProfiles = emptyMap(),
            appTheme = AppTheme.DARK,
            accentColorIndex = 0,
            sleepModeEnabled = false,
            sleepProfileId = "powersave",
            wakeProfileId = "balanced",
            statusMessage = "Tüm ayarlar sıfırlandı"
        )
    }
    fun getBackupPath(): String? {
        val f = java.io.File("/sdcard/DimensityTool/backup.json")
        return if (f.exists()) f.absolutePath else null
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveMonitoring()
        schedulerJob?.cancel()
    }
}
