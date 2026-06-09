package com.Blackbox.muslim.ui

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.GamificationManager
import com.Blackbox.muslim.data.SmartReminderManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var radioGroupThemes: RadioGroup
    private lateinit var btnApplyTheme: Button
    private lateinit var btnBackSettings: Button
    private lateinit var btnViewAchievements: Button
    private lateinit var switchSmartReminders: SwitchMaterial
    private lateinit var btnSmartInsights: Button

    private val themeButtons = mutableMapOf<String, RadioButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupUI()
        loadCurrentTheme()
    }

    private fun setupUI() {
        radioGroupThemes = findViewById<RadioGroup>(R.id.radioGroupThemes)
        btnApplyTheme = findViewById<Button>(R.id.btnApplyTheme)
        btnBackSettings = findViewById<Button>(R.id.btnBackSettings)
        btnViewAchievements = findViewById<Button>(R.id.btnViewAchievements)
        switchSmartReminders = findViewById<SwitchMaterial>(R.id.switchSmartReminders)
        btnSmartInsights = findViewById<Button>(R.id.btnSmartInsights)

        // Create theme radio buttons
        createThemeButton(AppPreferences.THEME_DEFAULT, R.id.radioDefault, "الوضع الافتراضي (أزرق داكن)")
        createThemeButton(AppPreferences.THEME_LIGHT, R.id.radioLight, "الوضع الفاتح")
        createThemeButton(AppPreferences.THEME_GREEN, R.id.radioGreen, "الإسلامي الأخضر")
        createThemeButton(AppPreferences.THEME_BLUE, R.id.radioBlue, "الأزرق الهادئ")
        createThemeButton(AppPreferences.THEME_BROWN, R.id.radioBrown, "البني الترابي")
        createThemeButton(AppPreferences.THEME_PURPLE, R.id.radioPurple, "الأرجواني العصري")

        btnApplyTheme.setOnClickListener {
            applySelectedTheme()
        }

        btnBackSettings.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in_up, R.anim.fade_out)
        }

        btnViewAchievements.setOnClickListener {
            showAchievementsDialog()
        }

        // Smart reminders setup
        switchSmartReminders.isChecked = preferences.getSmartRemindersEnabled()

        switchSmartReminders.setOnCheckedChangeListener { _, isChecked ->
            preferences.setSmartRemindersEnabled(isChecked)
        }

        btnSmartInsights.setOnClickListener {
            showSmartInsights()
        }
    }

    private fun showSmartInsights() {
        val smartReminderManager = SmartReminderManager(this)
        val optimalSettings = smartReminderManager.getOptimalReminderSettings()

        val message = StringBuilder()
        message.append("التوصيات الذكية:\n\n")
        message.append("⏰ وقت التذكير المقترح: ${optimalSettings.recommendedMinutesBefore} دقيقة قبل الأذان\n")
        message.append("📱 أفضل وقت للإشعارات: ${optimalSettings.recommendedNotificationHour}:00\n\n")
        message.append("رؤى أدائك:\n")

        optimalSettings.insights.forEach { insight ->
            message.append("• $insight\n")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("رؤى ذكية")
            .setMessage(message.toString())
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun showAchievementsDialog() {
        val gamificationManager = GamificationManager(this)
        val unlockedAchievements = gamificationManager.getUnlockedAchievements()
        val totalAchievements = GamificationManager.Achievement.ACHIEVEMENTS.size

        val message = StringBuilder()
        message.append("إنجازاتك: $unlockedAchievements.size / $totalAchievements\n\n")

        GamificationManager.Achievement.ACHIEVEMENTS.forEach { achievement ->
            val isUnlocked = achievement.id in unlockedAchievements
            val status = if (isUnlocked) "✓" else "✗"
            val progress = gamificationManager.getAchievementProgress(achievement.id)
            message.append("$status ${achievement.icon} ${achievement.title}\n")
            message.append("   ${achievement.description} (+${achievement.points} نقطة)\n")
            if (!isUnlocked && progress > 0) {
                message.append("   التقدم: $progress%\n")
            }
            message.append("\n")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("إنجازاتك")
            .setMessage(message.toString())
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun createThemeButton(themeKey: String, radioButtonId: Int, label: String) {
        val radioButton = findViewById<RadioButton>(radioButtonId)
        radioButton.text = label
        radioButton.tag = themeKey
        themeButtons[themeKey] = radioButton
    }

    private fun loadCurrentTheme() {
        val currentTheme = preferences.getTheme()
        themeButtons[currentTheme]?.isChecked = true
    }

    private fun applySelectedTheme() {
        val selectedRadioButtonId = radioGroupThemes.checkedRadioButtonId
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "الرجاء اختيار سمة", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val selectedTheme = selectedRadioButton.tag as? String

        if (selectedTheme != null) {
            preferences.setTheme(selectedTheme)

            // Restart activity to apply theme
            recreate()

            Toast.makeText(this, "تم تطبيق السمة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    override fun recreate() {
        // Apply theme before recreation
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))
        super.recreate()
    }
}
