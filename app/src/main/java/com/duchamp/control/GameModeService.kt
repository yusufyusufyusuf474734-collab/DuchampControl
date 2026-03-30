package com.duchamp.control

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.*

class GameModeService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastForegroundApp = ""
    private var gameModeActive = false

    companion object {
        const val CHANNEL_ID = "game_mode_channel"
        const val NOTIF_ID = 1001
        const val ACTION_START = "START_GAME_MODE"
        const val ACTION_STOP  = "STOP_GAME_MODE"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, GameModeService::class.java).apply {
                action = ACTION_START
            })
        }
        fun stop(context: Context) {
            context.startService(Intent(context, GameModeService::class.java).apply {
                action = ACTION_STOP
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
        }
        startForeground(NOTIF_ID, buildNotification("Oyun Modu İzleniyor", "Uygulama değişimi bekleniyor..."))
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        scope.launch {
            val prefs = AppPrefs
            while (isActive) {
                val foreground = getForegroundApp()
                if (foreground != lastForegroundApp) {
                    lastForegroundApp = foreground
                    val gameApps = prefs.loadGameApps()
                    val isGame = gameApps.contains(foreground)

                    if (isGame && !gameModeActive) {
                        // Oyun başladı
                        gameModeActive = true
                        PerformanceProfiles.presets.find { it.id == "gaming" }?.let {
                            PerformanceProfiles.apply(it)
                        }
                        acquireWakeLock()
                        updateNotification("🎮 Oyun Modu Aktif", foreground)
                    } else if (!isGame && gameModeActive) {
                        // Oyun bitti
                        gameModeActive = false
                        PerformanceProfiles.presets.find { it.id == "balanced" }?.let {
                            PerformanceProfiles.apply(it)
                        }
                        releaseWakeLock()
                        updateNotification("Oyun Modu İzleniyor", "Uygulama değişimi bekleniyor...")
                    }
                }
                delay(2000)
            }
        }
    }

    private fun getForegroundApp(): String {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 10000, now)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""
        } catch (e: Exception) {
            // Fallback: root ile al
            RootUtils.runCommand("dumpsys activity activities | grep mResumedActivity | head -1 | awk '{print $4}' | cut -d'/' -f1")
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "DimensityTool:GameMode")
        wakeLock?.acquire(4 * 60 * 60 * 1000L) // max 4 saat
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Oyun Modu", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Oyun modu servisi bildirimleri"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, text: String): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

    private fun updateNotification(title: String, text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(title, text))
    }

    override fun onDestroy() {
        scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
