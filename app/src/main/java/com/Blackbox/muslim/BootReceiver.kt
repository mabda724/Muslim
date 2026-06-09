package com.Blackbox.muslim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, starting services...")

            // Start prayer time service
            try {
                val serviceIntent = Intent(context, PrayerTimeService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start PrayerTimeService", e)
            }

            // Schedule alarms as backup
            try { PrayerScheduler.scheduleNextPrayer(context) } catch (e: Exception) { Log.e("BootReceiver", "PrayerScheduler error", e) }
            try { PrayerEndedReceiver.schedulePrayerEndedAlarms(context) } catch (e: Exception) { Log.e("BootReceiver", "PrayerEndedReceiver error", e) }
            try { DailyDhikrReceiver.scheduleDailyDhikr(context) } catch (e: Exception) { Log.e("BootReceiver", "DailyDhikrReceiver error", e) }
        }
    }
}
