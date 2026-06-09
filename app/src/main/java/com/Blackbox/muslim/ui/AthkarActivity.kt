package com.Blackbox.muslim.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.AthkarData

class AthkarActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private var currentCategory = "morning"
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_athkar)

        val tvCategoryTitle = findViewById<TextView>(R.id.tvCategoryTitle)
        val tvThikrArabic = findViewById<TextView>(R.id.tvThikrArabic)
        val tvThikrTranslation = findViewById<TextView>(R.id.tvThikrTranslation)
        val tvCounter = findViewById<TextView>(R.id.tvCounter)
        val tvCounterTotal = findViewById<TextView>(R.id.tvCounterTotal)
        val btnNext = findViewById<Button>(R.id.btnNextThikr)
        val btnPrev = findViewById<Button>(R.id.btnPrevThikr)
        val btnRepeat = findViewById<Button>(R.id.btnRepeatThikr)
        val btnMorning = findViewById<Button>(R.id.btnMorningAthkar)
        val btnEvening = findViewById<Button>(R.id.btnEveningAthkar)
        val btnSleep = findViewById<Button>(R.id.btnSleepAthkar)
        val btnWakeup = findViewById<Button>(R.id.btnWakeupAthkar)

        var counter = 0

        fun loadThikr() {
            val athkar = when (currentCategory) {
                "morning" -> AthkarData.morningAthkar
                "evening" -> AthkarData.eveningAthkar
                "sleep" -> AthkarData.sleepAthkar
                "wakeup" -> AthkarData.wakeupAthkar
                else -> AthkarData.morningAthkar
            }

            if (currentIndex < athkar.size) {
                val thikr = athkar[currentIndex]
                tvCategoryTitle.text = when (currentCategory) {
                    "morning" -> "أذكار الصباح"
                    "evening" -> "أذكار المساء"
                    "sleep" -> "أذكار النوم"
                    "wakeup" -> "أذكار الاستيقاظ"
                    else -> "الأذكار"
                }
                tvThikrArabic.text = thikr.arabic
                tvThikrTranslation.text = thikr.translation
                counter = 0
                tvCounter.text = "$counter / ${thikr.count}"
                tvCounterTotal.text = "${currentIndex + 1} / ${athkar.size}"
            }
        }

        loadThikr()

        btnRepeat.setOnClickListener {
            val athkar = when (currentCategory) {
                "morning" -> AthkarData.morningAthkar
                "evening" -> AthkarData.eveningAthkar
                "sleep" -> AthkarData.sleepAthkar
                "wakeup" -> AthkarData.wakeupAthkar
                else -> AthkarData.morningAthkar
            }
            if (currentIndex < athkar.size) {
                counter++
                tvCounter.text = "$counter / ${athkar[currentIndex].count}"
                if (counter >= athkar[currentIndex].count) {
                    Toast.makeText(this, "تم ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnNext.setOnClickListener {
            val athkar = when (currentCategory) {
                "morning" -> AthkarData.morningAthkar
                "evening" -> AthkarData.eveningAthkar
                "sleep" -> AthkarData.sleepAthkar
                "wakeup" -> AthkarData.wakeupAthkar
                else -> AthkarData.morningAthkar
            }
            if (currentIndex < athkar.size - 1) {
                currentIndex++
                loadThikr()
            }
        }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadThikr()
            }
        }

        btnMorning.setOnClickListener {
            currentCategory = "morning"
            currentIndex = 0
            loadThikr()
        }

        btnEvening.setOnClickListener {
            currentCategory = "evening"
            currentIndex = 0
            loadThikr()
        }

        btnSleep.setOnClickListener {
            currentCategory = "sleep"
            currentIndex = 0
            loadThikr()
        }

        btnWakeup.setOnClickListener {
            currentCategory = "wakeup"
            currentIndex = 0
            loadThikr()
        }
    }
}
