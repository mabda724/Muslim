package com.Blackbox.muslim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.Blackbox.muslim.shared.PrayerManager
import com.Blackbox.muslim.shared.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "prayer_times_channel"
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_BLOCKING = 1002
    }

    private val prayerManager = PrayerManager()

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Prayer Times"
            val descriptionText = "Notifications for prayer times and blocking"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPrayerReminder(prayerName: String, prayerNameArabic: String) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🕌 حان وقت الصلاة - $prayerName")
            .setContentText("$prayerNameArabic - يرجى التوجه للصلاة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    fun showBlockingNotification(prayerName: String, remainingMinutes: Int) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🔒 الحظر نشط - $prayerName")
            .setContentText("الوقت المتبقي: $remainingMinutes دقيقة")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BLOCKING, notification)
    }

    fun cancelBlockingNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_BLOCKING)
    }

    fun isBlockingNotificationMuted(): Boolean {
        val settingsRepository = SettingsRepository(context)
        return settingsRepository.isMuteActive()
    }
}
