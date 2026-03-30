package com.duchamp.control

object BootScriptManager {

    private const val MAGISK_SCRIPT = "/data/adb/post-fs-data.d/dimensitytool.sh"
    private const val INITD_SCRIPT  = "/system/etc/init.d/99dimensitytool"

    fun getScriptContent(): String {
        val magisk = RootUtils.runCommand("cat $MAGISK_SCRIPT 2>/dev/null")
        if (magisk.isNotBlank()) return magisk
        val initd = RootUtils.runCommand("cat $INITD_SCRIPT 2>/dev/null")
        return initd
    }

    fun getScriptPath(): String {
        val hasMagisk = RootUtils.runCommand("[ -d /data/adb/post-fs-data.d ] && echo 1 || echo 0") == "1"
        return if (hasMagisk) MAGISK_SCRIPT else INITD_SCRIPT
    }

    fun saveScript(content: String): Boolean {
        val path = getScriptPath()
        val dir = path.substringBeforeLast("/")
        RootUtils.runCommand("mkdir -p $dir")
        val escaped = content.replace("'", "'\\''")
        val result = RootUtils.runCommand("printf '%s' '$escaped' > $path")
        RootUtils.runCommand("chmod 755 $path")
        return true
    }

    fun deleteScript(): Boolean {
        RootUtils.runCommand("rm -f $MAGISK_SCRIPT 2>/dev/null")
        RootUtils.runCommand("rm -f $INITD_SCRIPT 2>/dev/null")
        return true
    }

    fun isScriptExists(): Boolean {
        val m = RootUtils.runCommand("[ -f $MAGISK_SCRIPT ] && echo 1 || echo 0") == "1"
        val i = RootUtils.runCommand("[ -f $INITD_SCRIPT ] && echo 1 || echo 0") == "1"
        return m || i
    }

    // Mevcut profil ayarlarından otomatik script oluştur
    fun generateFromProfile(profile: PerfProfile): String = buildString {
        appendLine("#!/system/bin/sh")
        appendLine("# DimensityTool Önyükleme Scripti")
        appendLine("# Profil: ${profile.name}")
        appendLine("# Otomatik oluşturuldu — $(date)")
        appendLine()
        appendLine("# CPU Governor")
        for (p in listOf(0, 4, 7)) {
            appendLine("echo ${profile.cpuGovernor} > /sys/devices/system/cpu/cpufreq/policy$p/scaling_governor")
        }
        appendLine()
        appendLine("# GPU Governor")
        appendLine("echo ${profile.gpuGovernor} > /sys/devices/platform/soc/13000000.mali/devfreq/13000000.mali/governor 2>/dev/null")
        appendLine()
        appendLine("# TCP Congestion")
        appendLine("echo ${profile.tcpCongestion} > /proc/sys/net/ipv4/tcp_congestion_control")
        appendLine()
        appendLine("# Swappiness")
        appendLine("echo ${profile.swappiness} > /proc/sys/vm/swappiness")
        appendLine()
        appendLine("# Touch Polling Rate")
        appendLine("echo ${profile.touchPollingRate} > /sys/devices/platform/goodix_ts.0/switch_report_rate 2>/dev/null")
        appendLine()
        appendLine("exit 0")
    }

    // Özel komutlardan script oluştur
    fun generateCustom(commands: List<String>): String = buildString {
        appendLine("#!/system/bin/sh")
        appendLine("# DimensityTool Önyükleme Scripti")
        appendLine("# Özel komutlar")
        appendLine()
        commands.forEach { cmd ->
            if (cmd.isNotBlank()) appendLine(cmd)
        }
        appendLine()
        appendLine("exit 0")
    }
}
