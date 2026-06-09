package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class GamificationManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gamification", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Points System
    companion object {
        const val POINTS_PRAYER_ON_TIME = 10
        const val POINTS_PRAYER_LATE = 5
        const val POINTS_PRAYER_MISSED = 0
        const val POINTS_DAY_COMPLETE = 50
        const val POINTS_STREAK_BONUS = 20
    }

    data class Level(
        val level: Int,
        val minPoints: Int,
        val maxPoints: Int,
        val title: String
    ) {
        companion object {
            val LEVELS = listOf(
                Level(1, 0, 100, "مبتدئ"),
                Level(2, 101, 300, "متمسك"),
                Level(3, 301, 600, "محافظ"),
                Level(4, 601, 1000, "مثابر"),
                Level(5, 1001, 1500, "مجتهد"),
                Level(6, 1501, 2500, "متفوق"),
                Level(7, 2501, 4000, "متميز"),
                Level(8, 4001, 6000, "بارع"),
                Level(9, 6001, 10000, "خبير"),
                Level(10, 10001, Int.MAX_VALUE, "أسطورة")
            )

            fun getLevel(points: Int): Level {
                return LEVELS.lastOrNull { points >= it.minPoints } ?: LEVELS[0]
            }
        }
    }

    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val points: Int,
        val icon: String
    ) {
        companion object {
            val ACHIEVEMENTS = listOf(
                Achievement("first_prayer", "بداية الخير", "أول صلاة تؤديها في وقتها", 10, "🌱"),
                Achievement("day_complete", "يوم كامل", "أكملت جميع صلوات اليوم", 50, "⭐"),
                Achievement("week_streak", "أسبوع متتالي", "أكملت أسبوعاً كاملاً من الصلوات", 100, "🔥"),
                Achievement("month_streak", "شهر متتالي", "أكملت شهراً كاملاً من الصلوات", 500, "🏆"),
                Achievement("100_prayers", "مائة صلاة", "أديت 100 صلاة في وقتها", 100, "💯"),
                Achievement("500_prayers", "خمسمائة صلاة", "أديت 500 صلاة في وقتها", 500, "🌟"),
                Achievement("1000_prayers", "ألف صلاة", "أديت 1000 صلاة في وقتها", 1000, "👑"),
                Achievement("early_bird", "البدار", "صليت الفجر في وقته 7 أيام متتالية", 50, "🐦"),
                Achievement("night_owl", "المحافظ", "صليت العشاء في وقته 7 أيام متتالية", 50, "🦉"),
                Achievement("perfect_week", "أسبوع مثالي", "100% صلوات الأسبوع في وقتها", 200, "💎")
            )
        }
    }

    // Points Management
    fun getTotalPoints(): Int {
        return prefs.getInt("total_points", 0)
    }

    fun addPoints(points: Int) {
        val current = getTotalPoints()
        prefs.edit().putInt("total_points", current + points).apply()
    }

    fun getCurrentLevel(): Level {
        return Level.getLevel(getTotalPoints())
    }

    fun getLevelProgress(): Float {
        val currentLevel = getCurrentLevel()
        val levelIndex = Level.LEVELS.indexOf(currentLevel)
        if (levelIndex >= Level.LEVELS.size - 1) return 1.0f

        val nextLevel = Level.LEVELS[levelIndex + 1]
        val currentPoints = getTotalPoints()
        val pointsInCurrentLevel = currentPoints - currentLevel.minPoints
        val pointsNeededForNextLevel = nextLevel.minPoints - currentLevel.minPoints

        return if (pointsNeededForNextLevel > 0) {
            pointsInCurrentLevel.toFloat() / pointsNeededForNextLevel
        } else {
            1.0f
        }
    }

    // Achievements Management
    fun unlockAchievement(achievementId: String): Boolean {
        val unlockedAchievements = getUnlockedAchievements()
        if (achievementId in unlockedAchievements) return false

        val newUnlocked = unlockedAchievements.toMutableSet()
        newUnlocked.add(achievementId)
        prefs.edit().putStringSet("unlocked_achievements", newUnlocked).apply()

        val achievement = Achievement.ACHIEVEMENTS.find { it.id == achievementId }
        achievement?.let {
            addPoints(it.points)
        }

        return true
    }

    fun getUnlockedAchievements(): Set<String> {
        return prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
    }

    fun isAchievementUnlocked(achievementId: String): Boolean {
        return achievementId in getUnlockedAchievements()
    }

    fun getAchievementProgress(achievementId: String): Int {
        return when (achievementId) {
            "first_prayer" -> if (isAchievementUnlocked(achievementId)) 1 else 0
            "day_complete" -> if (isAchievementUnlocked(achievementId)) 1 else 0
            "week_streak" -> getStreak()
            "month_streak" -> if (getStreak() >= 30) 1 else getStreak() / 30
            "100_prayers" -> getTotalPoints() / 10
            "500_prayers" -> getTotalPoints() / 50
            "1000_prayers" -> getTotalPoints() / 100
            "early_bird" -> getFajrStreak()
            "night_owl" -> getIshaStreak()
            "perfect_week" -> if (isAchievementUnlocked(achievementId)) 1 else 0
            else -> 0
        }
    }

    // Stats
    fun getStreak(): Int {
        return prefs.getInt("current_streak", 0)
    }

    fun setStreak(streak: Int) {
        prefs.edit().putInt("current_streak", streak).apply()
    }

    fun getLongestStreak(): Int {
        return prefs.getInt("longest_streak", 0)
    }

    fun setLongestStreak(streak: Int) {
        val currentLongest = getLongestStreak()
        if (streak > currentLongest) {
            prefs.edit().putInt("longest_streak", streak).apply()
        }
    }

    fun getFajrStreak(): Int {
        return prefs.getInt("fajr_streak", 0)
    }

    fun setFajrStreak(streak: Int) {
        prefs.edit().putInt("fajr_streak", streak).apply()
    }

    fun getIshaStreak(): Int {
        return prefs.getInt("isha_streak", 0)
    }

    fun setIshaStreak(streak: Int) {
        prefs.edit().putInt("isha_streak", streak).apply()
    }

    // Check for new achievements after prayer
    fun checkAchievementsAfterPrayer(
        prayerName: String,
        isOnTime: Boolean,
        dayComplete: Boolean,
        streak: Int,
        totalPrayers: Int
    ): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()

        if (isOnTime && !isAchievementUnlocked("first_prayer")) {
            if (unlockAchievement("first_prayer")) {
                Achievement.ACHIEVEMENTS.find { it.id == "first_prayer" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (dayComplete && !isAchievementUnlocked("day_complete")) {
            if (unlockAchievement("day_complete")) {
                Achievement.ACHIEVEMENTS.find { it.id == "day_complete" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (streak >= 7 && !isAchievementUnlocked("week_streak")) {
            if (unlockAchievement("week_streak")) {
                Achievement.ACHIEVEMENTS.find { it.id == "week_streak" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (streak >= 30 && !isAchievementUnlocked("month_streak")) {
            if (unlockAchievement("month_streak")) {
                Achievement.ACHIEVEMENTS.find { it.id == "month_streak" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (totalPrayers >= 100 && !isAchievementUnlocked("100_prayers")) {
            if (unlockAchievement("100_prayers")) {
                Achievement.ACHIEVEMENTS.find { it.id == "100_prayers" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (totalPrayers >= 500 && !isAchievementUnlocked("500_prayers")) {
            if (unlockAchievement("500_prayers")) {
                Achievement.ACHIEVEMENTS.find { it.id == "500_prayers" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (totalPrayers >= 1000 && !isAchievementUnlocked("1000_prayers")) {
            if (unlockAchievement("1000_prayers")) {
                Achievement.ACHIEVEMENTS.find { it.id == "1000_prayers" }?.let {
                    newAchievements.add(it)
                }
            }
        }

        if (prayerName == "fajr" && isOnTime) {
            val fajrStreak = getFajrStreak() + 1
            setFajrStreak(fajrStreak)
            if (fajrStreak >= 7 && !isAchievementUnlocked("early_bird")) {
                if (unlockAchievement("early_bird")) {
                    Achievement.ACHIEVEMENTS.find { it.id == "early_bird" }?.let {
                        newAchievements.add(it)
                    }
                }
            }
        }

        if (prayerName == "isha" && isOnTime) {
            val ishaStreak = getIshaStreak() + 1
            setIshaStreak(ishaStreak)
            if (ishaStreak >= 7 && !isAchievementUnlocked("night_owl")) {
                if (unlockAchievement("night_owl")) {
                    Achievement.ACHIEVEMENTS.find { it.id == "night_owl" }?.let {
                        newAchievements.add(it)
                    }
                }
            }
        }

        return newAchievements
    }
}
