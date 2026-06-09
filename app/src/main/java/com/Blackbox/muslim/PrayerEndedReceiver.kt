package com.Blackbox.muslim

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.PrayerTracker
import com.Blackbox.muslim.shared.PrayerManager
import java.util.*

class PrayerEndedReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_ended_channel"
        const val BASE_REQUEST_CODE = 4000
        private const val END_OFFSET_MINUTES = 20

        fun schedulePrayerEndedAlarms(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val preferences = AppPreferences(context)
            val tracker = PrayerTracker(context)
            val prayerManager = PrayerManager()

            val lat = preferences.getLatitude()
            val lng = preferences.getLongitude()
            val method = preferences.getCalculationMethod()

            val lowerPrayers = mapOf(
                "fajr" to "Fajr",
                "dhuhr" to "Dhuhr",
                "asr" to "Asr",
                "maghrib" to "Maghrib",
                "isha" to "Isha"
            )

            lowerPrayers.forEach { (lower, proper) ->
                if (tracker.isPrayedToday(lower)) return@forEach

                val intent = Intent(context, PrayerEndedReceiver::class.java).apply {
                    putExtra("prayer_name", lower)
                    putExtra("notification_id", BASE_REQUEST_CODE + lowerPrayers.keys.indexOf(lower))
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    BASE_REQUEST_CODE + lowerPrayers.keys.indexOf(lower),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    val prayerTimes = prayerManager.getPrayerTimes(lat, lng, method)
                    val prayerDate = when (proper) {
                        "Fajr" -> prayerTimes.fajr
                        "Dhuhr" -> prayerTimes.dhuhr
                        "Asr" -> prayerTimes.asr
                        "Maghrib" -> prayerTimes.maghrib
                        "Isha" -> prayerTimes.isha
                        else -> return@forEach
                    }

                    val endAlarmTime = prayerDate.time + (END_OFFSET_MINUTES * 60 * 1000L)
                    val now = System.currentTimeMillis()

                    if (endAlarmTime > now) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                endAlarmTime,
                                pendingIntent
                            )
                        } catch (e: SecurityException) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, endAlarmTime, pendingIntent)
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        fun getPrayerDisplayName(name: String): String {
            return when (name) {
                "fajr" -> "الفجر"
                "dhuhr" -> "الظهر"
                "asr" -> "العصر"
                "maghrib" -> "المغرب"
                "isha" -> "العشاء"
                else -> name
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val notificationId = intent.getIntExtra("notification_id", BASE_REQUEST_CODE)

        createNotificationChannel(context)

        val preferences = AppPreferences(context)
        val tracker = PrayerTracker(context)

        if (tracker.isPrayedToday(prayerName)) return

        if (preferences.isNotificationsEnabled()) {
            val displayName = getPrayerDisplayName(prayerName)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("هل صليت $displayName؟")
                .setContentText("اضغط هنا لتسجيل الصلاة")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(notificationId, notification)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكير بالصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكير بعد انتهاء وقت الصلاة"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
