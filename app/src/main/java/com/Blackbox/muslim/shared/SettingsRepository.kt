package com.Blackbox.muslim.shared

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("muslim_prefs", Context.MODE_PRIVATE)

    fun getLatitude(): Double = prefs.getFloat("latitude", 30.0444f).toDouble()
    fun setLatitude(value: Double) = prefs.edit().putFloat("latitude", value.toFloat()).apply()

    fun getLongitude(): Double = prefs.getFloat("longitude", 31.2357f).toDouble()
    fun setLongitude(value: Double) = prefs.edit().putFloat("longitude", value.toFloat()).apply()

    fun isBlockingEnabled(): Boolean = prefs.getBoolean("blocking_enabled", true)
    fun setBlockingEnabled(value: Boolean) = prefs.edit().putBoolean("blocking_enabled", value).apply()

    fun getPrayerWindowMinutes(): Int = prefs.getInt("prayer_window", 20)
    fun setPrayerWindowMinutes(value: Int) = prefs.edit().putInt("prayer_window", value).apply()

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(value: Boolean) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    fun isAdhanEnabled(): Boolean = prefs.getBoolean("adhan_enabled", true)
    fun setAdhanEnabled(value: Boolean) = prefs.edit().putBoolean("adhan_enabled", value).apply()

    fun isMuteModeEnabled(): Boolean = prefs.getBoolean("mute_mode_enabled", false)
    fun setMuteModeEnabled(value: Boolean) = prefs.edit().putBoolean("mute_mode_enabled", value).apply()

    fun getMuteEndTime(): Long = prefs.getLong("mute_end_time", 0L)
    fun setMuteEndTime(value: Long) = prefs.edit().putLong("mute_end_time", value).apply()

    fun isMuteActive(): Boolean {
        if (!isMuteModeEnabled()) return false
        val endTime = getMuteEndTime()
        return endTime > 0 && System.currentTimeMillis() < endTime
    }

    fun getCalculationMethod(): String = prefs.getString("calculation_method", "MUSLIM_WORLD_LEAGUE") ?: "MUSLIM_WORLD_LEAGUE"
    fun setCalculationMethod(value: String) = prefs.edit().putString("calculation_method", value).apply()

    fun isFajrEnabled(): Boolean = prefs.getBoolean("fajr_enabled", true)
    fun setFajrEnabled(value: Boolean) = prefs.edit().putBoolean("fajr_enabled", value).apply()

    fun isDhuhrEnabled(): Boolean = prefs.getBoolean("dhuhr_enabled", true)
    fun setDhuhrEnabled(value: Boolean) = prefs.edit().putBoolean("dhuhr_enabled", value).apply()

    fun isAsrEnabled(): Boolean = prefs.getBoolean("asr_enabled", true)
    fun setAsrEnabled(value: Boolean) = prefs.edit().putBoolean("asr_enabled", value).apply()

    fun isMaghribEnabled(): Boolean = prefs.getBoolean("maghrib_enabled", true)
    fun setMaghribEnabled(value: Boolean) = prefs.edit().putBoolean("maghrib_enabled", value).apply()

    fun isIshaEnabled(): Boolean = prefs.getBoolean("isha_enabled", true)
    fun setIshaEnabled(value: Boolean) = prefs.edit().putBoolean("isha_enabled", value).apply()

    fun getBlockedApps(): Set<String> {
        val apps = prefs.getStringSet("blocked_apps", null)
        return apps ?: getDefaultBlockedApps()
    }

    fun setBlockedApps(apps: Set<String>) = prefs.edit().putStringSet("blocked_apps", apps).apply()

    // Test Mode
    fun isTestModeEnabled(): Boolean = prefs.getBoolean("test_mode_enabled", false)
    fun setTestModeEnabled(value: Boolean) = prefs.edit().putBoolean("test_mode_enabled", value).apply()

    fun getTestPrayerName(): String = prefs.getString("test_prayer_name", "Fajr") ?: "Fajr"
    fun setTestPrayerName(value: String) = prefs.edit().putString("test_prayer_name", value).apply()

    fun getTestPrayerHour(): Int = prefs.getInt("test_prayer_hour", 4)
    fun setTestPrayerHour(value: Int) = prefs.edit().putInt("test_prayer_hour", value).apply()

    fun getTestPrayerMinute(): Int = prefs.getInt("test_prayer_minute", 30)
    fun setTestPrayerMinute(value: Int) = prefs.edit().putInt("test_prayer_minute", value).apply()

    private fun getDefaultBlockedApps(): Set<String> = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.zhiliaoapp.musically",
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.spotify.music",
        "com.discord",
        "com.reddit.frontpage",
        "com.pinterest"
    )
}
