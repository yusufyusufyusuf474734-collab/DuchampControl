package com.duchamp.control

import android.os.Build
import com.duchamp.control.RootUtils.hzToMhz
import com.duchamp.control.RootUtils.khzToMhz
import com.duchamp.control.RootUtils.tenthToC
import com.duchamp.control.RootUtils.uaToMa
import com.duchamp.control.RootUtils.uvToMv

// ── Data models ──────────────────────────────────────────────────────────────

data class CoreInfo(val id: Int, val freqMhz: String, val online: Boolean, val cluster: String)

data class CpuInfo(
    val governor: String,
    val curFreqMhz: String,
    val maxFreqMhz: String,
    val minFreqMhz: String,
    val availableGovernors: List<String>,
    val availableFreqs: List<String>,
    val cores: List<CoreInfo>,
    val schedTunables: Map<String, String>,
    // MT6897 cluster bazlı bilgi
    val clusterLittle: ClusterInfo,   // cpu0-3: A510 efficiency
    val clusterBig: ClusterInfo,      // cpu4-6: A715 performance
    val clusterPrime: ClusterInfo     // cpu7:   A715 prime
)

data class ClusterInfo(
    val name: String,
    val coreType: String,
    val coreRange: String,
    val curFreqMhz: String,
    val maxFreqMhz: String,
    val minFreqMhz: String,
    val maxFreqKhz: String,
    val availableFreqs: List<String>
)

data class GpuInfo(
    val governor: String,
    val curFreqMhz: String,
    val maxFreqMhz: String,
    val minFreqMhz: String,
    val utilization: String,
    val availableGovernors: List<String>
)

data class BatteryInfo(
    val capacity: Int,
    val status: String,
    val health: String,
    val tempC: String,
    val voltage: String,
    val currentNow: String,
    val technology: String,
    val cycleCount: String,
    val chargeLimit: String,
    val fastChargeEnabled: Boolean,
    val inputCurrentLimit: String
)

data class ThermalInfo(val zone: String, val tempC: String, val tempRaw: Int)

data class MemInfo(
    val totalMb: Long,
    val availMb: Long,
    val usedMb: Long,
    val swappiness: String,
    val zramSizeGb: String,
    val zramAlgo: String,
    val zramUsedMb: String
)

data class StorageInfo(
    val partition: String,
    val size: String,
    val used: String,
    val avail: String,
    val usePercent: String,
    val usePct: Float
)

data class NetworkInfo(
    val wifiIp: String,
    val wifiSignal: String,
    val wifiInterface: String,
    val tcpCongestion: String,
    val availableCongestion: List<String>,
    val dnsServers: List<String>,
    val wifiPowerSave: Boolean
)

data class DisplayInfo(
    val refreshRate: String,
    val availableRates: List<String>,
    val hbmEnabled: Boolean,
    val dcDimmingEnabled: Boolean,
    val colorProfile: String
)

data class AudioInfo(
    val dolbyEnabled: Boolean,
    val dolbyProfile: String,
    val speakerGain: String,
    val micGain: String
)

data class SystemInfo(
    val selinuxMode: String,
    val kernelVersion: String,
    val uptime: String,
    val loadAvg: String,
    val buildProps: Map<String, String>
)

data class LogcatLine(val level: String, val tag: String, val message: String)

// ── DeviceInfo object ────────────────────────────────────────────────────────

object DeviceInfo {

    // Device basic
    fun getDeviceBasic(): Map<String, String> = mapOf(
        "Model" to "${Build.MANUFACTURER} ${Build.MODEL}",
        "Codename" to "duchamp",
        "SoC" to "MediaTek Dimensity 8300 Ultra (MT6897)",
        "CPU" to "Octa-core Cortex-A715 + A510",
        "GPU" to "Mali G615-MC6",
        "Android" to Build.VERSION.RELEASE,
        "SDK" to Build.VERSION.SDK_INT.toString(),
        "Build" to Build.DISPLAY,
        "Fingerprint" to Build.FINGERPRINT.take(60)
    )

