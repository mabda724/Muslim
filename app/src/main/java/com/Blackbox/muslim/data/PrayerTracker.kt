package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class PrayerTracker(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_tracker", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val gamificationManager = GamificationManager(context)
    private val smartReminderManager = SmartReminderManager(context)

    data class DayStats(
        val date: String,
        val fajr: Boolean,
        val dhuhr: Boolean,
        val asr: Boolean,
        val maghrib: Boolean,
        val isha: Boolean
    ) {
        val prayedCount: Int get() = listOf(fajr, dhuhr, asr, maghrib, isha).count { it }
        val percentage: Int get() = (prayedCount * 100) / 5
    }

    fun markPrayed(prayerName: String, date: String = dateFormat.format(Date()), isOnTime: Boolean = true) {
        val key = "${date}_${prayerName.lowercase()}"
        prefs.edit().putBoolean(key, true).apply()

        // Add points
        val points = if (isOnTime) GamificationManager.POINTS_PRAYER_ON_TIME else GamificationManager.POINTS_PRAYER_LATE
        gamificationManager.addPoints(points)

        // Check for achievements
        val dayStats = getDayStats(date)
        val dayComplete = dayStats.percentage == 100
        val streak = getStreak()
        val totalPrayers = getTotalPrayersAllTime()

        gamificationManager.checkAchievementsAfterPrayer(
            prayerName,
            isOnTime,
            dayComplete,
            streak,
            totalPrayers
        )

        incrementTotalPrayers()
    }

    fun isPrayedToday(prayerName: String, date: String = dateFormat.format(Date())): Boolean {
        val key = "${date}_${prayerName.lowercase()}"
        return prefs.getBoolean(key, false)
    }

    fun getDayStats(date: String = dateFormat.format(Date())): DayStats {
        return DayStats(
            date = date,
            fajr = isPrayedToday("fajr", date),
            dhuhr = isPrayedToday("dhuhr", date),
            asr = isPrayedToday("asr", date),
            maghrib = isPrayedToday("maghrib", date),
            isha = isPrayedToday("isha", date)
        )
    }

    fun getWeekStats(): List<DayStats> {
        val cal = Calendar.getInstance()
        val stats = mutableListOf<DayStats>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            stats.add(getDayStats(dateFormat.format(dayCal.time)))
        }
        return stats
    }

    fun getMonthStats(): List<DayStats> {
        val stats = mutableListOf<DayStats>()
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

  for (day in 1..daysInMonth) {
    if (day > Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) continue
    val dayCal = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, day)
    }
    stats.add(getDayStats(dateFormat.format(dayCal.time)))
  }
        return stats
    }

    fun getStreak(): Int {
        var streak = 0
        val cal = Calendar.getInstance()

        while (true) {
            val date = dateFormat.format(cal.time)
            val stats = getDayStats(date)
            if (stats.percentage == 100) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getLongestStreak(): Int {
        return prefs.getInt("longest_streak", 0)
    }

    fun updateLongestStreak() {
        val currentStreak = getStreak()
        val longest = prefs.getInt("longest_streak", 0)
        if (currentStreak > longest) {
            prefs.edit().putInt("longest_streak", currentStreak).apply()
        }
    }

    fun getTotalPrayersThisMonth(): Int {
        return getMonthStats().sumOf { it.prayedCount }
    }

    fun getTotalPrayersAllTime(): Int {
        return prefs.getInt("total_prayers", 0)
    }

    fun incrementTotalPrayers() {
        val total = prefs.getInt("total_prayers", 0)
        prefs.edit().putInt("total_prayers", total + 1).apply()
    }

    fun getWeeklyAverage(): Int {
        val weekStats = getWeekStats()
        val totalPrayed = weekStats.sumOf { it.prayedCount }
        val totalPossible = weekStats.size * 5
        return if (totalPossible > 0) (totalPrayed * 100) / totalPossible else 0
    }

    fun getEncouragementMessage(): String {
        val today = getDayStats()
        val streak = getStreak()
        val weeklyAvg = getWeeklyAverage()

        return when {
            today.percentage == 100 && streak > 7 ->
                "ممتاز! أنت في سلسلة رائعة من $streak أيام مكتملة! سبحان الله! 🌟"
            today.percentage == 100 ->
                "أحسنت! أكملت اليوم بشكل كامل! الله يبارك فيك! ✨"
            today.percentage >= 60 ->
                "أداء رائع! لقد صليت ${today.prayedCount} من 5 صلوات اليوم. واصل! 💪"
            today.prayedCount > 0 ->
                "بالتوفيق! لقد صليت ${today.prayedCount} صلوات اليوم. حاول إكمال الباقي! 🤲"
            weeklyAvg >= 80 ->
                "متوسطك الأسبوعي $weeklyAvg% ممتاز! استمر في هذا الإيقاع! 🌙"
            streak >= 3 ->
                "سلسلة $streak أيام! لا تتوقف! الله معك! ❤️"
            else ->
                "ابدأ اليوم بصلاتك الخمس. الله يحب العبد الصبور! 🕌"
        }
    }

    fun getMotivationalQuote(): String {
        val quotes = listOf(
            "إن الله مع الصابرين",
            "وعلى الله فليتوكل المؤمنون",
            "لا يكلف الله نفساً إلا وسعها",
            "إن مع العسر يسراً",
            "قل هو الله أحد",
            "ادعوني استجب لكم",
            "ألا بذكر الله تطمئن القلوب",
            "ورفعنا لك ذكرك",
            "وإنك لعلى خُلُق عظيم",
            "وما أرسلناك إلا رحمة للعالمين",
            "فبأي آلاء ربكما تكذبان",
            "إن الله يأمر بالعدل والإحسان",
            "وتعافوا واغفروا",
            "إن الله يحب التوابين ويحب المتطهرين",
            "وقل ربي زدني علماً"
        )
        return quotes.random()
    }
}
