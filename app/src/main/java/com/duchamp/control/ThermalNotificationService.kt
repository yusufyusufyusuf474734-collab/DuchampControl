package com.duchamp.control

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class ThermalNotificationService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastAlertTime = 0L

    companion object {
        const val CHANNEL_ID = "thermal_alert_channel"
        const val NOTIF_ID   = 1002

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ThermalNotificationService::class.java))
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, ThermalNotificationService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotif("Termal İzleme Aktif", "Sıcaklık eşiği izleniyor..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                val prefs = AppPrefs
                if (prefs.thermalAlertEnabled) {
                    val tempRaw = RootUtils.readSysfs("/sys/class/thermal/thermal_zone0/temp")
                        .toIntOrNull() ?: 0
                    val tempC = if (tempRaw > 1000) tempRaw / 1000 else tempRaw
                    val threshold = prefs.thermalAlertTempC

                    if (tempC >= threshold) {
                        val now = System.currentTimeMillis()
                        // En az 60 saniyede bir bildirim
                        if (now - lastAlertTime > 60_000) {
                            lastAlertTime = now
                            sendAlert(tempC, threshold)
                        }
                    }
                }
                delay(5000)
            }
        }
    }

    private fun sendAlert(tempC: Int, threshold: Int) {
        val nm = getSystemService(NotificationManager::class.java)
        val notif = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Yüksek CPU Sıcaklığı")
            .setContentText("CPU sıcaklığı ${tempC}°C — eşik: ${threshold}°C")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID + 1, notif)
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Termal Uyarılar", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "CPU/GPU sıcaklık uyarıları"
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotif(title: String, text: String): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