    // CPU — MT6897 cluster yapısı:
    // cpu0-3: Cortex-A510 (efficiency) → max 2200 MHz  → policy0
    // cpu4-6: Cortex-A715 (performance) → max 3200 MHz → policy4
    // cpu7:   Cortex-A715 (prime)       → max 3350 MHz → policy7
    fun getCpuInfo(): CpuInfo {
        // policy bazlı okuma (modülle uyumlu)
        fun policy(p: Int, file: String) =
            RootUtils.readSysfs("/sys/devices/system/cpu/cpufreq/policy$p/$file")
        // fallback: cpu bazlı
        fun sysfs(cpu: Int, file: String) =
            RootUtils.readSysfs("/sys/devices/system/cpu/cpu$cpu/cpufreq/$file")

        fun readFreq(p: Int, file: String): String {
            val v = policy(p, file).ifEmpty { sysfs(p, file) }
            return v.toLongOrNull()?.khzToMhz() ?: "N/A"
        }
        fun readFreqKhz(p: Int, file: String): String =
            policy(p, file).ifEmpty { sysfs(p, file) }

        fun readAvailFreqs(p: Int): List<String> {
            val raw = policy(p, "scaling_available_frequencies").ifEmpty {
                sysfs(p, "scaling_available_frequencies")
            }
            return raw.split(" ").filter { it.isNotBlank() }
                .map { it.toLongOrNull()?.khzToMhz() ?: it }
        }

        val little = ClusterInfo(
            name = "Little", coreType = "Cortex-A510", coreRange = "cpu0-3",
            curFreqMhz = readFreq(0, "scaling_cur_freq"),
            maxFreqMhz = readFreq(0, "scaling_max_freq"),
            minFreqMhz = readFreq(0, "scaling_min_freq"),
            maxFreqKhz = readFreqKhz(0, "scaling_max_freq").ifEmpty { "2200000" },
            availableFreqs = readAvailFreqs(0)
        )
        val big = ClusterInfo(
            name = "Big", coreType = "Cortex-A715", coreRange = "cpu4-6",
            curFreqMhz = readFreq(4, "scaling_cur_freq"),
            maxFreqMhz = readFreq(4, "scaling_max_freq"),
            minFreqMhz = readFreq(4, "scaling_min_freq"),
            maxFreqKhz = readFreqKhz(4, "scaling_max_freq").ifEmpty { "3200000" },
            availableFreqs = readAvailFreqs(4)
        )
        val prime = ClusterInfo(
            name = "Prime", coreType = "Cortex-A715", coreRange = "cpu7",
            curFreqMhz = readFreq(7, "scaling_cur_freq"),
            maxFreqMhz = readFreq(7, "scaling_max_freq"),
            minFreqMhz = readFreq(7, "scaling_min_freq"),
            maxFreqKhz = readFreqKhz(7, "scaling_max_freq").ifEmpty { "3350000" },
            availableFreqs = readAvailFreqs(7)
        )

        val governor = policy(0, "scaling_governor").ifEmpty { sysfs(0, "scaling_governor") }
        val governors = policy(0, "scaling_available_governors")
            .ifEmpty { sysfs(0, "scaling_available_governors") }
            .split(" ").filter { it.isNotBlank() }

        val coreCount = Runtime.getRuntime().availableProcessors()
        val cores = (0 until coreCount).map { i ->
            val online = RootUtils.readSysfs("/sys/devices/system/cpu/cpu$i/online") == "1"
            val policyId = when (i) { in 0..3 -> 0; in 4..6 -> 4; else -> 7 }
            val freq = readFreq(policyId, "scaling_cur_freq")
            val cluster = when (i) { in 0..3 -> "Little"; in 4..6 -> "Big"; 7 -> "Prime"; else -> "?" }
            CoreInfo(i, freq, online || i == 0, cluster)
        }

        val schedTunables = mapOf(
            "sched_boost" to RootUtils.readSysfs("/proc/sys/kernel/sched_boost"),
            "sched_latency_ns" to RootUtils.readSysfs("/proc/sys/kernel/sched_latency_ns"),
            "sched_min_granularity_ns" to RootUtils.readSysfs("/proc/sys/kernel/sched_min_granularity_ns")
        )

        return CpuInfo(
            governor = governor,
            curFreqMhz = prime.curFreqMhz,
            maxFreqMhz = prime.maxFreqMhz,
            minFreqMhz = little.minFreqMhz,
            availableGovernors = governors,
            availableFreqs = prime.availableFreqs,
            cores = cores,
            schedTunables = schedTunables,
            clusterLittle = little,
            clusterBig = big,
            clusterPrime = prime
        )
    }

    fun setCpuGovernor(gov: String): Boolean {
        var ok = true
        // policy bazlı (modülle uyumlu)
        for (p in listOf(0, 4, 7)) {
            if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy$p/scaling_governor", gov)) ok = false
        }
        return ok
    }

    fun setCpuMaxFreqCluster(littleKhz: String, bigKhz: String, primeKhz: String): Boolean {
        var ok = true
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq", littleKhz)) ok = false
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq", bigKhz)) ok = false
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq", primeKhz)) ok = false
        return ok
    }

    fun setCpuMaxFreq(freqKhz: String): Boolean = setCpuMaxFreqCluster(freqKhz, freqKhz, freqKhz)

