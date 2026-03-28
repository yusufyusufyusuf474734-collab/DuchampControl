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

class MainViewModel(context: Context) : ViewModel() {

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
            scheduleRules = AppPrefs.loadScheduleRules()
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
    fun setGpuGovernor(gov: String) = update {
        DeviceInfo.setGpuGovernor(gov)
        _state.value.copy(gpuInfo = DeviceInfo.getGpuInfo(), statusMessage = "GPU governor: $gov")
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
        monitorJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val now = System.currentTimeMillis()
                _state.value = _state.value.copy(
                    cpuHistory = (_state.value.cpuHistory + LiveMetric(now, LiveMetrics.getCpuUsage())).takeLast(maxHistory),
                    gpuHistory = (_state.value.gpuHistory + LiveMetric(now, LiveMetrics.getGpuUsage())).takeLast(maxHistory),
                    ramHistory = (_state.value.ramHistory + LiveMetric(now, LiveMetrics.getRamUsagePct())).takeLast(maxHistory),
                    batteryHistory = (_state.value.batteryHistory + LiveMetric(now, LiveMetrics.getBatteryPct())).takeLast(maxHistory),
                    cpuTempHistory = (_state.value.cpuTempHistory + LiveMetric(now, LiveMetrics.getCpuTemp())).takeLast(maxHistory)
                )
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

    // Yardımcılar
    fun clearStatus() { _state.value = _state.value.copy(statusMessage = "") }
    fun refreshBattery() = update { _state.value.copy(batteryInfo = DeviceInfo.getBatteryInfo()) }
    fun refreshThermals() = update { _state.value.copy(thermals = DeviceInfo.getThermals()) }
    fun refreshCpu() = update { _state.value.copy(cpuInfo = DeviceInfo.getCpuInfo(), gpuInfo = DeviceInfo.getGpuInfo()) }

    override fun onCleared() {
        super.onCleared()
        stopLiveMonitoring()
        schedulerJob?.cancel()
    }
}
