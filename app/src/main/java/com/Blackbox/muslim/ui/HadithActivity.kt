package com.Blackbox.muslim.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.HadithData

class HadithActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var hadithData: HadithData
    private var currentHadithIndex = 0
    private var currentCategory = "daily" // daily, random, favorite
    private var currentFilterCategory: String? = null
    private var hadithList: List<HadithData.HadithItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hadith)

        hadithData = HadithData(this)

        setupUI()
        loadHadith()
    }

    private fun setupUI() {
        val btnBack = findViewById<Button>(R.id.btnBackHadith)
        val btnNext = findViewById<Button>(R.id.btnNextHadith)
        val btnPrevious = findViewById<Button>(R.id.btnPreviousHadith)
        val btnFavorite = findViewById<Button>(R.id.btnFavoriteHadith)
        val btnRandom = findViewById<Button>(R.id.btnRandomHadith)
        val btnDaily = findViewById<Button>(R.id.btnDailyHadith)
        val btnFavorites = findViewById<Button>(R.id.btnFavoritesHadith)
        val btnShare = findViewById<Button>(R.id.btnShareHadith)

        val tvHadithArabic = findViewById<TextView>(R.id.tvHadithArabic)
        val tvHadithEnglish = findViewById<TextView>(R.id.tvHadithEnglish)
        val tvSource = findViewById<TextView>(R.id.tvSource)
        val tvGrade = findViewById<TextView>(R.id.tvGrade)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val tvCounter = findViewById<TextView>(R.id.tvHadithCounter)

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out)
        }

        btnNext.setOnClickListener {
            if (currentHadithIndex < hadithList.size - 1) {
                currentHadithIndex++
                displayCurrentHadith()
            }
        }

        btnPrevious.setOnClickListener {
            if (currentHadithIndex > 0) {
                currentHadithIndex--
                displayCurrentHadith()
            }
        }

        btnFavorite.setOnClickListener {
            val currentHadith = hadithList.getOrNull(currentHadithIndex)
            if (currentHadith != null) {
                if (hadithData.isFavorite(currentHadith.id)) {
                    hadithData.removeFavoriteHadith(currentHadith)
                    btnFavorite.text = "☆"
                    Toast.makeText(this, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show()
                } else {
                    hadithData.saveFavoriteHadith(currentHadith)
                    btnFavorite.text = "★"
                    Toast.makeText(this, "تمت الإضافة إلى المفضلة", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRandom.setOnClickListener {
            currentCategory = "random"
            currentHadithIndex = 0
            hadithList = listOf(hadithData.getRandomHadith())
            displayCurrentHadith()
        }

        btnDaily.setOnClickListener {
            currentCategory = "daily"
            currentHadithIndex = 0
            hadithList = listOf(hadithData.getDailyHadith())
            displayCurrentHadith()
        }

        btnFavorites.setOnClickListener {
            val favoriteIds = hadithData.getFavoriteHadiths()
            if (favoriteIds.isEmpty()) {
                Toast.makeText(this, "لا توجد أحاديث في المفضلة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentCategory = "favorite"
            currentHadithIndex = 0
            hadithList = com.Blackbox.muslim.data.HadithData.localHadiths.filter { favoriteIds.contains(it.id) }
            displayCurrentHadith()
        }

        btnShare.setOnClickListener {
            val currentHadith = hadithList.getOrNull(currentHadithIndex)
            if (currentHadith != null) {
                shareHadith(currentHadith)
            }
        }
    }

    private fun loadHadith() {
        currentCategory = "daily"
        hadithList = listOf(hadithData.getDailyHadith())
        displayCurrentHadith()
    }

    private fun displayCurrentHadith() {
        val currentHadith = hadithList.getOrNull(currentHadithIndex)
        if (currentHadith == null) {
            Toast.makeText(this, "لا يوجد أحاديث", Toast.LENGTH_SHORT).show()
            return
        }

        val tvHadithArabic = findViewById<TextView>(R.id.tvHadithArabic)
        val tvHadithEnglish = findViewById<TextView>(R.id.tvHadithEnglish)
        val tvSource = findViewById<TextView>(R.id.tvSource)
        val tvGrade = findViewById<TextView>(R.id.tvGrade)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val tvCounter = findViewById<TextView>(R.id.tvHadithCounter)
        val btnFavorite = findViewById<Button>(R.id.btnFavoriteHadith)
        val btnPrevious = findViewById<Button>(R.id.btnPreviousHadith)
        val btnNext = findViewById<Button>(R.id.btnNextHadith)

        tvHadithArabic.text = currentHadith.arabic
        tvHadithEnglish.text = currentHadith.english
        tvSource.text = "المصدر: ${currentHadith.source}"
        tvGrade.text = "الدرجة: ${currentHadith.grade}"
        tvCategory.text = "التصنيف: ${currentHadith.category}"
        tvCounter.text = "${currentHadithIndex + 1} / ${hadithList.size}"

        // Update favorite button
        btnFavorite.text = if (hadithData.isFavorite(currentHadith.id)) "★" else "☆"

        // Enable/disable navigation buttons
        btnPrevious.isEnabled = currentHadithIndex > 0
        btnNext.isEnabled = currentHadithIndex < hadithList.size - 1

        // Animate the hadith
        animateCard(tvHadithArabic)
    }

    private fun animateCard(view: View) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun shareHadith(hadith: HadithData.HadithItem) {
        val shareText = """
            |حديث نبوي شريف
            |${hadith.arabic}
            |
            |${hadith.english}
            |
            |المصدر: ${hadith.source}
            |الدرجة: ${hadith.grade}
        """.trimMargin()

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة الحديث"))
    }
}