    fun setCpuMinFreqCluster(littleKhz: String, bigKhz: String, primeKhz: String): Boolean {
        var ok = true
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq", littleKhz)) ok = false
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq", bigKhz)) ok = false
        if (!RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq", primeKhz)) ok = false
        return ok
    }

    fun setCpuMinFreq(freqKhz: String): Boolean = setCpuMinFreqCluster(freqKhz, freqKhz, freqKhz)

    fun setCpuMaxFreqSingle(policyOrCore: Int, freqKhz: String): Boolean {
        val policy = when (policyOrCore) { in 0..3 -> 0; in 4..6 -> 4; else -> 7 }
        return RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy$policy/scaling_max_freq", freqKhz)
    }

    fun unlockPrimeCore(): Boolean =
        RootUtils.writeFile("/sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq", "3350000")

    // GPU — Mali G615-MC6
    // GPU path: /sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali
    fun getGpuInfo(): GpuInfo {
        val base = "/sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali"
        val fallback = "/sys/class/misc/mali0/device/devfreq/devfreq0"
        val path = if (RootUtils.runCommand("[ -d $base ] && echo 1 || echo 0") == "1") base else fallback
        val gov = RootUtils.readSysfs("$path/governor")
        val cur = RootUtils.readSysfs("$path/cur_freq").toLongOrNull()?.hzToMhz() ?: "N/A"
        val max = RootUtils.readSysfs("$path/max_freq").toLongOrNull()?.hzToMhz() ?: "N/A"
        val min = RootUtils.readSysfs("$path/min_freq").toLongOrNull()?.hzToMhz() ?: "N/A"
        val util = RootUtils.readSysfs("$path/../utilization").let {
            if (it != "N/A") "$it%" else "N/A"
        }
        val govs = RootUtils.readSysfs("$path/available_governors").split(" ").filter { it.isNotBlank() }
        return GpuInfo(gov, cur, max, min, util, govs)
    }

    fun setGpuGovernor(gov: String): Boolean {
        val base = "/sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali"
        val fallback = "/sys/class/misc/mali0/device/devfreq/devfreq0"
        val path = if (RootUtils.runCommand("[ -d $base ] && echo 1 || echo 0") == "1") base else fallback
        return RootUtils.writeFile("$path/governor", gov)
    }

    fun setGpuMaxFreq(hz: String): Boolean {
        val base = "/sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali"
        return RootUtils.writeFile("$base/max_freq", hz)
    }

    fun setGpuMinFreq(hz: String): Boolean {
        val base = "/sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali"
        return RootUtils.writeFile("$base/min_freq", hz)
    }

    // Battery
    fun getBatteryInfo(): BatteryInfo {
        val base = "/sys/class/power_supply/battery"
        val tempC = RootUtils.readSysfs("$base/temp").toIntOrNull()?.tenthToC() ?: "N/A"
        val volt = RootUtils.readSysfs("$base/voltage_now").toLongOrNull()?.uvToMv() ?: "N/A"
        val curr = RootUtils.readSysfs("$base/current_now").toLongOrNull()?.uaToMa() ?: "N/A"
        val cap = RootUtils.readSysfs("$base/capacity").toIntOrNull() ?: 0
        val fastCharge = RootUtils.readSysfs("$base/fast_charge_enable") == "1"
        val inputLimit = RootUtils.readSysfs("$base/input_current_limit").toLongOrNull()?.let { "${it / 1000} mA" } ?: "N/A"
        val chargeLimit = RootUtils.readSysfs("$base/charge_control_limit")
        return BatteryInfo(
            capacity = cap,
            status = RootUtils.readSysfs("$base/status"),
            health = RootUtils.readSysfs("$base/health"),
            tempC = tempC,
            voltage = volt,
            currentNow = curr,
            technology = RootUtils.readSysfs("$base/technology"),
            cycleCount = RootUtils.readSysfs("$base/cycle_count"),
            chargeLimit = if (chargeLimit != "N/A") "$chargeLimit%" else "100%",
            fastChargeEnabled = fastCharge,
            inputCurrentLimit = inputLimit
        )
    }

    fun setChargeLimit(pct: Int): Boolean =
        RootUtils.writeFile("/sys/class/power_supply/battery/charge_control_limit", pct.toString())

    fun setFastCharge(enabled: Boolean): Boolean =
        RootUtils.writeFile("/sys/class/power_supply/battery/fast_charge_enable", if (enabled) "1" else "0")

    // Thermals
    fun getThermals(): List<ThermalInfo> {
        return (0 until 30).mapNotNull { i ->
            val type = RootUtils.readSysfs("/sys/class/thermal/thermal_zone$i/type")
            val temp = RootUtils.readSysfs("/sys/class/thermal/thermal_zone$i/temp")
            if (type == "N/A" || temp == "N/A") null
            else {
                val raw = temp.toIntOrNull() ?: 0
                val tempC = if (raw > 1000) "${raw / 1000.0}°C" else "${raw}°C"
                ThermalInfo(type, tempC, raw)
            }
        }
    }

    // Memory
    fun getMemInfo(): MemInfo {
        val raw = RootUtils.runCommand("cat /proc/meminfo")
        fun extract(key: String): Long {
            val line = raw.lines().find { it.startsWith(key) } ?: return 0L
            return line.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
        }
        val total = extract("MemTotal:")
        val avail = extract("MemAvailable:")
        val swappiness = RootUtils.readSysfs("/proc/sys/vm/swappiness")
        val zramSize = RootUtils.runCommand("cat /sys/block/zram0/disksize").toLongOrNull()
            ?.let { "${it / 1024 / 1024 / 1024} GB" } ?: "N/A"
        val zramAlgo = RootUtils.readSysfs("/sys/block/zram0/comp_algorithm")
        val zramUsed = RootUtils.runCommand("cat /sys/block/zram0/mem_used_total").toLongOrNull()
            ?.let { "${it / 1024 / 1024} MB" } ?: "N/A"
        return MemInfo(total / 1024, avail / 1024, (total - avail) / 1024, swappiness, zramSize, zramAlgo, zramUsed)
    }

    fun setSwappiness(value: Int): Boolean =
        RootUtils.writeFile("/proc/sys/vm/swappiness", value.toString())

    fun setZramAlgo(algo: String): Boolean {
        RootUtils.runCommand("swapoff /dev/block/zram0 2>/dev/null")
        RootUtils.writeFile("/sys/block/zram0/reset", "1")
        RootUtils.writeFile("/sys/block/zram0/comp_algorithm", algo)
        return RootUtils.runCommand("mkswap /dev/block/zram0 && swapon /dev/block/zram0").isNotEmpty()
    }

    // Storage
    fun getStorageInfo(): List<StorageInfo> {
        val raw = RootUtils.runCommand("df -h /data /cache /system /vendor /product 2>/dev/null")
        return raw.lines().drop(1).mapNotNull { line ->
            val p = line.trim().split(Regex("\\s+"))
            if (p.size >= 6) {
                val pct = p[4].replace("%", "").toFloatOrNull()?.div(100f) ?: 0f
                StorageInfo(p[5], p[1], p[2], p[3], p[4], pct)
            } else null
        }
    }

    // I/O Scheduler
    fun getIoScheduler(): String = RootUtils.readSysfs("/sys/block/sda/queue/scheduler")
    fun getAvailableIoSchedulers(): List<String> {
        val raw = getIoScheduler()
        return Regex("\\[?(\\w+)\\]?").findAll(raw).map { it.groupValues[1] }.toList()
    }
    fun setIoScheduler(sched: String): Boolean =
        RootUtils.writeFile("/sys/block/sda/queue/scheduler", sched)

    // Touch
    fun getTouchPollingRate(): String = RootUtils.readSysfs("/sys/devices/platform/goodix_ts.0/switch_report_rate")
    fun setTouchPollingRate(rate: Int): Boolean =
        RootUtils.writeFile("/sys/devices/platform/goodix_ts.0/switch_report_rate", rate.toString())

    // Display
    fun getDisplayInfo(): DisplayInfo {
        val rate = RootUtils.runCommand("dumpsys display | grep 'mRefreshRate' | head -1 | awk '{print $" + "NF}'")
        val hbm = RootUtils.readSysfs("/sys/class/drm/card0-DSI-1/hbm_mode")
        val dc = RootUtils.readSysfs("/sys/class/drm/card0-DSI-1/dc_mode")
        val color = RootUtils.readSysfs("/sys/class/drm/card0-DSI-1/color_mode")
        return DisplayInfo(
            refreshRate = if (rate.isNotEmpty()) "$rate Hz" else "N/A",
            availableRates = listOf("60", "90", "120"),
            hbmEnabled = hbm == "1",
            dcDimmingEnabled = dc == "1",
            colorProfile = color.ifEmpty { "N/A" }
        )
    }

    fun setHbm(enabled: Boolean): Boolean =
        RootUtils.writeFile("/sys/class/drm/card0-DSI-1/hbm_mode", if (enabled) "1" else "0")

    fun setDcDimming(enabled: Boolean): Boolean =
        RootUtils.writeFile("/sys/class/drm/card0-DSI-1/dc_mode", if (enabled) "1" else "0")

    fun setRefreshRate(hz: Int): Boolean =
        RootUtils.runCommand("service call SurfaceFlinger 1035 i32 $hz").isNotEmpty()

    // Network
    fun getNetworkInfo(): NetworkInfo {
        val ip = RootUtils.runCommand("ip addr show wlan0 | grep 'inet ' | awk '{print $2}'")
        val signal = RootUtils.runCommand("cat /proc/net/wireless 2>/dev/null | tail -1 | awk '{print $3}'")
        val iface = RootUtils.runCommand("ip route | grep default | awk '{print $5}' | head -1")
        val tcp = RootUtils.readSysfs("/proc/sys/net/ipv4/tcp_congestion_control")
        val tcpAvail = RootUtils.readSysfs("/proc/sys/net/ipv4/tcp_available_congestion_control")
            .split(" ").filter { it.isNotBlank() }
        val dns1 = RootUtils.runCommand("getprop net.dns1")
        val dns2 = RootUtils.runCommand("getprop net.dns2")
        val powerSave = RootUtils.runCommand("iw dev wlan0 get power_save 2>/dev/null").contains("on")
        return NetworkInfo(
            wifiIp = ip.ifEmpty { "Bağlı değil" },
            wifiSignal = if (signal.isNotEmpty()) "$signal dBm" else "N/A",
            wifiInterface = iface.ifEmpty { "N/A" },
            tcpCongestion = tcp,
            availableCongestion = tcpAvail,
            dnsServers = listOf(dns1, dns2).filter { it.isNotBlank() },
            wifiPowerSave = powerSave
        )
    }

    fun setTcpCongestion(algo: String): Boolean =
        RootUtils.writeFile("/proc/sys/net/ipv4/tcp_congestion_control", algo)

    fun setDns(dns1: String, dns2: String): Boolean {
        RootUtils.runCommand("setprop net.dns1 $dns1")
        RootUtils.runCommand("setprop net.dns2 $dns2")
        return true
    }

    fun setWifiPowerSave(enabled: Boolean): Boolean =
        RootUtils.runCommand("iw dev wlan0 set power_save ${if (enabled) "on" else "off"}").let { true }

    // Audio
    fun getAudioInfo(): AudioInfo {
        val dolby = RootUtils.runCommand("getprop persist.vendor.audio.dolby.enable")
        val profile = RootUtils.runCommand("getprop persist.vendor.audio.dolby.profile")
        val spkGain = RootUtils.readSysfs("/sys/kernel/sound_control/speaker_gain")
        val micGain = RootUtils.readSysfs("/sys/kernel/sound_control/mic_gain")
        return AudioInfo(
            dolbyEnabled = dolby == "1" || dolby == "true",
            dolbyProfile = profile.ifEmpty { "0" },
            speakerGain = spkGain,
            micGain = micGain
        )
    }

    fun setDolbyEnabled(enabled: Boolean): Boolean =
        RootUtils.runCommand("setprop persist.vendor.audio.dolby.enable ${if (enabled) "1" else "0"}").let { true }

    fun setDolbyProfile(profile: String): Boolean =
        RootUtils.runCommand("setprop persist.vendor.audio.dolby.profile $profile").let { true }

    fun setSpeakerGain(gain: Int): Boolean =
        RootUtils.writeFile("/sys/kernel/sound_control/speaker_gain", gain.toString())

    fun setMicGain(gain: Int): Boolean =
        RootUtils.writeFile("/sys/kernel/sound_control/mic_gain", gain.toString())

    // System
    fun getSystemInfo(): SystemInfo {
        val selinux = RootUtils.runCommand("getenforce")
        val kernel = RootUtils.runCommand("uname -r")
        val uptime = RootUtils.runCommand("uptime -p")
        val load = RootUtils.runCommand("cat /proc/loadavg | awk '{print $1, $2, $3}'")
        val props = mapOf(
            "ro.build.version.release" to RootUtils.runCommand("getprop ro.build.version.release"),
            "ro.build.display.id" to RootUtils.runCommand("getprop ro.build.display.id"),
            "ro.product.model" to RootUtils.runCommand("getprop ro.product.model"),
            "ro.vendor.build.security_patch" to RootUtils.runCommand("getprop ro.vendor.build.security_patch"),
            "ro.build.type" to RootUtils.runCommand("getprop ro.build.type"),
            "ro.debuggable" to RootUtils.runCommand("getprop ro.debuggable"),
            "persist.sys.timezone" to RootUtils.runCommand("getprop persist.sys.timezone"),
            "ro.boot.verifiedbootstate" to RootUtils.runCommand("getprop ro.boot.verifiedbootstate")
        )
        return SystemInfo(selinux, kernel, uptime, load, props)
    }

    fun setSELinux(enforcing: Boolean): Boolean =
        RootUtils.runCommand("setenforce ${if (enforcing) "1" else "0"}").let { true }

    fun setProp(key: String, value: String): Boolean =
        RootUtils.runCommand("setprop $key $value").let { true }

    // Logcat
    fun getLogcat(lines: Int = 200): List<LogcatLine> {
        val raw = RootUtils.runCommand("logcat -d -t $lines")
        return raw.lines().mapNotNull { line ->
            val parts = line.split(Regex("\\s+"), limit = 6)
            if (parts.size >= 6) {
                val level = parts[4].firstOrNull()?.toString() ?: "?"
                val tag = parts[5].substringBefore(":")
                val msg = parts[5].substringAfter(": ", "")
                LogcatLine(level, tag, msg)
            } else null
        }
    }

    // NFC
    fun getNfcEnabled(): Boolean = RootUtils.runCommand("getprop sys.nfc.state") == "on"
    fun setNfcEnabled(enabled: Boolean): Boolean =
        RootUtils.runCommand("svc nfc ${if (enabled) "enable" else "disable"}").let { true }

    // IR Blaster
    fun sendIrSignal(frequency: Int, pattern: String): Boolean =
        RootUtils.runCommand("echo '$frequency $pattern' > /dev/lirc0").let { true }

    // Kernel modules
    fun getKernelModules(): List<String> =
        RootUtils.runCommand("lsmod").lines().drop(1)
            .filter { it.isNotBlank() }
            .map { it.split(Regex("\\s+")).firstOrNull() ?: "" }
            .filter { it.isNotBlank() }
}

// ── Magisk / KernelSU ────────────────────────────────────────────────────────

data class MagiskModule(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val enabled: Boolean
)

data class RootInfo(
    val rootType: String,       // "Magisk", "KernelSU", "None"
    val version: String,
    val versionCode: String,
    val modules: List<MagiskModule>,
    val bootloaderState: String,
    val verifiedBootState: String,
    val dmVerity: String
)

object MagiskInfo {
    fun getRootInfo(): RootInfo {
        val magiskVer = RootUtils.runCommand("magisk -v 2>/dev/null")
        val magiskCode = RootUtils.runCommand("magisk -V 2>/dev/null")
        val ksuVer = RootUtils.runCommand("ksud -V 2>/dev/null")

        val rootType = when {
            magiskVer.isNotEmpty() && !magiskVer.contains("not found") -> "Magisk"
            ksuVer.isNotEmpty() && !ksuVer.contains("not found") -> "KernelSU"
            else -> "Bilinmiyor"
        }

        val modules = if (rootType == "Magisk") getMagiskModules() else getKsuModules()

        return RootInfo(
            rootType = rootType,
            version = if (rootType == "Magisk") magiskVer else ksuVer,
            versionCode = magiskCode,
            modules = modules,
            bootloaderState = RootUtils.runCommand("getprop ro.boot.flash.locked"),
            verifiedBootState = RootUtils.runCommand("getprop ro.boot.verifiedbootstate"),
            dmVerity = RootUtils.runCommand("getprop ro.boot.veritymode")
        )
    }

