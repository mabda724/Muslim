package com.Blackbox.muslim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PrayerReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val prayerNameArabic = intent.getStringExtra("prayer_name_arabic") ?: return
        val reminderMinutes = intent.getIntExtra("reminder_minutes", 10)

        val settingsRepository = com.Blackbox.muslim.shared.SettingsRepository(context)
        if (settingsRepository.isMuteActive()) {
            return
        }

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("صلاة $prayerNameArabic")
            .setContentText("متبقي $reminderMinutes دقيقة على موعد الصلاة")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكير بالصلاة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير قبل الصلاة"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
