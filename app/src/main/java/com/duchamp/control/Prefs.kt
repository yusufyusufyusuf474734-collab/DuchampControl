package com.duchamp.control

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ── Tema ─────────────────────────────────────────────────────────────────────

enum class AppTheme { DARK, LIGHT, SYSTEM }

data class AccentColor(val name: String, val color: Color)

val accentColors = listOf(
    AccentColor("Mor",      Color(0xFF7C4DFF)),
    AccentColor("Mavi",     Color(0xFF2196F3)),
    AccentColor("Yeşil",    Color(0xFF4CAF50)),
    AccentColor("Turuncu",  Color(0xFFFF9800)),
    AccentColor("Kırmızı",  Color(0xFFF44336)),
    AccentColor("Pembe",    Color(0xFFE91E63)),
    AccentColor("Camgöbeği",Color(0xFF00BCD4)),
    AccentColor("Sarı",     Color(0xFFFFEB3B))
)

// ── Zamanlayıcı ──────────────────────────────────────────────────────────────

enum class ScheduleTrigger { TIME, CHARGING, TEMPERATURE }

data class ScheduleRule(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val trigger: ScheduleTrigger,
    // TIME trigger
    val hour: Int = 0,
    val minute: Int = 0,
    // CHARGING trigger
    val onCharging: Boolean = true,
    // TEMPERATURE trigger
    val tempThresholdC: Int = 70,
    // Aksiyon
    val profileId: String = "balanced",
    val customActions: List<String> = emptyList() // "cpu:schedutil", "polling:120" vb.
)

// ── MTK EAS/HMP ──────────────────────────────────────────────────────────────

data class MtkEasInfo(
    val schedBoost: String,
    val inputBoostFreq: String,
    val inputBoostDuration: String,
    val schedDownMigrateLoad: String,
    val schedUpMigrateLoad: String,
    val schedHmpBoost: String,
    val cpuInputBoostEnabled: Boolean
)

// ── Firewall ──────────────────────────────────────────────────────────────────

data class FirewallRule(
    val id: String,
    val packageName: String,
    val appLabel: String,
    val blockWifi: Boolean,
    val blockData: Boolean
)

// ── VPN ───────────────────────────────────────────────────────────────────────

data class VpnInfo(
    val active: Boolean,
    val interfaceName: String,
    val serverIp: String,
    val localIp: String,
    val protocol: String
)

// ── Güvenlik ─────────────────────────────────────────────────────────────────

data class RootAccessLog(
    val timestamp: Long,
    val packageName: String,
    val appLabel: String,
    val granted: Boolean
)

data class SecurityInfo(
    val bootloaderLocked: Boolean,
    val verifiedBootState: String,
    val dmVerity: String,
    val selinuxMode: String,
    val adbEnabled: Boolean,
    val developerOptions: Boolean,
    val installedCerts: List<String>,
    val rootAccessLogs: List<RootAccessLog>
)

// ── SharedPreferences yöneticisi ─────────────────────────────────────────────

