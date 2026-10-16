package com.example.ytmonitor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        // Разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            ContextCompat.startForegroundService(this, Intent(this, CheckService::class.java))
            status.text = "Мониторинг запущен"
        }

        findViewById<Button>(R.id.stopBtn).setOnClickListener {
            stopService(Intent(this, CheckService::class.java))
            status.text = "Мониторинг остановлен"
        }
    }
}