    private fun getMagiskModules(): List<MagiskModule> {
        val dirs = RootUtils.runCommand("ls /data/adb/modules 2>/dev/null").lines().filter { it.isNotBlank() }
        return dirs.map { id ->
            val base = "/data/adb/modules/$id"
            val props = RootUtils.runCommand("cat $base/module.prop 2>/dev/null")
            fun prop(key: String) = props.lines()
                .find { it.startsWith("$key=") }?.substringAfter("=") ?: ""
            val disabled = RootUtils.runCommand("[ -f $base/disable ] && echo 1 || echo 0") == "1"
            MagiskModule(
                id = id,
                name = prop("name").ifEmpty { id },
                version = prop("version"),
                author = prop("author"),
                description = prop("description"),
                enabled = !disabled
            )
        }
    }

    private fun getKsuModules(): List<MagiskModule> {
        val dirs = RootUtils.runCommand("ls /data/adb/ksu/modules 2>/dev/null").lines().filter { it.isNotBlank() }
        return dirs.map { id ->
            val base = "/data/adb/ksu/modules/$id"
            val props = RootUtils.runCommand("cat $base/module.prop 2>/dev/null")
            fun prop(key: String) = props.lines()
                .find { it.startsWith("$key=") }?.substringAfter("=") ?: ""
            val disabled = RootUtils.runCommand("[ -f $base/disable ] && echo 1 || echo 0") == "1"
            MagiskModule(
                id = id,
                name = prop("name").ifEmpty { id },
                version = prop("version"),
                author = prop("author"),
                description = prop("description"),
                enabled = !disabled
            )
        }
    }

