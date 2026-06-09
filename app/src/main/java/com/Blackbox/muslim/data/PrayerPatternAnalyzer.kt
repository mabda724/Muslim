package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class PrayerPatternAnalyzer(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("pattern_analysis", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class PatternAnalysis(
        val bestDay: String,
        val worstDay: String,
        val bestPrayer: String,
        val worstPrayer: String,
        val averageCompletion: Int,
        val trend: String,
        val recommendations: List<String>
    )

    fun analyzePrayerPatterns(prayerTracker: PrayerTracker): PatternAnalysis {
        val weekStats = prayerTracker.getWeekStats()
        val monthStats = prayerTracker.getMonthStats()

        // Find best and worst days
        val bestDay = findBestDay(weekStats)
        val worstDay = findWorstDay(weekStats)

        // Find best and worst prayers
        val bestPrayer = findBestPrayer(monthStats)
        val worstPrayer = findWorstPrayer(monthStats)

        // Calculate average completion
        val averageCompletion = calculateAverageCompletion(monthStats)

        // Determine trend
        val trend = determineTrend(weekStats)

        // Generate recommendations
        val recommendations = generateRecommendations(monthStats)

        return PatternAnalysis(
            bestDay = bestDay,
            worstDay = worstDay,
            bestPrayer = bestPrayer,
            worstPrayer = worstPrayer,
            averageCompletion = averageCompletion,
            trend = trend,
            recommendations = recommendations
        )
    }

    private fun findBestDay(stats: List<PrayerTracker.DayStats>): String {
        val best = stats.maxByOrNull { it.percentage }
        val sdf = SimpleDateFormat("EEE", Locale("ar"))
        return best?.let { sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)!!) } ?: "لا يوجد"
    }

    private fun findWorstDay(stats: List<PrayerTracker.DayStats>): String {
        val worst = stats.minByOrNull { it.percentage }
        val sdf = SimpleDateFormat("EEE", Locale("ar"))
        return worst?.let { sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)!!) } ?: "لا يوجد"
    }

    private fun findBestPrayer(stats: List<PrayerTracker.DayStats>): String {
        val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

        val prayerCompletion = prayerKeys.mapIndexed { index, key ->
            val completion = stats.count { stat ->
                when (key) {
                    "fajr" -> stat.fajr
                    "dhuhr" -> stat.dhuhr
                    "asr" -> stat.asr
                    "maghrib" -> stat.maghrib
                    "isha" -> stat.isha
                    else -> false
                }
            }
            prayerNames[index] to completion
        }

        return prayerCompletion.maxByOrNull { it.second }?.first ?: "لا يوجد"
    }

    private fun findWorstPrayer(stats: List<PrayerTracker.DayStats>): String {
        val prayerNames = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

        val prayerCompletion = prayerKeys.mapIndexed { index, key ->
            val completion = stats.count { stat ->
                when (key) {
                    "fajr" -> stat.fajr
                    "dhuhr" -> stat.dhuhr
                    "asr" -> stat.asr
                    "maghrib" -> stat.maghrib
                    "isha" -> stat.isha
                    else -> false
                }
            }
            prayerNames[index] to completion
        }

        return prayerCompletion.minByOrNull { it.second }?.first ?: "لا يوجد"
    }

    private fun calculateAverageCompletion(stats: List<PrayerTracker.DayStats>): Int {
        if (stats.isEmpty()) return 0

        val totalPrayed = stats.sumOf { it.prayedCount }
        val totalPossible = stats.size * 5

        return if (totalPossible > 0) (totalPrayed * 100) / totalPossible else 0
    }

    private fun determineTrend(stats: List<PrayerTracker.DayStats>): String {
        if (stats.size < 3) return "لا يوجد بيانات كافية"

        val firstHalf = stats.take(stats.size / 2)
        val secondHalf = stats.drop(stats.size / 2)

        val firstPercentage = calculateAverageCompletion(firstHalf)
        val secondPercentage = calculateAverageCompletion(secondHalf)

        val difference = secondPercentage - firstPercentage

        return when {
            difference >= 10 -> "تحسن ملحوظ 📈"
            difference >= 5 -> "تحسن طفيف ↗️"
            difference <= -10 -> "تراجع ملحوظ 📉"
            difference <= -5 -> "تراجع طفيف ↘️"
            else -> "مستقر ➡️"
        }
    }

    private fun generateRecommendations(stats: List<PrayerTracker.DayStats>): List<String> {
        val recommendations = mutableListOf<String>()

        val averageCompletion = calculateAverageCompletion(stats)
        val worstPrayer = findWorstPrayer(stats)

        when {
            averageCompletion < 50 -> {
                recommendations.add("حاول تحسين إجمالي صلواتك، ابدأ بالصلاة في وقتها")
                recommendations.add("ركز على صلاة $worstPrayer لأنها الأكثر فوتاً")
            }
            averageCompletion < 80 -> {
                recommendations.add("أداء جيد! يمكنك الوصول للكمال بقليل من الجهد")
                if (worstPrayer != "لا يوجد") {
                    recommendations.add("انتبه أكثر لصلاة $worstPrayer")
                }
            }
            else -> {
                recommendations.add("ممتاز! استمر في هذا الأداء الرائع")
                recommendations.add("حاول الحفاظ على سلسلة صلوات متتالية")
            }
        }

        // Day-specific recommendations
        val worstDay = findWorstDay(stats)
        if (worstDay != "لا يوجد") {
            recommendations.add("يوم $worstDay هو الأقل أداءً، حاول تحسينه")
        }

        return recommendations
    }

    fun savePatternAnalysis(analysis: PatternAnalysis) {
        val today = dateFormat.format(Date())
        val key = "pattern_analysis_$today"

        val json = """
            {
                "bestDay": "${analysis.bestDay}",
                "worstDay": "${analysis.worstDay}",
                "bestPrayer": "${analysis.bestPrayer}",
                "worstPrayer": "${analysis.worstPrayer}",
                "averageCompletion": ${analysis.averageCompletion},
                "trend": "${analysis.trend}"
            }
        """.trimIndent()

        prefs.edit().putString(key, json).apply()
    }

    fun getRecentPatternAnalysis(): PatternAnalysis? {
        val today = dateFormat.format(Date())
        val key = "pattern_analysis_$today"
        val json = prefs.getString(key, null) ?: return null

        // Simple parsing (in production, use proper JSON parser)
        return try {
            PatternAnalysis(
                bestDay = extractField(json, "bestDay"),
                worstDay = extractField(json, "worstDay"),
                bestPrayer = extractField(json, "bestPrayer"),
                worstPrayer = extractField(json, "worstPrayer"),
                averageCompletion = extractIntField(json, "averageCompletion"),
                trend = extractField(json, "trend"),
                recommendations = emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractField(json: String, fieldName: String): String {
        val pattern = "\"$fieldName\":\"(.*?)\"".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun extractIntField(json: String, fieldName: String): Int {
        val pattern = "\"$fieldName\":(\\d+)".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
}
