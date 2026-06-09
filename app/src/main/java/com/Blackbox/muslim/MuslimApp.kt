package com.Blackbox.muslim

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.Blackbox.muslim.data.AladhanPrayerApi
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.PrayerTimeCache

class MuslimApp : Application() {

    companion object {
        private const val TAG = "MuslimApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "App starting...")

        // Fetch prayer times from API in background
        fetchPrayerTimesFromApi()

        // Start prayer time service
        try {
            val serviceIntent = Intent(this, PrayerTimeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Log.d(TAG, "PrayerTimeService started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start PrayerTimeService", e)
        }

        // Schedule alarms as backup
        try { PrayerScheduler.scheduleNextPrayer(this) } catch (e: Exception) { Log.e(TAG, "PrayerScheduler error", e) }
        try { DailyDhikrReceiver.scheduleDailyDhikr(this) } catch (e: Exception) { Log.e(TAG, "DailyDhikrReceiver error", e) }
    }

    private fun fetchPrayerTimesFromApi() {
        val preferences = AppPreferences(this)
        val cache = PrayerTimeCache(this)

        if (cache.isCachedToday()) {
            Log.d(TAG, "Prayer times already cached for today")
            return
        }

        val city = preferences.getCity()
        val country = preferences.getCountry()
        val method = preferences.getCalculationMethod()

        AladhanPrayerApi().fetchPrayerTimes(city, country, method, object : AladhanPrayerApi.PrayerTimesCallback {
            override fun onSuccess(times: Map<String, String>) {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(System.currentTimeMillis())
                cache.save(times, today)
                Log.d(TAG, "Prayer times fetched and cached: $times")
            }

            override fun onError(error: String) {
                Log.e(TAG, "Failed to fetch prayer times: $error")
            }
        })
    }

    fun showDuaNotification() {
        try {
            val channelId = "dua_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "دعاء للمطور", NotificationManager.IMPORTANCE_HIGH)
                getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }

            val openIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val duaText = "اللهم ارحم عبدك محمد ابراهيم عبدالله وزوجته ووالديه وإخوته\n\n" +
                "اللهم بارك لهم في حياتهم واحتسب خيرهم واغفر ذنوبهم\n\n" +
                "اللهم شفي عبدك محمد ابراهيم عبدالله كلاء شفاءك ولا تترك به عاهة\n\n" +
                "اللهم قضِ دينه وأغنه عن الناس وأرزقه من حيث لا يحتسب\n\n" +
                "اللهم وفقهم لكل خير واجمع شملهم على طاعتك\n\n" +
                "من فضلك ادّعُ لهم ولو مرة واحدة"

            val notification = android.app.Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("دعوة للمطور")
                .setContentText("ادعو للمطور محمد ابراهيم عبدالله")
                .setStyle(android.app.Notification.BigTextStyle().bigText(duaText))
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            getSystemService(NotificationManager::class.java).notify(5000, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Dua notification error", e)
        }
    }
}
