package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class SmartReminderManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("smart_reminders", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Track when user opens app
    fun recordAppOpenTime() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val dateKey = dateFormat.format(now.time)

        // Record app open time for today
        prefs.edit()
            .putInt("app_open_hour_$dateKey", hour)
            .putInt("app_open_day_$dateKey", dayOfWeek)
            .apply()

        // Update average app open time
        updateAverageAppOpenTime(hour, dayOfWeek)
    }

    // Track when user prays after adhan
    fun recordPrayerTimeAfterAdhan(prayerName: String, minutesAfterAdhan: Int) {
        val today = dateFormat.format(Date())
        val key = "${today}_${prayerName}_minutes_after"
        prefs.edit().putInt(key, minutesAfterAdhan).apply()

        // Update average for this prayer
        updateAveragePrayerDelay(prayerName, minutesAfterAdhan)
    }

    // Get smart reminder time for a prayer
    fun getSmartReminderTime(prayerName: String): Int {
        val avgMinutesAfterAdhan = getAveragePrayerDelay(prayerName)

        // If user typically prays 10 minutes after adhan, remind them 5 minutes before
        // If user typically prays 30 minutes after, remind them 10 minutes before
        return when {
            avgMinutesAfterAdhan <= 5 -> 2
            avgMinutesAfterAdhan <= 10 -> 5
            avgMinutesAfterAdhan <= 20 -> 8
            avgMinutesAfterAdhan <= 30 -> 10
            else -> 12
        }
    }

    // Get optimal notification time based on app usage
    fun getOptimalNotificationHour(): Int {
        val avgHour = getAverageAppOpenTime()

        // If user opens app mostly in morning (6-12), suggest morning reminders
        // If mostly afternoon (12-18), suggest afternoon
        // If mostly evening (18-24), suggest evening

        return when {
            avgHour in 6..12 -> 8
            avgHour in 13..18 -> 14
            avgHour in 19..24 -> 20
            else -> 10
        }
    }

    // Get prayer-specific insights
    fun getPrayerInsights(prayerName: String): String {
        val avgDelay = getAveragePrayerDelay(prayerName)
        val completionRate = getPrayerCompletionRate(prayerName)

        return when {
            avgDelay <= 5 && completionRate >= 90 ->
                "أنت محافظ على $prayerName! 🌟"

            avgDelay <= 15 && completionRate >= 80 ->
                "أداء جيد على $prayerName، يمكنك تحسين التوقيت ⏰"

            avgDelay > 30 && completionRate >= 70 ->
                "حاول تقريب $prayerName من وقت الأذان 🕌"

            completionRate < 70 ->
                "انتبه لصلاتك، خاصة $prayerName ⚠️"

            else ->
                "استمر في المحافظة على صلواتك 💪"
        }
    }

    // Helper functions
    private fun updateAverageAppOpenTime(hour: Int, dayOfWeek: Int) {
        val currentSum = prefs.getInt("total_app_open_hours", 0)
        val currentCount = prefs.getInt("total_app_opens", 0)

        prefs.edit()
            .putInt("total_app_open_hours", currentSum + hour)
            .putInt("total_app_opens", currentCount + 1)
            .apply()
    }

    private fun getAverageAppOpenTime(): Int {
        val totalSum = prefs.getInt("total_app_open_hours", 0)
        val totalCount = prefs.getInt("total_app_opens", 0)

        return if (totalCount > 0) totalSum / totalCount else 10
    }

    private fun updateAveragePrayerDelay(prayerName: String, minutesAfter: Int) {
        val prayerKey = "${prayerName}_total_delay"
        val countKey = "${prayerName}_delay_count"

        val currentSum = prefs.getInt(prayerKey, 0)
        val currentCount = prefs.getInt(countKey, 0)

        prefs.edit()
            .putInt(prayerKey, currentSum + minutesAfter)
            .putInt(countKey, currentCount + 1)
            .apply()
    }

    private fun getAveragePrayerDelay(prayerName: String): Int {
        val prayerKey = "${prayerName}_total_delay"
        val countKey = "${prayerName}_delay_count"

        val totalSum = prefs.getInt(prayerKey, 0)
        val totalCount = prefs.getInt(countKey, 0)

        return if (totalCount > 0) totalSum / totalCount else 15
    }

    private fun getPrayerCompletionRate(prayerName: String): Int {
        // This would need to be calculated from PrayerTracker data
        // For now, return a default value
        return prefs.getInt("${prayerName}_completion_rate", 75)
    }

    // Suggest best reminder settings
    fun getOptimalReminderSettings(): OptimalReminderSettings {
        return OptimalReminderSettings(
            recommendedMinutesBefore = getSmartReminderTime("dhuhr"),
            recommendedNotificationHour = getOptimalNotificationHour(),
            insights = listOf(
                getPrayerInsights("fajr"),
                getPrayerInsights("dhuhr"),
                getPrayerInsights("asr")
            )
        )
    }

    data class OptimalReminderSettings(
        val recommendedMinutesBefore: Int,
        val recommendedNotificationHour: Int,
        val insights: List<String>
    )
}
