package com.Blackbox.muslim.shared

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class PrayerManager {

    data class PrayerInfo(
        val name: String,
        val nameArabic: String,
        val time: Date
    )

    private fun getCalculationMethod(methodName: String): CalculationMethod {
        return when (methodName) {
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN
            "KARACHI" -> CalculationMethod.KARACHI
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA
            "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE"
    ): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val calendar = Calendar.getInstance()
        val date = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val params = getCalculationMethod(methodName).parameters
        return PrayerTimes(coordinates, date, params)
    }

    fun getPrayerTimesList(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
        testOverrides: Map<String, Date>? = null
    ): List<PrayerInfo> {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)

        val list = listOf(
            PrayerInfo("Fajr", "الفجر", testOverrides?.get("Fajr") ?: prayerTimes.fajr),
            PrayerInfo("Sunrise", "الشروق", prayerTimes.sunrise),
            PrayerInfo("Dhuhr", "الظهر", testOverrides?.get("Dhuhr") ?: prayerTimes.dhuhr),
            PrayerInfo("Asr", "العصر", testOverrides?.get("Asr") ?: prayerTimes.asr),
            PrayerInfo("Maghrib", "المغرب", testOverrides?.get("Maghrib") ?: prayerTimes.maghrib),
            PrayerInfo("Isha", "العشاء", testOverrides?.get("Isha") ?: prayerTimes.isha)
        )

        return list.filter { enabledPrayers.contains(it.name) }
    }

    fun isCurrentlyPrayerTime(
        latitude: Double,
        longitude: Double,
        windowMinutes: Int = 20,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
        testOverrides: Map<String, Date>? = null
    ): Boolean {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = Date()
        val windowMillis = windowMinutes * 60 * 1000L

        val prayerList = mutableListOf<Date>()
        if (enabledPrayers.contains("Fajr")) prayerList.add(testOverrides?.get("Fajr") ?: prayerTimes.fajr)
        if (enabledPrayers.contains("Dhuhr")) prayerList.add(testOverrides?.get("Dhuhr") ?: prayerTimes.dhuhr)
        if (enabledPrayers.contains("Asr")) prayerList.add(testOverrides?.get("Asr") ?: prayerTimes.asr)
        if (enabledPrayers.contains("Maghrib")) prayerList.add(testOverrides?.get("Maghrib") ?: prayerTimes.maghrib)
        if (enabledPrayers.contains("Isha")) prayerList.add(testOverrides?.get("Isha") ?: prayerTimes.isha)

        return prayerList.any { prayerTime ->
            val diff = now.time - prayerTime.time
            diff in 0..windowMillis
        }
    }

    fun getNextPrayerTime(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        testOverrides: Map<String, Date>? = null
    ): PrayerInfo? {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = Date()

        val prayers = listOf(
            PrayerInfo("Fajr", "الفجر", testOverrides?.get("Fajr") ?: prayerTimes.fajr),
            PrayerInfo("Dhuhr", "الظهر", testOverrides?.get("Dhuhr") ?: prayerTimes.dhuhr),
            PrayerInfo("Asr", "العصر", testOverrides?.get("Asr") ?: prayerTimes.asr),
            PrayerInfo("Maghrib", "المغرب", testOverrides?.get("Maghrib") ?: prayerTimes.maghrib),
            PrayerInfo("Isha", "العشاء", testOverrides?.get("Isha") ?: prayerTimes.isha)
        )

        return prayers.firstOrNull { it.time.after(now) }
            ?: prayers.firstOrNull()
    }

    fun getRemainingPrayerMinutes(
        latitude: Double,
        longitude: Double,
        windowMinutes: Int = 20,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
        testOverrides: Map<String, Date>? = null
    ): Int {
        if (!isCurrentlyPrayerTime(latitude, longitude, windowMinutes, methodName, enabledPrayers, testOverrides)) return -1

        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = Date()
        val windowMillis = windowMinutes * 60 * 1000L

        val prayerList = mutableListOf<Date>()
        if (enabledPrayers.contains("Fajr")) prayerList.add(testOverrides?.get("Fajr") ?: prayerTimes.fajr)
        if (enabledPrayers.contains("Dhuhr")) prayerList.add(testOverrides?.get("Dhuhr") ?: prayerTimes.dhuhr)
        if (enabledPrayers.contains("Asr")) prayerList.add(testOverrides?.get("Asr") ?: prayerTimes.asr)
        if (enabledPrayers.contains("Maghrib")) prayerList.add(testOverrides?.get("Maghrib") ?: prayerTimes.maghrib)
        if (enabledPrayers.contains("Isha")) prayerList.add(testOverrides?.get("Isha") ?: prayerTimes.isha)

        for (prayerTime in prayerList) {
            val diff = now.time - prayerTime.time
            if (diff in 0..windowMillis) {
                val endMillis = prayerTime.time + windowMillis
                val remaining = ((endMillis - now.time) / 60000).toInt()
                return remaining.coerceAtLeast(0)
            }
        }
        return -1
    }

    fun formatTime(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatCountdown(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millis % (1000 * 60)) / 1000
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}
