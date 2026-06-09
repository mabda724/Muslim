package com.Blackbox.muslim.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.DailyDhikrData
import com.Blackbox.muslim.data.GamificationManager
import com.Blackbox.muslim.data.PrayerTracker
import com.google.android.material.switchmaterial.SwitchMaterial
import com.Blackbox.muslim.shared.PrayerManager
import com.Blackbox.muslim.shared.SettingsRepository
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var prayerTracker: PrayerTracker
private lateinit var prayerManager: PrayerManager
private lateinit var settingsRepository: SettingsRepository
private lateinit var gamificationManager: GamificationManager
    private val handler = Handler(Looper.getMainLooper())
    private var dhikrIndex = 0
    private var categoryIndex = 0
    private var dhikrRunnable: Runnable? = null

    private val dhikrCategories = listOf("dailyAthkar", "salawat", "istighfar", "tips")

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prayerTracker = PrayerTracker(this)
        prayerTracker = PrayerTracker(this)
  prayerManager = PrayerManager()
  settingsRepository = SettingsRepository(this)
  gamificationManager = GamificationManager(this)

        setupUI()
        loadDashboard()
        startDhikrRotation()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun setupUI() {
        val btnBack = findViewById<Button>(R.id.btnBackDashboard)
        val btnReports = findViewById<Button>(R.id.btnReports)
        btnBack.setOnClickListener { finish() }

        btnReports.setOnClickListener {
            startActivity(Intent(this, com.Blackbox.muslim.ui.ReportsActivity::class.java))
        }

  val switchMuteMode = findViewById<SwitchMaterial>(R.id.switchMuteMode)
  switchMuteMode.isChecked = settingsRepository.isMuteModeEnabled()

  switchMuteMode.setOnCheckedChangeListener { _, isChecked ->
    settingsRepository.setMuteModeEnabled(isChecked)
    if (isChecked) {
      val endTime = System.currentTimeMillis() + (60 * 60 * 1000)
      settingsRepository.setMuteEndTime(endTime)
      Toast.makeText(this, "تم كتم التنبيهات لمدة ساعة", Toast.LENGTH_SHORT).show()
    } else {
      settingsRepository.setMuteEndTime(0L)
      Toast.makeText(this, "تم تفعيل التنبيهات", Toast.LENGTH_SHORT).show()
    }
  }

        val btnMarkFajr = findViewById<Button>(R.id.btnMarkFajr)
        val btnMarkDhuhr = findViewById<Button>(R.id.btnMarkDhuhr)
        val btnMarkAsr = findViewById<Button>(R.id.btnMarkAsr)
        val btnMarkMaghrib = findViewById<Button>(R.id.btnMarkMaghrib)
        val btnMarkIsha = findViewById<Button>(R.id.btnMarkIsha)

  val markPrayer = { name: String, btn: Button ->
    vibrate()
    prayerTracker.markPrayed(name, isOnTime = true)
    prayerTracker.updateLongestStreak()
    btn.text = "✓ تم"
    btn.alpha = 0.5f

    AnimationHelper.createSuccessPulse(btn)

    loadDashboard()
    Toast.makeText(this, "تم تسجيل صلاة $name (+${GamificationManager.POINTS_PRAYER_ON_TIME} نقطة)", Toast.LENGTH_SHORT).show()
  }

        btnMarkFajr.setOnClickListener { markPrayer("fajr", btnMarkFajr) }
        btnMarkDhuhr.setOnClickListener { markPrayer("dhuhr", btnMarkDhuhr) }
        btnMarkAsr.setOnClickListener { markPrayer("asr", btnMarkAsr) }
        btnMarkMaghrib.setOnClickListener { markPrayer("maghrib", btnMarkMaghrib) }
        btnMarkIsha.setOnClickListener { markPrayer("isha", btnMarkIsha) }
    }

    private fun loadDashboard() {
        val today = prayerTracker.getDayStats()
        val weekStats = prayerTracker.getWeekStats()
        val monthStats = prayerTracker.getMonthStats()
        val streak = prayerTracker.getStreak()
        val longestStreak = prayerTracker.getLongestStreak()
        val weeklyAvg = prayerTracker.getWeeklyAverage()
        val totalMonth = prayerTracker.getTotalPrayersThisMonth()
        val totalAllTime = prayerTracker.getTotalPrayersAllTime()
        val totalPoints = gamificationManager.getTotalPoints()
        val currentLevel = gamificationManager.getCurrentLevel()
        val levelProgress = gamificationManager.getLevelProgress()

        // Today's progress
        val tvTodayProgress = findViewById<TextView>(R.id.tvTodayProgress)
        val tvPrayerCount = findViewById<TextView>(R.id.tvPrayerCount)
        val progressToday = findViewById<ProgressBar>(R.id.progressToday)

        tvTodayProgress.text = "اليوم - ${today.percentage}%"
        tvPrayerCount.text = "${today.prayedCount} / 5 صلوات"
        progressToday.progress = today.percentage

        if (today.percentage == 100) {
            vibrate()
            Toast.makeText(this, "مبارك! لقد أتممت صلوات اليوم 🎉", Toast.LENGTH_LONG).show()
            tvTodayProgress.text = "اليوم - مكتمل! 🎉"
            val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
            progressToday.startAnimation(pulseAnim)
        }

        // Mark prayed buttons
        val btnMarkFajr = findViewById<Button>(R.id.btnMarkFajr)
        val btnMarkDhuhr = findViewById<Button>(R.id.btnMarkDhuhr)
        val btnMarkAsr = findViewById<Button>(R.id.btnMarkAsr)
        val btnMarkMaghrib = findViewById<Button>(R.id.btnMarkMaghrib)
        val btnMarkIsha = findViewById<Button>(R.id.btnMarkIsha)

        if (today.fajr) { btnMarkFajr.text = "✓ تم"; btnMarkFajr.alpha = 0.5f }
        if (today.dhuhr) { btnMarkDhuhr.text = "✓ تم"; btnMarkDhuhr.alpha = 0.5f }
        if (today.asr) { btnMarkAsr.text = "✓ تم"; btnMarkAsr.alpha = 0.5f }
        if (today.maghrib) { btnMarkMaghrib.text = "✓ تم"; btnMarkMaghrib.alpha = 0.5f }
        if (today.isha) { btnMarkIsha.text = "✓ تم"; btnMarkIsha.alpha = 0.5f }

        // Stats
        val tvStreak = findViewById<TextView>(R.id.tvStreak)
        val tvLongestStreak = findViewById<TextView>(R.id.tvLongestStreak)
        val tvWeeklyAvg = findViewById<TextView>(R.id.tvWeeklyAvg)
        val tvTotalMonth = findViewById<TextView>(R.id.tvTotalMonth)
        val tvTotalAllTime = findViewById<TextView>(R.id.tvTotalAllTime)

        tvStreak.text = "$streak يوم"
        tvLongestStreak.text = "$longestStreak يوم"
        tvWeeklyAvg.text = "$weeklyAvg%"
        tvTotalMonth.text = "$totalMonth صلاة"
        tvTotalAllTime.text = "$totalAllTime صلاة"

        // Weekly chart
        val tvWeekChart = findViewById<TextView>(R.id.tvWeekChart)
        val sdf = SimpleDateFormat("EEE", Locale("ar"))
        val weekBars = StringBuilder()
        weekStats.forEach { stat ->
            val dayName = sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.date)!!)
            val bar = "█".repeat(stat.prayedCount) + "░".repeat(5 - stat.prayedCount)
            weekBars.appendLine("$dayName: $bar ${stat.percentage}%")
        }
        tvWeekChart.text = weekBars.toString()

        // Gamification info
        val tvTotalPoints = findViewById<TextView>(R.id.tvTotalPoints)
        val tvCurrentLevel = findViewById<TextView>(R.id.tvCurrentLevel)
        val progressLevel = findViewById<ProgressBar>(R.id.progressLevel)

        tvTotalPoints.text = "$totalPoints نقطة"
        tvCurrentLevel.text = "${currentLevel.level} - ${currentLevel.title}"
        progressLevel.progress = (levelProgress * 100).toInt()

        // Encouragement
        val tvEncouragement = findViewById<TextView>(R.id.tvEncouragement)
        val tvMotivational = findViewById<TextView>(R.id.tvMotivational)
        tvEncouragement.text = prayerTracker.getEncouragementMessage()
        tvMotivational.text = prayerTracker.getMotivationalQuote()

        // Animate encouragement
        tvEncouragement.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
        tvMotivational.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
    }

    private fun startDhikrRotation() {
        val tvDhikrArabic = findViewById<TextView>(R.id.tvDhikrArabic)
        val tvDhikrTranslation = findViewById<TextView>(R.id.tvDhikrTranslation)
        val tvDhikrCategory = findViewById<TextView>(R.id.tvDhikrCategory)

        val rotateDhikr = object : Runnable {
            override fun run() {
                val category = dhikrCategories[categoryIndex]
                val dhikrList = when (category) {
                    "salawat" -> DailyDhikrData.salawatOnProphet
                    "istighfar" -> DailyDhikrData.istighfar
                    "dailyAthkar" -> DailyDhikrData.dailyAthkar
                    "tips" -> DailyDhikrData.dailyTips.map {
                        DailyDhikrData.DhikrItem(it, "", it, 0, "نصيحة", "")
                    }
                    else -> DailyDhikrData.dailyAthkar
                }

                if (dhikrIndex < dhikrList.size) {
                    val item = dhikrList[dhikrIndex]
                    tvDhikrArabic.text = item.arabic
                    tvDhikrTranslation.text = item.translation
                    tvDhikrCategory.text = when (category) {
                        "salawat" -> "صلاة على النبي ﷺ"
                        "istighfar" -> "استغفار"
                        "dailyAthkar" -> "أذكار"
                        "tips" -> "نصيحة يومية"
                        else -> ""
                    }

                    tvDhikrArabic.animation = AnimationUtils.loadAnimation(this@DashboardActivity, R.anim.fade_in_up)
                    tvDhikrTranslation.animation = AnimationUtils.loadAnimation(this@DashboardActivity, R.anim.fade_in_up)

                    dhikrIndex++
                    if (dhikrIndex >= dhikrList.size) {
                        dhikrIndex = 0
                        categoryIndex = (categoryIndex + 1) % dhikrCategories.size
                    }
                }

                handler.postDelayed(this, 30000)
            }
        }

        handler.postDelayed(rotateDhikr, 2000)
        dhikrRunnable = rotateDhikr
    }

    override fun onDestroy() {
        super.onDestroy()
        dhikrRunnable?.let { handler.removeCallbacks(it) }
    }
}