    fun setModuleEnabled(id: String, enabled: Boolean, rootType: String): Boolean {
        val base = if (rootType == "KernelSU") "/data/adb/ksu/modules/$id"
                   else "/data/adb/modules/$id"
        return if (enabled) {
            RootUtils.runCommand("rm -f $base/disable").let { true }
        } else {
            RootUtils.runCommand("touch $base/disable").let { true }
        }
    }
}

// ── Kernel Tweaks ────────────────────────────────────────────────────────────

data class KernelTweaks(
    val dirtyRatio: String,
    val dirtyBgRatio: String,
    val rmemMax: String,
    val wmemMax: String,
    val perfEventParanoid: String,
    val inotifyMaxWatches: String,
    val tcpFastOpen: String,
    val vmOvercommit: String,
    val schedChildRunsFirst: String,
    val randomizeVaSpace: String
)

object KernelTweakInfo {
    fun get(): KernelTweaks = KernelTweaks(
        dirtyRatio         = RootUtils.readSysfs("/proc/sys/vm/dirty_ratio"),
        dirtyBgRatio       = RootUtils.readSysfs("/proc/sys/vm/dirty_background_ratio"),
        rmemMax            = RootUtils.readSysfs("/proc/sys/net/core/rmem_max"),
        wmemMax            = RootUtils.readSysfs("/proc/sys/net/core/wmem_max"),
        perfEventParanoid  = RootUtils.readSysfs("/proc/sys/kernel/perf_event_paranoid"),
        inotifyMaxWatches  = RootUtils.readSysfs("/proc/sys/fs/inotify/max_user_watches"),
        tcpFastOpen        = RootUtils.readSysfs("/proc/sys/net/ipv4/tcp_fastopen"),
        vmOvercommit       = RootUtils.readSysfs("/proc/sys/vm/overcommit_memory"),
        schedChildRunsFirst = RootUtils.readSysfs("/proc/sys/kernel/sched_child_runs_first"),
        randomizeVaSpace   = RootUtils.readSysfs("/proc/sys/kernel/randomize_va_space")
    )

