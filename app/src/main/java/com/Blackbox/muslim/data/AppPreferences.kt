package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import com.Blackbox.muslim.R

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("muslim_prefs", Context.MODE_PRIVATE)

    // Location
    fun getLatitude(): Double = prefs.getFloat("latitude", 30.0444f).toDouble()
    fun setLatitude(value: Double) = prefs.edit().putFloat("latitude", value.toFloat()).apply()
    fun getLongitude(): Double = prefs.getFloat("longitude", 31.2357f).toDouble()
    fun setLongitude(value: Double) = prefs.edit().putFloat("longitude", value.toFloat()).apply()
    fun getLocationName(): String = prefs.getString("location_name", "") ?: ""
    fun setLocationName(value: String) = prefs.edit().putString("location_name", value).apply()

    // Blocking
    fun isBlockingEnabled(): Boolean = prefs.getBoolean("blocking_enabled", true)
    fun setBlockingEnabled(value: Boolean) = prefs.edit().putBoolean("blocking_enabled", value).apply()
    fun getPrayerWindowMinutes(): Int = prefs.getInt("prayer_window", 20)
    fun setPrayerWindowMinutes(value: Int) = prefs.edit().putInt("prayer_window", value).apply()

    // Notifications
    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(value: Boolean) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    // Adhan
    fun isAdhanEnabled(): Boolean = prefs.getBoolean("adhan_enabled", true)
    fun setAdhanEnabled(value: Boolean) = prefs.edit().putBoolean("adhan_enabled", value).apply()
    fun getAdhanVolume(): Int = prefs.getInt("adhan_volume", 80)
    fun setAdhanVolume(value: Int) = prefs.edit().putInt("adhan_volume", value).apply()

    // Calculation
    fun getCalculationMethod(): String = prefs.getString("calculation_method", "MUSLIM_WORLD_LEAGUE") ?: "MUSLIM_WORLD_LEAGUE"
    fun setCalculationMethod(value: String) = prefs.edit().putString("calculation_method", value).apply()

    // Prayer toggles
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

    // Blocked apps
    fun getBlockedApps(): Set<String> {
        return prefs.getStringSet("blocked_apps", null) ?: getDefaultBlockedApps()
    }
    fun setBlockedApps(apps: Set<String>) = prefs.edit().putStringSet("blocked_apps", apps).apply()

    // Unblocked apps (whitelist during prayer)
    fun getUnblockedApps(): Set<String> {
        return prefs.getStringSet("unblocked_apps", null) ?: getDefaultUnblockedApps()
    }
    fun setUnblockedApps(apps: Set<String>) = prefs.edit().putStringSet("unblocked_apps", apps).apply()

    // Test Mode
    fun isTestModeEnabled(): Boolean = prefs.getBoolean("test_mode_enabled", false)
    fun setTestModeEnabled(value: Boolean) = prefs.edit().putBoolean("test_mode_enabled", value).apply()
    fun getTestPrayerName(): String = prefs.getString("test_prayer_name", "Fajr") ?: "Fajr"
    fun setTestPrayerName(value: String) = prefs.edit().putString("test_prayer_name", value).apply()
    fun getTestPrayerHour(): Int = prefs.getInt("test_prayer_hour", 4)
    fun setTestPrayerHour(value: Int) = prefs.edit().putInt("test_prayer_hour", value).apply()
    fun getTestPrayerMinute(): Int = prefs.getInt("test_prayer_minute", 30)
    fun setTestPrayerMinute(value: Int) = prefs.edit().putInt("test_prayer_minute", value).apply()

    // Auth
    fun isAuthRequired(): Boolean = prefs.getBoolean("auth_required", true)
    fun setAuthRequired(value: Boolean) = prefs.edit().putBoolean("auth_required", value).apply()
    fun getPinCode(): String = prefs.getString("pin_code", "") ?: ""
    fun setPinCode(value: String) = prefs.edit().putString("pin_code", value).apply()
    fun useBiometric(): Boolean = prefs.getBoolean("use_biometric", true)
    fun setUseBiometric(value: Boolean) = prefs.edit().putBoolean("use_biometric", value).apply()

    // Reminder & Iqamah
    fun getReminderMinutesBefore(): Int = prefs.getInt("reminder_minutes_before", 10)
    fun setReminderMinutesBefore(value: Int) = prefs.edit().putInt("reminder_minutes_before", value.coerceIn(5, 10)).apply()
    fun getIqamahDelayMinutes(): Int = prefs.getInt("iqamah_delay_minutes", 7)
    fun setIqamahDelayMinutes(value: Int) = prefs.edit().putInt("iqamah_delay_minutes", value.coerceIn(7, 10)).apply()

    // City/Country for API
    fun getCity(): String = prefs.getString("city", "Cairo") ?: "Cairo"
    fun setCity(value: String) = prefs.edit().putString("city", value).apply()
    fun getCountry(): String = prefs.getString("country", "Egypt") ?: "Egypt"
    fun setCountry(value: String) = prefs.edit().putString("country", value).apply()

    // Dark/Light mode
    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", true)
    fun setDarkMode(value: Boolean) = prefs.edit().putBoolean("dark_mode", value).apply()

    // Smart Reminders
    fun getSmartRemindersEnabled(): Boolean = prefs.getBoolean("smart_reminders_enabled", false)
    fun setSmartRemindersEnabled(value: Boolean) = prefs.edit().putBoolean("smart_reminders_enabled", value).apply()

    // Theme
    fun getTheme(): String = prefs.getString("app_theme", "default") ?: "default"
    fun setTheme(value: String) = prefs.edit().putString("app_theme", value).apply()

    companion object {
        const val THEME_DEFAULT = "default"
        const val THEME_LIGHT = "light"
        const val THEME_GREEN = "green"
        const val THEME_BLUE = "blue"
        const val THEME_BROWN = "brown"
        const val THEME_PURPLE = "purple"

        fun getThemeResourceId(themeName: String): Int {
            return when (themeName) {
                THEME_LIGHT -> R.style.Theme_Muslim_Light
                THEME_GREEN -> R.style.Theme_Muslim_Green
                THEME_BLUE -> R.style.Theme_Muslim_Blue
                THEME_BROWN -> R.style.Theme_Muslim_Brown
                THEME_PURPLE -> R.style.Theme_Muslim_Purple
                else -> R.style.Theme_Muslim
            }
        }
    }

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

    private fun getDefaultUnblockedApps(): Set<String> = setOf(
        "com.android.phone",
        "com.android.contacts",
        "com.android.mms",
        "com.whatsapp"
    )
}
