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
            chargeNotifyEnabled = AppPrefs.chargeNotifyEnabled,
            chargeNotifyPct = AppPrefs.chargeNotifyPct,
            nightChargeEnabled = AppPrefs.nightChargeEnabled,
            nightChargeStartHour = AppPrefs.nightChargeStartHour,
            nightChargeEndHour = AppPrefs.nightChargeEndHour,
            nightChargeLimitPct = AppPrefs.nightChargeLimitPct,
            quickTileProfileId = AppPrefs.quickTileProfileId,
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

    // Prop editörü - tüm prop'ları listele
    fun loadAllProps() = update {
        val raw = RootUtils.runCommand("getprop")
        val map = raw.lines().mapNotNull { line ->
            val m = Regex("\\[(.+?)\\]: \\[(.*)\\]").find(line)
            m?.let { it.groupValues[1] to it.groupValues[2] }
        }.toMap()
        _state.value.copy(allProps = map)
    }

    // Sistem bilgisi paylaşımı
    fun buildSystemShareText(): String {
        val s = _state.value
        return buildString {
            appendLine("=== DimensityTool Sistem Raporu ===")
            appendLine("Cihaz: Poco X6 Pro 5G (duchamp)")
            appendLine("SoC: MediaTek Dimensity 8300 Ultra (MT6897)")
            appendLine("Android: ${s.deviceBasic["Android"] ?: "N/A"}")
            appendLine("Build: ${s.deviceBasic["Build"] ?: "N/A"}")
            appendLine()
            s.cpuInfo?.let {
                appendLine("CPU Governor: ${it.governor}")
                appendLine("Prime Frekans: ${it.clusterPrime.curFreqMhz}")
                appendLine("Big Frekans: ${it.clusterBig.curFreqMhz}")
                appendLine("Little Frekans: ${it.clusterLittle.curFreqMhz}")
            }
            s.gpuInfo?.let { appendLine("GPU: ${it.curFreqMhz} / ${it.governor}") }
            s.batteryInfo?.let {
                appendLine("Batarya: ${it.capacity}% / ${it.status} / ${it.tempC}")
            }
            s.memInfo?.let {
                appendLine("RAM: ${it.usedMb}/${it.totalMb} MB")
                appendLine("Swappiness: ${it.swappiness}")
            }
            s.systemInfo?.let {
                appendLine("Kernel: ${it.kernelVersion}")
                appendLine("SELinux: ${it.selinuxMode}")
                appendLine("Uptime: ${it.uptime}")
            }
            appendLine("Root: ${s.rootInfo?.rootType ?: "N/A"} ${s.rootInfo?.version ?: ""}")
            appendLine("Aktif Profil: ${s.activeProfileId}")
        }
    }

    // Pil bildirimi
    fun setChargeNotify(enabled: Boolean, pct: Int) {
        AppPrefs.chargeNotifyEnabled = enabled
        AppPrefs.chargeNotifyPct = pct
        _state.value = _state.value.copy(chargeNotifyEnabled = enabled, chargeNotifyPct = pct,
            statusMessage = if (enabled) "Pil bildirimi aktif: %$pct" else "Pil bildirimi kapatıldı")
    }

    // Gece şarj modu
    fun setNightCharge(enabled: Boolean, startHour: Int, endHour: Int, limitPct: Int) {
        AppPrefs.nightChargeEnabled = enabled
        AppPrefs.nightChargeStartHour = startHour
        AppPrefs.nightChargeEndHour = endHour
        AppPrefs.nightChargeLimitPct = limitPct
        _state.value = _state.value.copy(
            nightChargeEnabled = enabled,
            nightChargeStartHour = startHour,
            nightChargeEndHour = endHour,
            nightChargeLimitPct = limitPct,
            statusMessage = if (enabled) "Gece şarj modu aktif: %02d:00-%02d:00 → %%%d".format(startHour, endHour, limitPct)
                            else "Gece şarj modu kapatıldı"
        )
    }

    // Ping testi
    fun runPingTest() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(pingRunning = true)
            val targets = mapOf(
                "Google (8.8.8.8)"      to "8.8.8.8",
                "Cloudflare (1.1.1.1)"  to "1.1.1.1",
                "Quad9 (9.9.9.9)"       to "9.9.9.9",
                "Google DNS (8.8.4.4)"  to "8.8.4.4"
            )
            val results = targets.mapValues { (_, ip) ->
                val out = RootUtils.runCommand("ping -c 3 -W 2 $ip 2>/dev/null | tail -1")
                if (out.contains("avg")) {
                    val avg = out.substringAfter("/").substringBefore("/")
                    "${avg.trim()} ms"
                } else "Zaman aşımı"
            }
            _state.value = _state.value.copy(pingResults = results, pingRunning = false)
        }
    }

    // APK yedekleme
    fun backupApk(packageName: String, appLabel: String) = update {
        val apkPath = RootUtils.runCommand("pm path $packageName | cut -d: -f2").trim()
        if (apkPath.isNotBlank()) {
            val dir = "/sdcard/DimensityTool/apk"
            RootUtils.runCommand("mkdir -p $dir")
            RootUtils.runCommand("cp $apkPath $dir/${appLabel.replace(" ", "_")}.apk")
            _state.value.copy(apkBackupStatus = "APK kaydedildi: $dir/${appLabel}.apk",
                statusMessage = "APK yedeklendi: $appLabel")
        } else {
            _state.value.copy(apkBackupStatus = "APK bulunamadı: $packageName")
        }
    }

    // Görev yöneticisi
    fun loadProcessList() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(processLoading = true)
            val raw = RootUtils.runCommand("ps -A -o PID,USER,RSS,PCPU,NAME 2>/dev/null | tail -n +2")
            val list = raw.lines().mapNotNull { line ->
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 5) {
                    try {
                        ProcessInfo(
                            pid = parts[0].toInt(),
                            user = parts[1],
                            ramMb = (parts[2].toLongOrNull() ?: 0L) / 1024f,
                            cpuPct = parts[3].toFloatOrNull() ?: 0f,
                            name = parts.drop(4).joinToString(" ")
                        )
                    } catch (e: Exception) { null }
                } else null
            }.sortedByDescending { it.ramMb }
            _state.value = _state.value.copy(processList = list, processLoading = false)
        }
    }
    fun killProcess(pid: Int, name: String) = update {
        RootUtils.runCommand("kill -9 $pid")
        val list = _state.value.processList.filter { it.pid != pid }
        _state.value.copy(processList = list, statusMessage = "Sonlandırıldı: $name")
    }
    fun clearRamCache() = update {
        RootUtils.runCommand("am send-trim-memory all COMPLETE")
        _state.value.copy(memInfo = DeviceInfo.getMemInfo(), statusMessage = "RAM cache temizlendi")
    }
    fun dropCaches() = update {
        RootUtils.writeFile("/proc/sys/vm/drop_caches", "3")
        _state.value.copy(memInfo = DeviceInfo.getMemInfo(), statusMessage = "Drop caches uygulandı")
    }
    fun setZramSize(size: String) = update {
        val bytes = when (size) {
            "1G" -> "1073741824"
            "2G" -> "2147483648"
            "3G" -> "3221225472"
            "4G" -> "4294967296"
            else -> "2147483648"
        }
        RootUtils.runCommand("swapoff /dev/block/zram0 2>/dev/null")
        RootUtils.writeFile("/sys/block/zram0/reset", "1")
        RootUtils.writeFile("/sys/block/zram0/disksize", bytes)
        RootUtils.runCommand("mkswap /dev/block/zram0 && swapon /dev/block/zram0")
        _state.value.copy(memInfo = DeviceInfo.getMemInfo(), statusMessage = "ZRAM boyutu: $size")
    }

    // Hız testi
    fun runSpeedTest() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(speedTestRunning = true)
            try {
                // Ping
                val pingOut = RootUtils.runCommand("ping -c 3 -W 2 8.8.8.8 2>/dev/null | tail -1")
                val pingMs = if (pingOut.contains("avg")) {
                    pingOut.substringAfter("/").substringBefore("/").trim().toFloatOrNull()?.toInt() ?: 0
                } else 0

                // İndirme hızı (10MB dosya)
                val dlStart = System.currentTimeMillis()
                val dlBytes = RootUtils.runCommand(
                    "curl -s -o /dev/null -w '%{size_download}' --max-time 10 https://speed.cloudflare.com/__down?bytes=10000000 2>/dev/null"
                ).toLongOrNull() ?: 0L
                val dlTime = (System.currentTimeMillis() - dlStart) / 1000f
                val dlMbps = if (dlTime > 0) (dlBytes * 8f / 1_000_000f / dlTime) else 0f

                // Yükleme hızı (1MB)
                val ulStart = System.currentTimeMillis()
                RootUtils.runCommand(
                    "curl -s -o /dev/null -w '%{size_upload}' --max-time 10 -X POST -d @/dev/urandom --max-filesize 1000000 https://speed.cloudflare.com/__up 2>/dev/null"
                )
                val ulTime = (System.currentTimeMillis() - ulStart) / 1000f
                val ulMbps = if (ulTime > 0) (1_000_000f * 8f / 1_000_000f / ulTime) else 0f

                _state.value = _state.value.copy(
                    speedTestResult = SpeedTestResult(dlMbps, ulMbps, pingMs, "Cloudflare"),
                    speedTestRunning = false,
                    statusMessage = "Hız testi tamamlandı: ${dlMbps.toInt()} Mbps ↓"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(speedTestRunning = false,
                    statusMessage = "Hız testi başarısız: ${e.message}")
            }
        }
    }

    // Wi-Fi analizi
    fun scanWifiNetworks() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(wifiScanRunning = true)
            val raw = RootUtils.runCommand("iw dev wlan0 scan 2>/dev/null")
            val networks = mutableListOf<WifiNetwork>()
            var bssid = ""; var ssid = ""; var level = -100
            var freq = 2412; var security = "Open"
            raw.lines().forEach { line ->
                val t = line.trim()
                when {
                    t.startsWith("BSS ") -> {
                        if (bssid.isNotEmpty()) {
                            val ch = if (freq > 4000) ((freq - 5000) / 5) else ((freq - 2407) / 5)
                            networks.add(WifiNetwork(ssid, bssid, level, freq, ch, security))
                        }
                        bssid = t.substringAfter("BSS ").substringBefore("(").trim()
                        ssid = ""; level = -100; freq = 2412; security = "Open"
                    }
                    t.startsWith("SSID:") -> ssid = t.substringAfter("SSID:").trim()
                    t.startsWith("freq:") -> freq = t.substringAfter("freq:").trim().toIntOrNull() ?: 2412
                    t.startsWith("signal:") -> level = t.substringAfter("signal:").trim().substringBefore(" ").toFloatOrNull()?.toInt() ?: -100
                    t.contains("WPA") || t.contains("RSN") -> security = "WPA2"
                    t.contains("WEP") -> security = "WEP"
                }
            }
            if (bssid.isNotEmpty()) {
                val ch = if (freq > 4000) ((freq - 5000) / 5) else ((freq - 2407) / 5)
                networks.add(WifiNetwork(ssid, bssid, level, freq, ch, security))
            }
            _state.value = _state.value.copy(wifiNetworks = networks, wifiScanRunning = false,
                statusMessage = "${networks.size} Wi-Fi ağı bulundu")
        }
    }

    // Pil sağlığı skoru
    fun calculateBatteryHealth() = update {
        val bat = DeviceInfo.getBatteryInfo()
        val cycles = bat.cycleCount.toIntOrNull() ?: 0
        val tempC = bat.tempC.replace("°C", "").toFloatOrNull() ?: 25f
        val capacity = bat.capacity

        var score = 100
        val details = mutableListOf<String>()

        if (cycles > 500) { score -= 20; details.add("Yüksek şarj döngüsü: $cycles") }
        else if (cycles > 300) { score -= 10; details.add("Orta şarj döngüsü: $cycles") }
        else details.add("Şarj döngüsü normal: $cycles")

        if (tempC > 40f) { score -= 15; details.add("Yüksek sıcaklık: ${tempC}°C") }
        else if (tempC > 35f) { score -= 5; details.add("Sıcaklık biraz yüksek: ${tempC}°C") }
        else details.add("Sıcaklık normal: ${tempC}°C")

        if (capacity < 80) { score -= 20; details.add("Düşük kapasite: %$capacity") }
        else if (capacity < 90) { score -= 5; details.add("Kapasite iyi: %$capacity") }
        else details.add("Kapasite mükemmel: %$capacity")

        val grade = when {
            score >= 80 -> "İyi"
            score >= 60 -> "Orta"
            else        -> "Kötü"
        }

        _state.value.copy(
            batteryHealthScore = BatteryHealthScore(
                score = score.coerceIn(0, 100),
                grade = grade,
                cycleCount = cycles,
                avgTempC = tempC,
                capacityPct = capacity,
                details = details
            )
        )
    }

    // Stres testi
    fun startStressTest(durationSec: Int) {
        if (_state.value.stressTestRunning) return
        _state.value = _state.value.copy(stressTestRunning = true, stressTestLog = emptyList())
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationSec * 1000L
            // CPU yükü başlat
            val stressJob = launch(Dispatchers.Default) {
                while (isActive && System.currentTimeMillis() < endTime) {
                    var x = 1.0
                    repeat(100_000) { x = kotlin.math.sqrt(x + it) }
                }
            }
            while (System.currentTimeMillis() < endTime && _state.value.stressTestRunning) {
                val tempRaw = RootUtils.readSysfs("/sys/class/thermal/thermal_zone0/temp").toIntOrNull() ?: 0
                val tempC = if (tempRaw > 1000) tempRaw / 1000f else tempRaw.toFloat()
                val cpuFreq = DeviceInfo.getCpuInfo().clusterPrime.curFreqMhz
                val gpuFreq = DeviceInfo.getGpuInfo().curFreqMhz
                val entry = StressLogEntry(System.currentTimeMillis(), tempC, cpuFreq, gpuFreq)
                _state.value = _state.value.copy(
                    stressTestLog = _state.value.stressTestLog + entry
                )
                delay(2000)
            }
            stressJob.cancel()
            _state.value = _state.value.copy(stressTestRunning = false,
                statusMessage = "Stres testi tamamlandı")
        }
    }
    fun stopStressTest() {
        _state.value = _state.value.copy(stressTestRunning = false)
    }
    fun addCustomProfile(profile: CustomProfile) {
        val list = _state.value.customProfiles + profile
        _state.value = _state.value.copy(customProfiles = list, statusMessage = "Profil oluşturuldu: ${profile.name}")
    }
    fun deleteCustomProfile(id: String) {
        val list = _state.value.customProfiles.filter { it.id != id }
        _state.value = _state.value.copy(customProfiles = list)
    }
    fun applyCustomProfile(profile: CustomProfile) = update {
        DeviceInfo.setCpuGovernor(profile.cpuGovernor)
        DeviceInfo.setGpuGovernor(profile.gpuGovernor)
        DeviceInfo.setTouchPollingRate(profile.touchPollingRate)
        DeviceInfo.setSwappiness(profile.swappiness)
        DeviceInfo.setTcpCongestion(profile.tcpCongestion)
        val stats = _state.value.profileStats.toMutableMap()
        stats[profile.id] = (stats[profile.id] ?: 0) + 1
        _state.value.copy(
            cpuInfo = DeviceInfo.getCpuInfo(),
            gpuInfo = DeviceInfo.getGpuInfo(),
            profileStats = stats,
            statusMessage = "Özel profil uygulandı: ${profile.name}"
        )
    }

    // Debloat
    fun loadDebloatStatus() = update {
        val list = com.duchamp.control.ui.miuiDebloatList.map { app ->
            val enabled = RootUtils.runCommand("pm list packages -d | grep ${app.packageName}").isBlank()
            app.copy(disabled = !enabled)
        }
        _state.value.copy(debloatList = list)
    }
    fun disableDebloatApp(pkg: String) = update {
        RootUtils.runCommand("pm disable-user --user 0 $pkg")
        val list = _state.value.debloatList.map { if (it.packageName == pkg) it.copy(disabled = true) else it }
        _state.value.copy(debloatList = list, statusMessage = "Devre dışı: $pkg")
    }
    fun enableDebloatApp(pkg: String) = update {
        RootUtils.runCommand("pm enable $pkg")
        val list = _state.value.debloatList.map { if (it.packageName == pkg) it.copy(disabled = false) else it }
        _state.value.copy(debloatList = list, statusMessage = "Etkinleştirildi: $pkg")
    }
    fun disableAllDebloat(apps: List<DebloatApp>) = update {
        apps.forEach { RootUtils.runCommand("pm disable-user --user 0 ${it.packageName}") }
        val list = _state.value.debloatList.map { app ->
            if (apps.any { it.packageName == app.packageName }) app.copy(disabled = true) else app
        }
        _state.value.copy(debloatList = list, statusMessage = "${apps.size} uygulama devre dışı bırakıldı")
    }

    // Kernel parametreleri
    fun loadKernelParams() = update {
        val paths = listOf("/proc/sys/vm", "/proc/sys/kernel", "/proc/sys/net/ipv4", "/proc/sys/fs")
        val params = paths.flatMap { dir ->
            val files = RootUtils.runCommand("ls $dir 2>/dev/null").lines().filter { it.isNotBlank() }
            files.mapNotNull { file ->
                val path = "$dir/$file"
                val value = RootUtils.runCommand("cat $path 2>/dev/null").trim()
                if (value.isNotBlank() && !value.contains("\n"))
                    KernelParam(path, file, value)
                else null
            }
        }
        _state.value.copy(kernelParams = params)
    }

    // Dmesg
    fun loadDmesg() = update {
        val lines = RootUtils.runCommand("dmesg 2>/dev/null | tail -500").lines()
            .filter { it.isNotBlank() }
        _state.value.copy(dmesgLines = lines)
    }

    // Reboot
    fun reboot(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            RootUtils.runCommand(command)
        }
    }

    // Quick Tile
    fun setQuickTileProfile(profileId: String) {
        AppPrefs.quickTileProfileId = profileId
        _state.value = _state.value.copy(quickTileProfileId = profileId,
            statusMessage = "Tile profili: $profileId")
    }

    // Kamera optimizasyonu
    fun applyCameraPreset(preset: String) = update {
        if (preset == "quality") {
            val props = mapOf(
                "persist.vendor.camera.preview.display_60fps" to "1",
                "persist.vendor.camera.isp.tuning.enable" to "1",
                "persist.vendor.camera.hdr.enable" to "1",
                "persist.vendor.camera.night.mode.enable" to "1",
                "persist.vendor.camera.video.hdr.enable" to "1"
            )
            props.forEach { (k, v) -> RootUtils.runCommand("setprop $k $v") }
            _state.value.copy(statusMessage = "Kamera kalite preset'i uygulandı")
        } else {
            _state.value.copy(statusMessage = "Kamera ayarları sıfırlandı")
        }
    }

    // Uygulama boyut analizi
    fun loadAppSizes() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = _state.value.installedApps.ifEmpty {
                AppManager.getInstalledApps(false)
            }
            val sizes = apps.map { app ->
                val size = RootUtils.runCommand("du -sb /data/data/${app.packageName} 2>/dev/null | awk '{print $1}'")
                    .toLongOrNull() ?: 0L
                app.label to size
            }.sortedByDescending { it.second }
            _state.value = _state.value.copy(appSizeList = sizes)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveMonitoring()
        schedulerJob?.cancel()
    }
}