    fun set(key: String, value: String): Boolean = RootUtils.writeFile(key, value)
}

// ── Performans Profilleri ────────────────────────────────────────────────────

data class PerfProfile(
    val id: String,
    val name: String,
    val icon: String,
    val cpuGovernor: String,
    val gpuGovernor: String,
    val touchPollingRate: Int,
    val swappiness: Int,
    val tcpCongestion: String,
    val description: String
)

object PerformanceProfiles {
    val presets = listOf(
        PerfProfile(
            id = "powersave",
            name = "Pil Tasarrufu",
            icon = "battery",
            cpuGovernor = "powersave",
            gpuGovernor = "simple_ondemand",
            touchPollingRate = 60,
            swappiness = 10,
            tcpCongestion = "cubic",
            description = "Maksimum pil ömrü, düşük performans"
        ),
        PerfProfile(
            id = "balanced",
            name = "Dengeli",
            icon = "balance",
            cpuGovernor = "schedutil",
            gpuGovernor = "simple_ondemand",
            touchPollingRate = 120,
            swappiness = 60,
            tcpCongestion = "cubic",
            description = "Günlük kullanım için ideal denge"
        ),
        PerfProfile(
            id = "performance",
            name = "Performans",
            icon = "speed",
            cpuGovernor = "performance",
            gpuGovernor = "performance",
            touchPollingRate = 240,
            swappiness = 100,
            tcpCongestion = "bbr",
            description = "Yüksek performans, daha fazla pil tüketimi"
        ),
        PerfProfile(
            id = "gaming",
            name = "Oyun",
            icon = "gamepad",
            cpuGovernor = "performance",
            gpuGovernor = "performance",
            touchPollingRate = 360,
            swappiness = 160,
            tcpCongestion = "bbr",
            description = "Maksimum FPS ve düşük gecikme"
        )
    )

    fun apply(profile: PerfProfile) {
        DeviceInfo.setCpuGovernor(profile.cpuGovernor)
        DeviceInfo.setGpuGovernor(profile.gpuGovernor)
        DeviceInfo.setTouchPollingRate(profile.touchPollingRate)
        DeviceInfo.setSwappiness(profile.swappiness)
        DeviceInfo.setTcpCongestion(profile.tcpCongestion)
    }
}

// ── Canlı Metrikler (grafik için) ────────────────────────────────────────────

data class LiveMetric(val timestamp: Long, val value: Float)

object LiveMetrics {
    fun getCpuUsage(): Float {
        val raw = RootUtils.runCommand("cat /proc/stat | head -1")
        val parts = raw.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
        if (parts.size < 4) return 0f
        val idle = parts[3]
        val total = parts.sum()
        return if (total > 0) ((total - idle).toFloat() / total.toFloat()) * 100f else 0f
    }

    fun getGpuUsage(): Float {
        val raw = RootUtils.readSysfs("/sys/class/misc/mali0/device/utilization")
        return raw.toFloatOrNull() ?: 0f
    }

    fun getRamUsagePct(): Float {
        val raw = RootUtils.runCommand("cat /proc/meminfo")
        fun extract(key: String): Long {
            val line = raw.lines().find { it.startsWith(key) } ?: return 0L
            return line.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
        }
        val total = extract("MemTotal:")
        val avail = extract("MemAvailable:")
        return if (total > 0) ((total - avail).toFloat() / total.toFloat()) * 100f else 0f
    }

    fun getBatteryPct(): Float {
        return RootUtils.readSysfs("/sys/class/power_supply/battery/capacity").toFloatOrNull() ?: 0f
    }

    fun getCpuTemp(): Float {
        val raw = RootUtils.readSysfs("/sys/class/thermal/thermal_zone0/temp")
        val v = raw.toIntOrNull() ?: return 0f
        return if (v > 1000) v / 1000f else v.toFloat()
    }
}

// ── Uygulama Yöneticisi ──────────────────────────────────────────────────────

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val installedSize: String,
    val dataSize: String,
    val targetSdk: String
)

object AppManager {

    fun getInstalledApps(includeSystem: Boolean = false): List<AppInfo> {
        val flag = if (includeSystem) "" else "-3"
        val raw = RootUtils.runCommand("pm list packages $flag -f 2>/dev/null")
        return raw.lines()
            .filter { it.startsWith("package:") }
            .mapNotNull { line ->
                val pkg = line.substringAfterLast("=").trim()
                if (pkg.isEmpty()) return@mapNotNull null
                val label = RootUtils.runCommand(
                    "dumpsys package $pkg 2>/dev/null | grep 'applicationInfo' | head -1"
                ).let { pkg } // label için basit fallback
                val enabled = RootUtils.runCommand(
                    "pm list packages -e 2>/dev/null | grep $pkg"
                ).isNotEmpty()
                val versionName = RootUtils.runCommand(
                    "dumpsys package $pkg 2>/dev/null | grep 'versionName' | head -1 | awk -F= '{print $2}'"
                ).trim()
                val targetSdk = RootUtils.runCommand(
                    "dumpsys package $pkg 2>/dev/null | grep 'targetSdk' | head -1 | awk -F= '{print $2}'"
                ).trim()
                val isSystem = line.contains("/system/") || line.contains("/product/")
                AppInfo(
                    packageName = pkg,
                    label = pkg.substringAfterLast("."),
                    versionName = versionName.ifEmpty { "N/A" },
                    isSystem = isSystem,
                    isEnabled = enabled,
                    installedSize = "N/A",
                    dataSize = "N/A",
                    targetSdk = targetSdk.ifEmpty { "N/A" }
                )
            }
            .sortedWith(compareBy({ it.isSystem }, { it.packageName }))
    }

