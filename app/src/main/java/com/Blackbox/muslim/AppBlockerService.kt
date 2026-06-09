package com.Blackbox.muslim

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.Blackbox.muslim.shared.PrayerManager
import com.Blackbox.muslim.shared.SettingsRepository
import java.util.Calendar
import java.util.Date

class AppBlockerService : AccessibilityService() {

    private lateinit var prayerManager: PrayerManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationHelper: PrayerNotificationHelper
    private var overlay: BlockingOverlay? = null
    private var lastBlockedPackage: String? = null
    private var blockCount = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        prayerManager = PrayerManager()
        settingsRepository = SettingsRepository(this)
        notificationHelper = PrayerNotificationHelper(this)
        notificationHelper.createNotificationChannel()
        overlay = BlockingOverlay(this)
    }

    private fun getTestOverrides(): Map<String, Date>? {
        if (!settingsRepository.isTestModeEnabled()) return null

        val hour = settingsRepository.getTestPrayerHour()
        val minute = settingsRepository.getTestPrayerMinute()
        val prayerName = settingsRepository.getTestPrayerName()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        return mapOf(prayerName to cal.time)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName == "com.Blackbox.muslim") return
            if (packageName == "com.android.settings") return
            if (packageName == "com.android.systemui") return

            val lat = settingsRepository.getLatitude()
            val lng = settingsRepository.getLongitude()
            val method = settingsRepository.getCalculationMethod()
            val window = settingsRepository.getPrayerWindowMinutes()
            val testOverrides = getTestOverrides()

            val enabledPrayers = mutableSetOf<String>()
            if (settingsRepository.isFajrEnabled()) enabledPrayers.add("Fajr")
            if (settingsRepository.isDhuhrEnabled()) enabledPrayers.add("Dhuhr")
            if (settingsRepository.isAsrEnabled()) enabledPrayers.add("Asr")
            if (settingsRepository.isMaghribEnabled()) enabledPrayers.add("Maghrib")
            if (settingsRepository.isIshaEnabled()) enabledPrayers.add("Isha")

            if (!settingsRepository.isBlockingEnabled()) return

            if (prayerManager.isCurrentlyPrayerTime(lat, lng, window, method, enabledPrayers, testOverrides)) {
                val blockedApps = settingsRepository.getBlockedApps()

                if (blockedApps.contains(packageName)) {
                    blockCount++
                    val remaining = prayerManager.getRemainingPrayerMinutes(lat, lng, window, method, enabledPrayers, testOverrides)
                    val nextPrayer = prayerManager.getNextPrayerTime(lat, lng, method, testOverrides)

                    if (lastBlockedPackage != packageName || blockCount % 5 == 1) {
                        showBlockingUI(nextPrayer, remaining)
                                    if (!notificationHelper.isBlockingNotificationMuted()) {
                                        notificationHelper.showBlockingNotification(
                                            nextPrayer?.name ?: "الصلاة",
                                            remaining
                                        )
                                    }
                    }

                    lastBlockedPackage = packageName
                    goHome()
                }
            } else {
                if (overlay?.isShowing() == true) {
                    overlay?.dismiss()
                    notificationHelper.cancelBlockingNotification()
                    lastBlockedPackage = null
                    blockCount = 0
                }
            }
        }
    }

    private fun showBlockingUI(nextPrayer: PrayerManager.PrayerInfo?, remaining: Int) {
        val prayerName = nextPrayer?.name ?: "الصلاة"
        val prayerNameArabic = nextPrayer?.nameArabic ?: "الصلاة"
        overlay?.show(prayerName, prayerNameArabic, remaining)
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        overlay?.dismiss()
    }
}
