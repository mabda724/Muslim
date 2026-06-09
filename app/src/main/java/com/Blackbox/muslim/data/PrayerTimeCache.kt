package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class PrayerTimeCache(context: Context) {

    companion object {
        private const val TAG = "PrayerTimeCache"
        private const val PREFS_NAME = "muslim_prefs"
        private const val KEY_PREFIX = "cached_prayer_times_"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(times: Map<String, String>, date: String) {
        try {
            val json = JSONObject()
            for ((key, value) in times) {
                json.put(key, value)
            }
            prefs.edit().putString(KEY_PREFIX + date, json.toString()).apply()
            Log.d(TAG, "Cached prayer times for $date: $times")
        } catch (e: Exception) {
            Log.e(TAG, "Cache save error", e)
        }
    }

    fun getTodayTimes(): Map<String, String>? {
        val today = getTodayDate()
        return getTimesForDate(today)
    }

    fun getTimesForDate(date: String): Map<String, String>? {
        try {
            val jsonStr = prefs.getString(KEY_PREFIX + date, null) ?: return null
            val json = JSONObject(jsonStr)
            val times = mutableMapOf<String, String>()

            val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
            for (key in keys) {
                if (json.has(key)) {
                    times[key] = json.getString(key)
                }
            }

            Log.d(TAG, "Retrieved cached times for $date: $times")
            return times
        } catch (e: Exception) {
            Log.e(TAG, "Cache read error", e)
            return null
        }
    }

    fun isCachedToday(): Boolean {
        val today = getTodayDate()
        return prefs.contains(KEY_PREFIX + today)
    }

    fun clearOldCaches() {
        val today = getTodayDate()
        val yesterday = getYesterdayDate()
        prefs.edit().remove(KEY_PREFIX + yesterday).apply()
        Log.d(TAG, "Cleared old cache entries")
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(System.currentTimeMillis())
    }

    private fun getYesterdayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(System.currentTimeMillis() - 86400000L)
    }
}