    fun forceStop(pkg: String): Boolean =
        RootUtils.runCommand("am force-stop $pkg").let { true }

    fun clearData(pkg: String): Boolean =
        RootUtils.runCommand("pm clear $pkg").contains("Success")

    fun disableApp(pkg: String): Boolean =
        RootUtils.runCommand("pm disable-user --user 0 $pkg").contains("disabled")

    fun enableApp(pkg: String): Boolean =
        RootUtils.runCommand("pm enable $pkg").contains("enabled")

    fun uninstallApp(pkg: String): Boolean =
        RootUtils.runCommand("pm uninstall --user 0 $pkg").contains("Success")

    fun freezeApp(pkg: String): Boolean =
        RootUtils.runCommand("pm suspend --user 0 $pkg").let { true }

    fun unfreezeApp(pkg: String): Boolean =
        RootUtils.runCommand("pm unsuspend --user 0 $pkg").let { true }

    fun getAppDetails(pkg: String): Map<String, String> {
        val raw = RootUtils.runCommand("dumpsys package $pkg 2>/dev/null")
        val result = mutableMapOf<String, String>()
        raw.lines().forEach { line ->
            when {
                line.contains("versionName=") -> result["Versiyon"] = line.substringAfter("versionName=").trim()
                line.contains("targetSdk=")   -> result["Target SDK"] = line.substringAfter("targetSdk=").trim()
                line.contains("minSdk=")      -> result["Min SDK"] = line.substringAfter("minSdk=").trim()
                line.contains("firstInstallTime=") -> result["İlk Kurulum"] = line.substringAfter("firstInstallTime=").trim()
                line.contains("lastUpdateTime=")   -> result["Son Güncelleme"] = line.substringAfter("lastUpdateTime=").trim()
                line.contains("userId=")      -> result["User ID"] = line.substringAfter("userId=").trim()
            }
        }
        return result
    }
}

// ── Doze Yöneticisi ──────────────────────────────────────────────────────────

object DozeManager {

    fun getWhitelist(): List<String> {
        val raw = RootUtils.runCommand("dumpsys deviceidle whitelist 2>/dev/null")
        return raw.lines()
            .filter { it.trim().startsWith("system-excidle,") || it.trim().startsWith("user,") }
            .map { it.trim().substringAfterLast(",").trim() }
            .filter { it.isNotBlank() }
    }

    fun addToWhitelist(pkg: String): Boolean =
        RootUtils.runCommand("dumpsys deviceidle whitelist +$pkg").contains(pkg)

    fun removeFromWhitelist(pkg: String): Boolean =
        RootUtils.runCommand("dumpsys deviceidle whitelist -$pkg").let { true }

    fun isDozeEnabled(): Boolean {
        val raw = RootUtils.runCommand("dumpsys deviceidle 2>/dev/null | grep 'mEnabled' | head -1")
        return raw.contains("true") || raw.contains("1")
    }

    fun forceDoze(): Boolean =
        RootUtils.runCommand("dumpsys deviceidle force-idle").let { true }

    fun exitDoze(): Boolean =
        RootUtils.runCommand("dumpsys deviceidle unforce").let { true }

    fun getDozeState(): String =
        RootUtils.runCommand("dumpsys deviceidle 2>/dev/null | grep 'mState=' | head -1")
            .substringAfter("mState=").trim().ifEmpty { "N/A" }

    // Popüler uygulamalar için hızlı ekleme
    val popularApps = listOf(
        "com.whatsapp"                    to "WhatsApp",
        "com.whatsapp.w4b"               to "WhatsApp Business",
        "org.telegram.messenger"          to "Telegram",
        "com.instagram.android"           to "Instagram",
        "com.twitter.android"             to "Twitter/X",
        "com.google.android.gm"           to "Gmail",
        "com.google.android.apps.messaging" to "Messages",
        "com.viber.voip"                  to "Viber",
        "com.skype.raider"                to "Skype",
        "com.discord"                     to "Discord",
        "com.spotify.music"               to "Spotify",
        "com.netflix.mediaclient"         to "Netflix",
        "com.google.android.youtube"      to "YouTube"
    )
}

// ── Güvenlik ─────────────────────────────────────────────────────────────────

object SecurityManager {

    fun getSecurityInfo(): SecurityInfo {
        val blLocked = RootUtils.runCommand("getprop ro.boot.flash.locked") == "1"
        val vbState  = RootUtils.runCommand("getprop ro.boot.verifiedbootstate")
        val dmVerity = RootUtils.runCommand("getprop ro.boot.veritymode")
        val selinux  = RootUtils.runCommand("getenforce")
        val adb      = RootUtils.runCommand("getprop persist.sys.usb.config").contains("adb")
        val devOpts  = RootUtils.runCommand("getprop persist.sys.developerOptions") == "1" ||
                       RootUtils.runCommand("settings get global development_settings_enabled") == "1"

        val certs = RootUtils.runCommand(
            "ls /data/misc/user/0/cacerts-added/ 2>/dev/null"
        ).lines().filter { it.isNotBlank() }

        val logs = getRootAccessLogs()

        return SecurityInfo(
            bootloaderLocked = blLocked,
            verifiedBootState = vbState.ifEmpty { "N/A" },
            dmVerity = dmVerity.ifEmpty { "N/A" },
            selinuxMode = selinux.ifEmpty { "N/A" },
            adbEnabled = adb,
            developerOptions = devOpts,
            installedCerts = certs,
            rootAccessLogs = logs
        )
    }