object AppPrefs {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("duchamp_prefs", Context.MODE_PRIVATE)
    }

    var theme: AppTheme
        get() = AppTheme.valueOf(prefs.getString("theme", AppTheme.DARK.name) ?: AppTheme.DARK.name)
        set(v) { prefs.edit().putString("theme", v.name).apply() }

    var accentColorIndex: Int
        get() = prefs.getInt("accent_index", 0)
        set(v) { prefs.edit().putInt("accent_index", v).apply() }

    var dashboardCompact: Boolean
        get() = prefs.getBoolean("dashboard_compact", false)
        set(v) { prefs.edit().putBoolean("dashboard_compact", v).apply() }

    var sleepModeEnabled: Boolean
        get() = prefs.getBoolean("sleep_mode_enabled", false)
        set(v) { prefs.edit().putBoolean("sleep_mode_enabled", v).apply() }

    var sleepProfileId: String
        get() = prefs.getString("sleep_profile_id", "powersave") ?: "powersave"
        set(v) { prefs.edit().putString("sleep_profile_id", v).apply() }

    var wakeProfileId: String
        get() = prefs.getString("wake_profile_id", "balanced") ?: "balanced"
        set(v) { prefs.edit().putString("wake_profile_id", v).apply() }

    fun saveAppProfiles(map: Map<String, String>) {
        val json = map.entries.joinToString(",", "{", "}") { (k, v) -> "\"$k\":\"$v\"" }
        prefs.edit().putString("app_profiles", json).apply()
    }

    fun loadAppProfiles(): Map<String, String> {
        val json = prefs.getString("app_profiles", "{}") ?: "{}"
        if (json == "{}") return emptyMap()
        return try {
            json.removeSurrounding("{", "}")
                .split(",")
                .filter { it.contains(":") }
                .associate { entry ->
                    val (k, v) = entry.split(":", limit = 2)
                    k.trim().removeSurrounding("\"") to v.trim().removeSurrounding("\"")
                }
        } catch (e: Exception) { emptyMap() }
    }

    fun getBackupJson(
        scheduleRules: List<ScheduleRule>,
        appProfiles: Map<String, String>,
        theme: AppTheme,
        accentIndex: Int,
        sleepEnabled: Boolean,
        sleepProfile: String,
        wakeProfile: String
    ): String {
        val rulesJson = buildString {
            append("[")
            scheduleRules.forEachIndexed { i, r ->
                if (i > 0) append(",")
                append("""{"id":"${r.id}","name":"${r.name}","enabled":${r.enabled},""")
                append(""""trigger":"${r.trigger.name}","hour":${r.hour},"minute":${r.minute},""")
                append(""""onCharging":${r.onCharging},"tempThreshold":${r.tempThresholdC},""")
                append(""""profileId":"${r.profileId}"}""")
            }
            append("]")
        }
        val appProfilesJson = appProfiles.entries.joinToString(",", "{", "}") { (k, v) -> "\"$k\":\"$v\"" }
        return """{"version":1,"theme":"${theme.name}","accentIndex":$accentIndex,"scheduleRules":$rulesJson,"appProfiles":$appProfilesJson,"sleepModeEnabled":$sleepEnabled,"sleepProfileId":"$sleepProfile","wakeProfileId":"$wakeProfile"}"""
    }

    var scheduleRulesJson: String
        get() = prefs.getString("schedule_rules", "[]") ?: "[]"
        set(v) { prefs.edit().putString("schedule_rules", v).apply() }

    fun saveScheduleRules(rules: List<ScheduleRule>) {
        val json = buildString {
            append("[")
            rules.forEachIndexed { i, r ->
                if (i > 0) append(",")
                append("""{"id":"${r.id}","name":"${r.name}","enabled":${r.enabled},""")
                append(""""trigger":"${r.trigger.name}","hour":${r.hour},"minute":${r.minute},""")
                append(""""onCharging":${r.onCharging},"tempThreshold":${r.tempThresholdC},""")
                append(""""profileId":"${r.profileId}"}""")
            }
            append("]")
        }
        scheduleRulesJson = json
    }

    fun loadScheduleRules(): List<ScheduleRule> {
        // Basit JSON parse (harici kütüphane olmadan)
        val json = scheduleRulesJson
        if (json == "[]" || json.isBlank()) return emptyList()
        return try {
            json.removeSurrounding("[", "]")
                .split("},")
                .filter { it.isNotBlank() }
                .map { obj ->
                    val clean = obj.trimEnd('}').trimStart('{')
                    fun field(key: String): String =
                        Regex(""""$key"\s*:\s*"?([^",}]+)"?""").find(clean)?.groupValues?.get(1) ?: ""
                    ScheduleRule(
                        id = field("id"),
                        name = field("name"),
                        enabled = field("enabled") == "true",
                        trigger = ScheduleTrigger.valueOf(field("trigger").ifEmpty { "TIME" }),
                        hour = field("hour").toIntOrNull() ?: 0,
                        minute = field("minute").toIntOrNull() ?: 0,
                        onCharging = field("onCharging") == "true",
                        tempThresholdC = field("tempThreshold").toIntOrNull() ?: 70,
                        profileId = field("profileId")
                    )
                }
        } catch (e: Exception) { emptyList() }
    }
}
