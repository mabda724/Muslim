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
import com.Blackbox.muslim.data.DailyDhikrData
import java.util.*

class DailyDhikrReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "daily_dhikr_channel"
        const val NOTIFICATION_BASE_ID = 3000

        fun scheduleDailyDhikr(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val times = listOf(
                Pair(7, 0),   // morning athkar
                Pair(9, 30),  // salawat
                Pair(12, 0),  // istighfar
                Pair(15, 0),  // tip
                Pair(17, 30), // salawat
                Pair(19, 0),  // evening athkar
                Pair(21, 0),  // daily dhikr
                Pair(22, 30)  // sleep reminder
            )

            times.forEachIndexed { index, (hour, minute) ->
                val intent = Intent(context, DailyDhikrReceiver::class.java).apply {
                    putExtra("notification_id", NOTIFICATION_BASE_ID + index)
                    putExtra("type_index", index % 4)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", NOTIFICATION_BASE_ID)
        val typeIndex = intent.getIntExtra("type_index", 0)

        createNotificationChannel(context)

        val preferences = AppPreferences(context)
        if (!preferences.isNotificationsEnabled()) return

        val (title, body, bigText) = getDhikrContent(typeIndex)

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    private fun getDhikrContent(typeIndex: Int): Triple<String, String, String> {
        return when (typeIndex) {
            0 -> {
                val item = DailyDhikrData.salawatOnProphet.random()
                Triple(
                    "صلاة على النبي ﷺ",
                    item.arabic,
                    "${item.arabic}\n\n${item.translation}\n\nالأجر: ${item.reward}"
                )
            }
            1 -> {
                val item = DailyDhikrData.istighfar.random()
                Triple(
                    "وقت الاستغفار",
                    item.arabic,
                    "${item.arabic}\n\n${item.translation}\n\nالأجر: ${item.reward}"
                )
            }
            2 -> {
                val item = DailyDhikrData.dailyAthkar.random()
                Triple(
                    "أذكار",
                    item.arabic,
                    "${item.arabic}\n\n${item.transliteration}\n\n${item.translation}\n\n次数: ${item.count}"
                )
            }
            3 -> {
                val tip = DailyDhikrData.dailyTips.random()
                Triple("نصيحة يومية", tip, tip)
            }
            else -> {
                Triple(" muslim", "أذكار", "اللهم صل وسلم على نبينا محمد")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "أذكار يومية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات الأذكار والذكر اليومي"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
