package com.Blackbox.muslim

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.Blackbox.muslim.ui.AdhanAlarmActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PrayerAlarm"
        const val CHANNEL_ID = "adhan_alarm_channel"
        const val NOTIFICATION_ID = 9999
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val prayerNameArabic = intent.getStringExtra("prayer_name_arabic") ?: return

        val settingsRepository = com.Blackbox.muslim.shared.SettingsRepository(context)
        if (settingsRepository.isMuteActive()) {
            return
        }

        Log.d(TAG, "ALARM FIRED: $prayerName ($prayerNameArabic)")

        val activityIntent = Intent(context, AdhanAlarmActivity::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("prayer_name_arabic", prayerNameArabic)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        context.startActivity(activityIntent)

        if (!settingsRepository.isMuteActive()) {
            showFullScreenNotification(context, prayerName, prayerNameArabic)
        }

        try {
            PrayerScheduler.scheduleNextPrayer(context)
        } catch (e: Exception) {
            Log.e(TAG, "Schedule next error", e)
        }
    }

    private fun showFullScreenNotification(context: Context, prayerName: String, prayerNameArabic: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "أذان الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه أذان الصلاة"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AdhanAlarmActivity::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("prayer_name_arabic", prayerNameArabic)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val fullScreenPI = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("أذان $prayerNameArabic")
            .setContentText("حان وقت صلاة $prayerNameArabic")
            .setStyle(
                Notification.BigTextStyle()
                    .bigText("حان وقت صلاة $prayerNameArabic\n\nاللهم صل وسلم على نبينا محمد")
            )
            .setContentIntent(fullScreenPI)
            .setFullScreenIntent(fullScreenPI, true)
            .setPriority(Notification.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "FullScreen notification posted")
    }
}
