package com.Blackbox.muslim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class IqamahReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_iqamah_channel"
        const val NOTIFICATION_ID = 6001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val prayerNameArabic = intent.getStringExtra("prayer_name_arabic") ?: return

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("أقيمت الصلاة")
            .setContentText("أقيمت صلاة $prayerNameArabic")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID + getPrayerIndex(prayerName), notification)
    }

    private fun getPrayerIndex(name: String): Int {
        return when (name.lowercase()) {
            "fajr" -> 0
            "sunrise" -> 1
            "dhuhr" -> 2
            "asr" -> 3
            "maghrib" -> 4
            "isha" -> 5
            else -> 0
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "إقامة الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه إقامة الصلاة بعد الأذان"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
