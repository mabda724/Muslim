package com.Blackbox.muslim.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.PrayerTracker
import com.Blackbox.muslim.data.GamificationManager
import com.Blackbox.muslim.data.PrayerPatternAnalyzer
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var prayerTracker: PrayerTracker
    private lateinit var gamificationManager: GamificationManager
    private lateinit var patternAnalyzer: PrayerPatternAnalyzer

    private var currentReportType = "week" // week, month

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        prayerTracker = PrayerTracker(this)
        gamificationManager = GamificationManager(this)
        patternAnalyzer = PrayerPatternAnalyzer(this)

        setupUI()
        loadReports()
    }

    private fun setupUI() {
        val btnBack = findViewById<Button>(R.id.btnBackReports)
        val btnWeek = findViewById<Button>(R.id.btnWeekReport)
        val btnMonth = findViewById<Button>(R.id.btnMonthReport)
        val btnAnalyzePattern = findViewById<Button>(R.id.btnAnalyzePattern)

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out)
        }

        btnWeek.setOnClickListener {
            currentReportType = "week"
            loadReports()
        }

        btnMonth.setOnClickListener {
            currentReportType = "month"
            loadReports()
        }

        btnAnalyzePattern.setOnClickListener {
            analyzePattern()
        }
    }

    private fun loadReports() {
        val stats = if (currentReportType == "week") {
            prayerTracker.getWeekStats()
        } else {
            prayerTracker.getMonthStats()
        }

        // Update UI
        updateSummaryStats(stats)
        updatePrayerBreakdown(stats)
        updateTrends(stats)
        updateProgressChart(stats)
    }

    private fun updateSummaryStats(stats: List<PrayerTracker.DayStats>) {
        val tvTotalPrayers = findViewById<TextView>(R.id.tvReportTotalPrayers)
        val tvPercentage = findViewById<TextView>(R.id.tvReportPercentage)
        val tvBestDay = findViewById<TextView>(R.id.tvReportBestDay)
        val tvStreak = findViewById<TextView>(R.id.tvReportStreak)

        val totalPrayers = stats.sumOf { it.prayedCount }
        val totalPossible = stats.size * 5
        val percentage = if (totalPossible > 0) (totalPrayers * 100) / totalPossible else 0

        tvTotalPrayers.text = "$totalPrayers / $totalPossible صلاة"
        tvPercentage.text = "$percentage%"

        // Find best day
        val bestDay = stats.maxByOrNull { it.percentage }
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        tvBestDay.text = bestDay?.let { sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)!!) } ?: "لا يوجد"

        // Streak
        val streak = prayerTracker.getStreak()
        tvStreak.text = "$streak يوم"
    }

    private fun updatePrayerBreakdown(stats: List<PrayerTracker.DayStats>) {
        val tvFajrCount = findViewById<TextView>(R.id.tvFajrCount)
        val tvDhuhrCount = findViewById<TextView>(R.id.tvDhuhrCount)
        val tvAsrCount = findViewById<TextView>(R.id.tvAsrCount)
        val tvMaghribCount = findViewById<TextView>(R.id.tvMaghribCount)
        val tvIshaCount = findViewById<TextView>(R.id.tvIshaCount)

        val fajrCount = stats.count { it.fajr }
        val dhuhrCount = stats.count { it.dhuhr }
        val asrCount = stats.count { it.asr }
        val maghribCount = stats.count { it.maghrib }
        val ishaCount = stats.count { it.isha }

        tvFajrCount.text = "$fajrCount / ${stats.size}"
        tvDhuhrCount.text = "$dhuhrCount / ${stats.size}"
        tvAsrCount.text = "$asrCount / ${stats.size}"
        tvMaghribCount.text = "$maghribCount / ${stats.size}"
        tvIshaCount.text = "$ishaCount / ${stats.size}"
    }

    private fun updateTrends(stats: List<PrayerTracker.DayStats>) {
        val tvTrendMessage = findViewById<TextView>(R.id.tvTrendMessage)

        val firstHalf = stats.take(stats.size / 2)
        val secondHalf = stats.drop(stats.size / 2)

        val firstHalfPercentage = if (firstHalf.isNotEmpty()) {
            (firstHalf.sumOf { it.prayedCount } * 100) / (firstHalf.size * 5)
        } else {
            0
        }

        val secondHalfPercentage = if (secondHalf.isNotEmpty()) {
            (secondHalf.sumOf { it.prayedCount } * 100) / (secondHalf.size * 5)
        } else {
            0
        }

        val trend = when {
            secondHalfPercentage > firstHalfPercentage + 10 -> "تحسن ملحوظ! 📈"
            secondHalfPercentage > firstHalfPercentage -> "تحسن طفيف ↗️"
            secondHalfPercentage < firstHalfPercentage - 10 -> "تراجع ملحوظ! 📉"
            secondHalfPercentage < firstHalfPercentage -> "تراجع طفيف ↘️"
            else -> "مستقر ➡️"
        }

        tvTrendMessage.text = "الاتجاه: $trend"
    }

    private fun updateProgressChart(stats: List<PrayerTracker.DayStats>) {
        val tvChart = findViewById<TextView>(R.id.tvReportChart)
        val sdf = SimpleDateFormat("EEE", Locale("ar"))

        val chartText = StringBuilder()
        stats.forEach { stat ->
            val dayName = sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.date)!!)
            val bar = "█".repeat(stat.prayedCount) + "░".repeat(5 - stat.prayedCount)
            chartText.appendLine("$dayName: $bar ${stat.percentage}%")
        }

        tvChart.text = chartText.toString()
    }

    private fun animateCard(view: View) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun analyzePattern() {
        val analysis = patternAnalyzer.analyzePrayerPatterns(prayerTracker)

        // Save analysis
        patternAnalyzer.savePatternAnalysis(analysis)

        // Display analysis
        val message = StringBuilder()
        message.append("📊 تحليل نمط الصلاة\n\n")
        message.append("أفضل يوم: ${analysis.bestDay}\n")
        message.append("أسوأ يوم: ${analysis.worstDay}\n")
        message.append("أفضل صلاة: ${analysis.bestPrayer}\n")
        message.append("أسوأ صلاة: ${analysis.worstPrayer}\n")
        message.append("متوسط الإنجاز: ${analysis.averageCompletion}%\n")
        message.append("الاتجاه: ${analysis.trend}\n\n")
        message.append("💡 التوصيات:\n")

        analysis.recommendations.forEach { recommendation: String ->
            message.append("• $recommendation\n")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("تحليل نمط الصلاة")
            .setMessage(message.toString())
            .setPositiveButton("حسناً", null)
            .show()
    }
}
