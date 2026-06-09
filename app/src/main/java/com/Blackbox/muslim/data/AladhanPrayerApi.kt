package com.Blackbox.muslim.data

import android.util.Log
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class AladhanPrayerApi {

    companion object {
        private const val TAG = "AladhanPrayerApi"
        private const val BASE_URL = "https://api.aladhan.com/v1/timingsByCity"

        fun getMethodCode(methodName: String): String {
            return when (methodName) {
                "EGYPTIAN" -> "2"
                "KARACHI" -> "1"
                "UMM_AL_QURA" -> "3"
                "MUSLIM_WORLD_LEAGUE" -> "8"
                else -> "8"
            }
        }
    }

    data class PrayerTimesResult(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    interface PrayerTimesCallback {
        fun onSuccess(times: Map<String, String>)
        fun onError(error: String)
    }

    fun fetchPrayerTimes(
        city: String,
        country: String,
        method: String,
        callback: PrayerTimesCallback
    ) {
        Thread {
            try {
                val methodCode = getMethodCode(method)
                val url = "$BASE_URL?city=$city&country=$country&method=$methodCode"
                Log.d(TAG, "Fetching from: $url")

                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val response = connection.getInputStream().bufferedReader().use { it.readText() }

                val json = JSONObject(response)
                val data = json.getJSONObject("data")
                val timings = data.getJSONObject("timings")

                val times = mutableMapOf<String, String>()
                times["Fajr"] = extractTime(timings.getString("Fajr"))
                times["Sunrise"] = extractTime(timings.getString("Sunrise"))
                times["Dhuhr"] = extractTime(timings.getString("Dhuhr"))
                times["Asr"] = extractTime(timings.getString("Asr"))
                times["Maghrib"] = extractTime(timings.getString("Maghrib"))
                times["Isha"] = extractTime(timings.getString("Isha"))

                Log.d(TAG, "Fetched times: $times")
                callback.onSuccess(times)
            } catch (e: Exception) {
                Log.e(TAG, "Fetch error", e)
                callback.onError(e.message ?: "Unknown error")
            }
        }.start()
    }

    fun fetchPrayerTimesSync(
        city: String,
        country: String,
        method: String
    ): Map<String, String>? {
        return try {
            val methodCode = getMethodCode(method)
            val url = "$BASE_URL?city=$city&country=$country&method=$methodCode"
            Log.d(TAG, "Fetching sync from: $url")

            val connection = URL(url).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val response = connection.getInputStream().bufferedReader().use { it.readText() }

            val json = JSONObject(response)
            val data = json.getJSONObject("data")
            val timings = data.getJSONObject("timings")

            val times = mutableMapOf<String, String>()
            times["Fajr"] = extractTime(timings.getString("Fajr"))
            times["Sunrise"] = extractTime(timings.getString("Sunrise"))
            times["Dhuhr"] = extractTime(timings.getString("Dhuhr"))
            times["Asr"] = extractTime(timings.getString("Asr"))
            times["Maghrib"] = extractTime(timings.getString("Maghrib"))
            times["Isha"] = extractTime(timings.getString("Isha"))

            Log.d(TAG, "Fetched sync times: $times")
            times
        } catch (e: Exception) {
            Log.e(TAG, "Fetch sync error", e)
            null
        }
    }

    private fun extractTime(timeStr: String): String {
        val cleaned = timeStr.replace(Regex("\\s*\\(.*\\)"), "").trim()
        return if (cleaned.length >= 5) cleaned.substring(0, 5) else cleaned
    }
}
