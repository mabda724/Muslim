package com.Blackbox.muslim.shared

expect class SettingsRepository {
    fun getLatitude(): Double
    fun setLatitude(value: Double)
    fun getLongitude(): Double
    fun setLongitude(value: Double)
    fun isBlockingEnabled(): Boolean
    fun setBlockingEnabled(value: Boolean)
    fun getPrayerWindowMinutes(): Int
    fun setPrayerWindowMinutes(value: Int)
    fun isNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(value: Boolean)
    fun getCalculationMethod(): String
    fun setCalculationMethod(value: String)
    fun isFajrEnabled(): Boolean
    fun setFajrEnabled(value: Boolean)
    fun isDhuhrEnabled(): Boolean
    fun setDhuhrEnabled(value: Boolean)
    fun isAsrEnabled(): Boolean
    fun setAsrEnabled(value: Boolean)
    fun isMaghribEnabled(): Boolean
    fun setMaghribEnabled(value: Boolean)
    fun isIshaEnabled(): Boolean
    fun setIshaEnabled(value: Boolean)
    fun getBlockedApps(): Set<String>
    fun setBlockedApps(apps: Set<String>)
}