    private fun getRootAccessLogs(): List<RootAccessLog> {
        // Magisk log
        val magiskLog = RootUtils.runCommand(
            "cat /data/adb/magisk/log 2>/dev/null | grep -E 'grant|deny' | tail -30"
        )
        // KernelSU log
        val ksuLog = RootUtils.runCommand(
            "cat /data/adb/ksu/log 2>/dev/null | tail -30"
        )
        val combined = (magiskLog + "\n" + ksuLog).lines().filter { it.isNotBlank() }
        return combined.mapNotNull { line ->
            val granted = line.contains("grant", ignoreCase = true)
            val denied  = line.contains("deny",  ignoreCase = true)
            if (!granted && !denied) return@mapNotNull null
            val pkg = Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+").find(line)?.value ?: return@mapNotNull null
            RootAccessLog(
                timestamp = System.currentTimeMillis(),
                packageName = pkg,
                appLabel = pkg.substringAfterLast("."),
                granted = granted
            )
        }.take(50)
    }

    fun getHostsFile(): String =
        RootUtils.runCommand("cat /etc/hosts 2>/dev/null")

    fun setHostsFile(content: String): Boolean {
        val escaped = content.replace("'", "\\'")
        return RootUtils.runCommand("echo '$escaped' > /etc/hosts").let { true }
    }

    fun addHostsEntry(domain: String, ip: String = "0.0.0.0"): Boolean =
        RootUtils.runCommand("echo '$ip $domain' >> /etc/hosts").let { true }

    fun removeHostsEntry(domain: String): Boolean =
        RootUtils.runCommand("sed -i '/$domain/d' /etc/hosts").let { true }

    fun getInstalledUserCerts(): List<String> =
        RootUtils.runCommand("ls /data/misc/user/0/cacerts-added/ 2>/dev/null")
            .lines().filter { it.isNotBlank() }

    fun removeUserCert(hash: String): Boolean =
        RootUtils.runCommand("rm /data/misc/user/0/cacerts-added/$hash").let { true }
}

// ── KernelKit Modül Kurulumu ─────────────────────────────────────────────────

object KernelKitInstaller {

    private const val MODULE_NAME = "KernelKit-FreqUnlock-v0.1.zip"
    private const val MODULE_ID   = "kernelkit_freq"

    fun isInstalled(): Boolean {
        val magisk = RootUtils.runCommand("[ -d /data/adb/modules/$MODULE_ID ] && echo 1 || echo 0")
        val ksu    = RootUtils.runCommand("[ -d /data/adb/ksu/modules/$MODULE_ID ] && echo 1 || echo 0")
        return magisk == "1" || ksu == "1"
    }

    fun isEnabled(): Boolean {
        val magiskDisabled = RootUtils.runCommand("[ -f /data/adb/modules/$MODULE_ID/disable ] && echo 1 || echo 0")
        val ksuDisabled    = RootUtils.runCommand("[ -f /data/adb/ksu/modules/$MODULE_ID/disable ] && echo 1 || echo 0")
        return magiskDisabled != "1" && ksuDisabled != "1" && isInstalled()
    }

    fun getRootType(): String {
        val magisk = RootUtils.runCommand("magisk -v 2>/dev/null")
        val ksu    = RootUtils.runCommand("ksud -V 2>/dev/null")
        return when {
            magisk.isNotEmpty() && !magisk.contains("not found") -> "Magisk"
            ksu.isNotEmpty()    && !ksu.contains("not found")    -> "KernelSU"
            else -> "Unknown"
        }
    }

    // APK assets'ten cihaza kopyala ve kur
    fun install(context: android.content.Context): Pair<Boolean, String> {
        return try {
            // 1. Assets'ten /data/local/tmp'ye kopyala
            val tmpPath = "/data/local/tmp/$MODULE_NAME"
            val bytes = context.assets.open(MODULE_NAME).readBytes()
            val tmpFile = java.io.File(context.cacheDir, MODULE_NAME)
            tmpFile.writeBytes(bytes)

            // 2. Root ile tmp'ye taşı
            RootUtils.runCommand("cp '${tmpFile.absolutePath}' '$tmpPath'")
            RootUtils.runCommand("chmod 644 '$tmpPath'")

            // 3. Root tipine göre kur
            val rootType = getRootType()
            val result = when (rootType) {
                "Magisk" -> {
                    val out = RootUtils.runCommand("magisk --install-module '$tmpPath' 2>&1")
                    if (out.contains("Done") || out.contains("Success") || out.isEmpty()) {
                        // Magisk modül dizinine manuel kopyala (fallback)
                        RootUtils.runCommand("mkdir -p /data/adb/modules/$MODULE_ID")
                        RootUtils.runCommand("unzip -o '$tmpPath' -d /data/adb/modules/$MODULE_ID 2>&1")
                        true to "Magisk modülü kuruldu. Yeniden başlatın."
                    } else {
                        false to "Magisk kurulum hatası: $out"
                    }
                }
                "KernelSU" -> {
                    RootUtils.runCommand("mkdir -p /data/adb/ksu/modules/$MODULE_ID")
                    val out = RootUtils.runCommand("unzip -o '$tmpPath' -d /data/adb/ksu/modules/$MODULE_ID 2>&1")
                    if (!out.contains("error")) {
                        true to "KernelSU modülü kuruldu. Yeniden başlatın."
                    } else {
                        false to "KernelSU kurulum hatası: $out"
                    }
                }
                else -> false to "Root tipi tespit edilemedi"
            }

            // 4. Temizlik
            RootUtils.runCommand("rm -f '$tmpPath'")
            tmpFile.delete()
            result
        } catch (e: Exception) {
            false to "Hata: ${e.message}"
        }
    }

    fun uninstall(): Boolean {
        RootUtils.runCommand("rm -rf /data/adb/modules/$MODULE_ID")
        RootUtils.runCommand("rm -rf /data/adb/ksu/modules/$MODULE_ID")
        return true
    }
}
