package com.example.ytmonitor

import android.app.*
import android.content.Intent
import android.net.HttpResponseCache
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class CheckService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    companion object {
        const val CHANNEL_ID = "yt_monitor"
        private const val NOTIF_ID = 1
        private const val INTERVAL_MS = 5 * 60 * 1000L // 5 минут
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(NOTIF_ID, buildNotification("Мониторинг запущен",
            "Проверка youtube.com каждые 5 минут"))
        if (job == null) startLoop()
        return START_STICKY
    }

    private fun startLoop() {
        job = scope.launch {
            while (isActive) {
                checkSite()
                delay(INTERVAL_MS)
            }
        }
    }

    private suspend fun checkSite() {
        val ok = try {
            val conn = URL("https://www.youtube.com").openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.responseCode in 200..399
        } catch (e: Exception) {
            false
        }
        if (ok) {
            showResultNotification()
        }
    }

    private fun showResultNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(2, NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("YouTube доступен")
            .setContentText("Соединение успешно установлено")
            .setAutoCancel(true)
            .build())
    }

    private fun buildNotification(title: String, text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .build()

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Мониторинг YouTube",
            NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
