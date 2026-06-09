package com.Blackbox.muslim

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.shared.PrayerManager
import java.util.Calendar

object PrayerScheduler {

    private const val TAG = "PrayerScheduler"

    fun scheduleNextPrayer(context: Context) {
        try {
            val preferences = AppPreferences(context)
            val prayerManager = PrayerManager()

            val lat = preferences.getLatitude()
            val lng = preferences.getLongitude()
            val method = preferences.getCalculationMethod()

            Log.d(TAG, "Scheduling: lat=$lat, lng=$lng, method=$method")

            val nextPrayer = prayerManager.getNextPrayerTime(lat, lng, method)
            if (nextPrayer == null) {
                Log.e(TAG, "Next prayer is null!")
                return
            }

            Log.d(TAG, "Next prayer: ${nextPrayer.name} at ${nextPrayer.time}")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val adhanTime = nextPrayer.time.time
            val reminderTime = adhanTime - (10 * 60 * 1000L)
            val now = System.currentTimeMillis()

            Log.d(TAG, "Now=$now, adhanTime=$adhanTime, reminderTime=$reminderTime")

            if (reminderTime > now) {
                val reminderIntent = Intent(context, PrayerReminderReceiver::class.java).apply {
                    putExtra("prayer_name", nextPrayer.name)
                    putExtra("prayer_name_arabic", nextPrayer.nameArabic)
                }
                val reminderPI = PendingIntent.getBroadcast(
                    context, 0, reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val reminderClockInfo = AlarmManager.AlarmClockInfo(reminderTime, reminderPI)
                alarmManager.setAlarmClock(reminderClockInfo, reminderPI)
                Log.d(TAG, "Reminder alarm set for: $reminderTime")
            }

            if (adhanTime > now) {
                val adhanIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra("prayer_name", nextPrayer.name)
                    putExtra("prayer_name_arabic", nextPrayer.nameArabic)
                }
                val adhanPI = PendingIntent.getBroadcast(
                    context, 1, adhanIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val adhanClockInfo = AlarmManager.AlarmClockInfo(adhanTime, adhanPI)
                alarmManager.setAlarmClock(adhanClockInfo, adhanPI)
                Log.d(TAG, "Adhan alarm set for: $adhanTime")
            }
        } catch (e: Exception) {
            Log.e("PrayerScheduler", "Error scheduling prayer", e)
        }
    }
}
