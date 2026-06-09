package com.Blackbox.muslim.shared

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.Prayer
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class PrayerManager {

    data class PrayerInfo(
        val name: String,
        val nameArabic: String,
        val timeMillis: Long,
        val prayer: Prayer
    )

    private fun getCalculationMethod(methodName: String): CalculationMethod {
        return when (methodName) {
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN
            "KARACHI" -> CalculationMethod.KARACHI
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA
            "GULF" -> CalculationMethod.GULF
            "TEHRAN" -> CalculationMethod.TEHRAN
            "JAFARI" -> CalculationMethod.JAFARI
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
    }

    private fun nowMillis(): Long = System.currentTimeMillis()

    fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE"
    ): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val localDate = now.toLocalDateTime(tz).date
        val date = DateComponents(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        val params = getCalculationMethod(methodName).parameters
        return PrayerTimes(coordinates, date, params)
    }

    private fun PrayerTimes.toInstantMillis(prayer: Prayer): Long {
        val instant = when (prayer) {
            Prayer.FAJR -> this.fajr
            Prayer.SUNRISE -> this.sunrise
            Prayer.DHUHR -> this.dhuhr
            Prayer.ASR -> this.asr
            Prayer.MAGHRIB -> this.maghrib
            Prayer.ISHA -> this.isha
            Prayer.NONE -> this.fajr
        }
        return instant.toEpochMilliseconds()
    }

    fun getPrayerTimesList(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    ): List<PrayerInfo> {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)

        return listOf(
            PrayerInfo("Fajr", "الفجر", prayerTimes.toInstantMillis(Prayer.FAJR), Prayer.FAJR),
            PrayerInfo("Sunrise", "الشروق", prayerTimes.toInstantMillis(Prayer.SUNRISE), Prayer.SUNRISE),
            PrayerInfo("Dhuhr", "الظهر", prayerTimes.toInstantMillis(Prayer.DHUHR), Prayer.DHUHR),
            PrayerInfo("Asr", "العصر", prayerTimes.toInstantMillis(Prayer.ASR), Prayer.ASR),
            PrayerInfo("Maghrib", "المغرب", prayerTimes.toInstantMillis(Prayer.MAGHRIB), Prayer.MAGHRIB),
            PrayerInfo("Isha", "العشاء", prayerTimes.toInstantMillis(Prayer.ISHA), Prayer.ISHA)
        ).filter { enabledPrayers.contains(it.name) }
    }

    fun isCurrentlyPrayerTime(
        latitude: Double,
        longitude: Double,
        windowMinutes: Int = 20,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    ): Boolean {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = nowMillis()
        val windowMillis = windowMinutes * 60 * 1000L

        val prayerList = mutableListOf<Long>()
        if (enabledPrayers.contains("Fajr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.FAJR))
        if (enabledPrayers.contains("Dhuhr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.DHUHR))
        if (enabledPrayers.contains("Asr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.ASR))
        if (enabledPrayers.contains("Maghrib")) prayerList.add(prayerTimes.toInstantMillis(Prayer.MAGHRIB))
        if (enabledPrayers.contains("Isha")) prayerList.add(prayerTimes.toInstantMillis(Prayer.ISHA))

        return prayerList.any { prayerTimeMillis ->
            val diff = now - prayerTimeMillis
            diff in 0..windowMillis
        }
    }

    fun getNextPrayerTime(
        latitude: Double,
        longitude: Double,
        methodName: String = "MUSLIM_WORLD_LEAGUE"
    ): PrayerInfo? {
        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = nowMillis()

        val prayers = listOf(
            PrayerInfo("Fajr", "الفجر", prayerTimes.toInstantMillis(Prayer.FAJR), Prayer.FAJR),
            PrayerInfo("Dhuhr", "الظهر", prayerTimes.toInstantMillis(Prayer.DHUHR), Prayer.DHUHR),
            PrayerInfo("Asr", "العصر", prayerTimes.toInstantMillis(Prayer.ASR), Prayer.ASR),
            PrayerInfo("Maghrib", "المغرب", prayerTimes.toInstantMillis(Prayer.MAGHRIB), Prayer.MAGHRIB),
            PrayerInfo("Isha", "العشاء", prayerTimes.toInstantMillis(Prayer.ISHA), Prayer.ISHA)
        )

        return prayers.firstOrNull { it.timeMillis > now }
            ?: prayers.firstOrNull()
    }

    fun getRemainingPrayerMinutes(
        latitude: Double,
        longitude: Double,
        windowMinutes: Int = 20,
        methodName: String = "MUSLIM_WORLD_LEAGUE",
        enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    ): Int {
        if (!isCurrentlyPrayerTime(latitude, longitude, windowMinutes, methodName, enabledPrayers)) return -1

        val prayerTimes = getPrayerTimes(latitude, longitude, methodName)
        val now = nowMillis()
        val windowMillis = windowMinutes * 60 * 1000L

        val prayerList = mutableListOf<Long>()
        if (enabledPrayers.contains("Fajr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.FAJR))
        if (enabledPrayers.contains("Dhuhr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.DHUHR))
        if (enabledPrayers.contains("Asr")) prayerList.add(prayerTimes.toInstantMillis(Prayer.ASR))
        if (enabledPrayers.contains("Maghrib")) prayerList.add(prayerTimes.toInstantMillis(Prayer.MAGHRIB))
        if (enabledPrayers.contains("Isha")) prayerList.add(prayerTimes.toInstantMillis(Prayer.ISHA))

        for (prayerTimeMillis in prayerList) {
            val diff = now - prayerTimeMillis
            if (diff in 0..windowMillis) {
                val endMillis = prayerTimeMillis + windowMillis
                val remaining = ((endMillis - now) / 60000).toInt()
                return remaining.coerceAtLeast(0)
            }
        }
        return -1
    }

    fun formatTime(millis: Long): String {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        val tz = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(tz)
        return String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)
    }
}
