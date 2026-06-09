package com.Blackbox.muslim

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.shared.PrayerManager

class PrayerCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        private const val TAG = "PrayerCheckWorker"
    }

    override fun doWork(): Result {
        Log.d(TAG, "Running prayer check...")

        try {
            val preferences = AppPreferences(applicationContext)
            val prayerManager = PrayerManager()

            val lat = preferences.getLatitude()
            val lng = preferences.getLongitude()
            val method = preferences.getCalculationMethod()

            val now = System.currentTimeMillis()
            val prayers = prayerManager.getPrayerTimesList(lat, lng, method)

            for (prayer in prayers) {
                val prayerTime = prayer.time.time
                val diff = prayerTime - now

                if (diff in -60_000 until 30_000) {
                    Log.d(TAG, "Prayer time matched: ${prayer.name}")

                    val intent = android.content.Intent(applicationContext, PrayerAlarmReceiver::class.java).apply {
                        putExtra("prayer_name", prayer.name)
                        putExtra("prayer_name_arabic", prayer.nameArabic)
                        flags = android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                    }
                    applicationContext.sendBroadcast(intent)
                    break
                }
            }

            PrayerScheduler.scheduleNextPrayer(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error in prayer check", e)
        }

        return Result.success()
    }
}
