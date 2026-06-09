package com.Blackbox.muslim

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.Blackbox.muslim.data.AladhanPrayerApi
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.PrayerTimeCache
import com.Blackbox.muslim.shared.PrayerManager
import com.Blackbox.muslim.shared.SettingsRepository
import com.Blackbox.muslim.ui.AdhanAlarmActivity
import java.text.SimpleDateFormat
import java.util.*

class PrayerTimeService : Service() {

    companion object {
        private const val TAG = "PrayerTimeService"
        private const val CHANNEL_ID = "prayer_service_channel"
        private const val REMINDER_CHANNEL_ID = "prayer_reminder_channel"
        private const val IQAMAH_CHANNEL_ID = "prayer_iqamah_channel"
        private const val NOTIFICATION_ID = 7777
        private const val CHECK_INTERVAL = 30_000L
        private const val FETCH_INTERVAL = 3600_000L

        private const val REMINDER_BASE_CODE = 10000
        private const val ADHAN_BASE_CODE = 20000
        private const val IQAMAH_BASE_CODE = 30000

        private val PRAYER_NAMES = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
        private val PRAYER_NAMES_ARABIC = mapOf(
            "Fajr" to "الفجر",
            "Sunrise" to "الشروق",
            "Dhuhr" to "الظهر",
            "Asr" to "العصر",
            "Maghrib" to "المغرب",
            "Isha" to "العشاء"
        )
        private val PRAYER_NAMES_LOWERCASE = mapOf(
            "Fajr" to "fajr",
            "Sunrise" to "sunrise",
            "Dhuhr" to "dhuhr",
            "Asr" to "asr",
            "Maghrib" to "maghrib",
            "Isha" to "isha"
        )
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastTriggeredPrayer = ""
    private var lastTriggeredDate = ""
    private var lastFetchTime = 0L

    private val checker = object : Runnable {
        override fun run() {
            checkPrayerTime()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    private val fetcher = object : Runnable {
        override fun run() {
            fetchAndSchedulePrayers()
            handler.postDelayed(this, FETCH_INTERVAL)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service started")
        createChannels()
        startForeground(NOTIFICATION_ID, buildPersistentNotification())
        handler.post(checker)
        handler.post(fetcher)
        fetchAndSchedulePrayers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun isMuted(): Boolean {
        val settingsRepository = SettingsRepository(this)
        return settingsRepository.isMuteActive()
    }

    private fun fetchAndSchedulePrayers() {
        val preferences = AppPreferences(this)
        val cache = PrayerTimeCache(this)
        val city = preferences.getCity()
        val country = preferences.getCountry()
        val method = preferences.getCalculationMethod()

        if (cache.isCachedToday()) {
            Log.d(TAG, "Using cached prayer times for today")
            val cachedTimes = cache.getTodayTimes()
            if (cachedTimes != null) {
                scheduleAllAlarms(cachedTimes, preferences)
                return
            }
        }

        AladhanPrayerApi().fetchPrayerTimes(city, country, method, object : AladhanPrayerApi.PrayerTimesCallback {
            override fun onSuccess(times: Map<String, String>) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())
                cache.save(times, today)
                lastFetchTime = System.currentTimeMillis()
                handler.post { scheduleAllAlarms(times, preferences) }
            }

            override fun onError(error: String) {
                Log.e(TAG, "API fetch failed, using local calculation: $error")
                val localTimes = getLocalPrayerTimes(preferences)
                if (localTimes != null) {
                    handler.post { scheduleAllAlarms(localTimes, preferences) }
                }
            }
        })
    }

    private fun getLocalPrayerTimes(preferences: AppPreferences): Map<String, String>? {
        return try {
            val prayerManager = PrayerManager()
            val lat = preferences.getLatitude()
            val lng = preferences.getLongitude()
            val method = preferences.getCalculationMethod()
            val sdf = SimpleDateFormat("HH:mm", Locale.US)

            val prayerTimes = prayerManager.getPrayerTimes(lat, lng, method)
            mapOf(
                "Fajr" to sdf.format(prayerTimes.fajr),
                "Sunrise" to sdf.format(prayerTimes.sunrise),
                "Dhuhr" to sdf.format(prayerTimes.dhuhr),
                "Asr" to sdf.format(prayerTimes.asr),
                "Maghrib" to sdf.format(prayerTimes.maghrib),
                "Isha" to sdf.format(prayerTimes.isha)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Local calculation failed", e)
            null
        }
    }

    private fun scheduleAllAlarms(times: Map<String, String>, preferences: AppPreferences) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderMinutes = preferences.getReminderMinutesBefore()
        val iqamahMinutes = preferences.getIqamahDelayMinutes()
        val now = System.currentTimeMillis()
        val today = Calendar.getInstance()

        for ((index, prayerName) in PRAYER_NAMES.withIndex()) {
            val timeStr = times[prayerName] ?: continue
            val prayerCal = Calendar.getInstance().apply {
                val parts = timeStr.split(":")
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val prayerTimeMillis = prayerCal.timeInMillis
            val reminderTimeMillis = prayerTimeMillis - (reminderMinutes * 60 * 1000L)
            val iqamahTimeMillis = prayerTimeMillis + (iqamahMinutes * 60 * 1000L)

            val prayerNameLower = PRAYER_NAMES_LOWERCASE[prayerName] ?: prayerName.lowercase()
            val prayerNameArabic = PRAYER_NAMES_ARABIC[prayerName] ?: prayerName

            // Reminder alarm (X minutes before)
            if (reminderTimeMillis > now) {
                val reminderIntent = Intent(this, PrayerReminderReceiver::class.java).apply {
                    putExtra("prayer_name", prayerNameLower)
                    putExtra("prayer_name_arabic", prayerNameArabic)
                    putExtra("reminder_minutes", reminderMinutes)
                }
                val reminderPI = PendingIntent.getBroadcast(
                    this,
                    REMINDER_BASE_CODE + index,
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val reminderClockInfo = AlarmManager.AlarmClockInfo(reminderTimeMillis, reminderPI)
                alarmManager.setAlarmClock(reminderClockInfo, reminderPI)
                Log.d(TAG, "Reminder alarm set for $prayerName at $reminderTimeMillis (min $reminderMinutes before)")
            }

            // Adhan alarm (exact prayer time)
            if (prayerTimeMillis > now) {
                val adhanIntent = Intent(this, PrayerAlarmReceiver::class.java).apply {
                    putExtra("prayer_name", prayerNameLower)
                    putExtra("prayer_name_arabic", prayerNameArabic)
                }
                val adhanPI = PendingIntent.getBroadcast(
                    this,
                    ADHAN_BASE_CODE + index,
                    adhanIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val adhanClockInfo = AlarmManager.AlarmClockInfo(prayerTimeMillis, adhanPI)
                alarmManager.setAlarmClock(adhanClockInfo, adhanPI)
                Log.d(TAG, "Adhan alarm set for $prayerName at $prayerTimeMillis")
            }

            // Iqamah alarm (X minutes after prayer)
            if (iqamahTimeMillis > now) {
                val iqamahIntent = Intent(this, IqamahReceiver::class.java).apply {
                    putExtra("prayer_name", prayerNameLower)
                    putExtra("prayer_name_arabic", prayerNameArabic)
                }
                val iqamahPI = PendingIntent.getBroadcast(
                    this,
                    IQAMAH_BASE_CODE + index,
                    iqamahIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val iqamahClockInfo = AlarmManager.AlarmClockInfo(iqamahTimeMillis, iqamahPI)
                alarmManager.setAlarmClock(iqamahClockInfo, iqamahPI)
                Log.d(TAG, "Iqamah alarm set for $prayerName at $iqamahTimeMillis (min $iqamahMinutes after)")
            }
        }
    }

    private fun checkPrayerTime() {
        try {
            val preferences = AppPreferences(this)
            val prayerManager = PrayerManager()

            val lat = preferences.getLatitude()
            val lng = preferences.getLongitude()
            val method = preferences.getCalculationMethod()

            val now = Calendar.getInstance()
            val today = "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DAY_OF_MONTH)}"

            if (lastTriggeredDate != today) {
                lastTriggeredPrayer = ""
                lastTriggeredDate = today
                if (!PrayerTimeCache(this).isCachedToday()) {
                    fetchAndSchedulePrayers()
                }
            }

            if (isMuted()) return

            val prayerTimes = prayerManager.getPrayerTimesList(lat, lng, method)

            for (prayer in prayerTimes) {
                val prayerCal = Calendar.getInstance().apply { time = prayer.time }
                val diff = now.timeInMillis - prayerCal.timeInMillis

                if (diff in 0 until 60_000 && prayer.name != lastTriggeredPrayer) {
                    lastTriggeredPrayer = prayer.name
                    Log.d(TAG, "PRAYER TIME (backup check): ${prayer.name}")
                    showFullScreenAlarm(prayer.name, prayer.nameArabic)
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Check error", e)
        }
    }

    private fun showFullScreenAlarm(prayerName: String, prayerNameArabic: String) {
        val settingsRepository = SettingsRepository(this)
        if (settingsRepository.isMuteActive()) {
            Log.d(TAG, "Mute active, skipping full screen alarm")
            return
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val fullScreenIntent = Intent(this, AdhanAlarmActivity::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("prayer_name_arabic", prayerNameArabic)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val fullScreenPI = PendingIntent.getActivity(
            this, NOTIFICATION_ID, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
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
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setOngoing(true)
            .setAutoCancel(false)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Full screen alarm notification posted")
    }

    private fun buildPersistentNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPI = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("muslim")
            .setContentText("مراقبة أوقات الصلاة نشطة")
            .setContentIntent(openPI)
            .setOngoing(true)
            .build()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "مراقبة الصلاة", NotificationManager.IMPORTANCE_LOW).apply {
                description = "يخبرك عند حصول وقت الصلاة"
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(CHANNEL_ID, "أذان الصلاة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تنبيه أذان الصلاة"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            val reminderChannel = NotificationChannel(REMINDER_CHANNEL_ID, "تذكير بالصلاة", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تذكير قبل الصلاة"
            }

            val iqamahChannel = NotificationChannel(IQAMAH_CHANNEL_ID, "إقامة الصلاة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تنبيه إقامة الصلاة"
                enableVibration(true)
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(serviceChannel)
            nm.createNotificationChannel(alarmChannel)
            nm.createNotificationChannel(reminderChannel)
            nm.createNotificationChannel(iqamahChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checker)
        handler.removeCallbacks(fetcher)
        Log.d(TAG, "Service destroyed")
    }
}
