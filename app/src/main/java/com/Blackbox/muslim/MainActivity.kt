package com.Blackbox.muslim

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Blackbox.muslim.data.AppPreferences
import com.Blackbox.muslim.data.AthkarData
import com.Blackbox.muslim.data.PrayerTracker
import com.Blackbox.muslim.shared.PrayerManager
import com.Blackbox.muslim.ui.*
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var prayerManager: PrayerManager
    private lateinit var notificationHelper: PrayerNotificationHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var adhanPlayer: AdhanPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var pendingAction: (() -> Unit)? = null

    private val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    private val prayerNamesArabic = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")

    private val pinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingAction?.invoke()
            pendingAction = null
        }
    }

    private val appsListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updateUI()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 1000)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_main)
        prayerManager = PrayerManager()
        notificationHelper = PrayerNotificationHelper(this)
        notificationHelper.createNotificationChannel()
        locationHelper = LocationHelper(this)
        adhanPlayer = AdhanPlayer(this)

        startGlowAnimation()
        setupUI()
        loadSettings()
        updateQuote()
        animateCards()
        handler.post(updateRunnable)

        try {
            val app = application as MuslimApp
            app.showDuaNotification()
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        adhanPlayer.release()
    }

    private fun startGlowAnimation() {
        val glowTop = findViewById<View>(R.id.glowTop) ?: return
        val glowBottom = findViewById<View>(R.id.glowBottom) ?: return

        ValueAnimator.ofFloat(0.4f, 0.8f, 0.4f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { glowTop.alpha = it.animatedValue as Float }
        }.start()

        ValueAnimator.ofFloat(0.2f, 0.6f, 0.2f).apply {
            duration = 5000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { glowBottom.alpha = it.animatedValue as Float }
        }.start()
    }

    private fun animateCards() {
        val cards = listOf(
            R.id.tvNextPrayerNameArabic, R.id.tvQuote, R.id.tvQuoteTranslation,
            R.id.btnAthkar, R.id.btnHadith, R.id.btnBlockedApps, R.id.testModeCard
        )
        cards.forEachIndexed { index, id ->
            val view = findViewById<View>(id) ?: return@forEachIndexed
            val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
            anim.startOffset = (index * 120).toLong()
            view.startAnimation(anim)
        }
    }

    private fun isPrayerMarkedToday(prayerKey: String): Boolean {
        val prefs = getSharedPreferences("prayer_tracking", MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(System.currentTimeMillis())
        return prefs.getBoolean("${today}_${prayerKey}", false)
    }

    private fun setPrayerMarkedToday(prayerKey: String, marked: Boolean) {
        val prefs = getSharedPreferences("prayer_tracking", MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(System.currentTimeMillis())
        prefs.edit().putBoolean("${today}_${prayerKey}", marked).apply()
    }

    private fun getTodayMarkedCount(): Int {
        return prayerNames.count { isPrayerMarkedToday(it) }
    }

    private fun authenticate(action: () -> Unit) {
        if (!preferences.isAuthRequired()) {
            action()
            return
        }

        val biometricHelper = BiometricHelper(this)
        if (preferences.useBiometric() && biometricHelper.isBiometricAvailable()) {
            pendingAction = action
            biometricHelper.showBiometricPrompt(
                callback = object : BiometricHelper.AuthCallback {
                    override fun onAuthSuccess() { action() }
                    override fun onAuthFailed() { Toast.makeText(this@MainActivity, "فشل التحقق", Toast.LENGTH_SHORT).show() }
                    override fun onAuthError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != 5) {
                            Toast.makeText(this@MainActivity, "خطأ: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        } else if (preferences.getPinCode().isNotEmpty()) {
            pendingAction = action
            val intent = Intent(this, PinCodeActivity::class.java)
            pinLauncher.launch(intent)
        } else {
            action()
        }
    }

    private fun setupUI() {
        val switchBlocking = findViewById<Switch>(R.id.switchBlocking)
        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        val switchTestMode = findViewById<Switch>(R.id.switchTestMode)
        val seekBarWindow = findViewById<SeekBar>(R.id.seekBarWindow)
        val tvWindowMinutes = findViewById<TextView>(R.id.tvWindowMinutes)
        val btnEnableAccessibility = findViewById<Button>(R.id.btnEnableAccessibility)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnAthkar = findViewById<Button>(R.id.btnAthkar)
        val btnHadith = findViewById<Button>(R.id.btnHadith)
        val btnBlockedApps = findViewById<Button>(R.id.btnBlockedApps)
        val tvLocationName = findViewById<TextView>(R.id.tvLocationName)
        val testModeControls = findViewById<LinearLayout>(R.id.testModeControls)
        val spinnerTestPrayer = findViewById<Spinner>(R.id.spinnerTestPrayer)
        val timePickerTest = findViewById<TimePicker>(R.id.timePickerTest)
        val btnApplyTestTime = findViewById<Button>(R.id.btnApplyTestTime)
        val switchAuth = findViewById<Switch>(R.id.switchAuthRequired)
        val tvQuote = findViewById<TextView>(R.id.tvQuote)
        val tvQuoteTranslation = findViewById<TextView>(R.id.tvQuoteTranslation)

        switchBlocking.isChecked = preferences.isBlockingEnabled()
        switchNotifications.isChecked = preferences.isNotificationsEnabled()
        switchTestMode.isChecked = preferences.isTestModeEnabled()
        switchAuth.isChecked = preferences.isAuthRequired()
        seekBarWindow.progress = preferences.getPrayerWindowMinutes()
        tvWindowMinutes.text = "${preferences.getPrayerWindowMinutes()} دقيقة"
        tvLocationName.text = preferences.getLocationName().ifEmpty { "جاري تحديد الموقع..." }

        if (preferences.isTestModeEnabled()) testModeControls.visibility = View.VISIBLE

        // Test Prayer Spinner
        val testPrayers = listOf("Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر", "Maghrib" to "المغرب", "Isha" to "العشاء")
        spinnerTestPrayer.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, testPrayers.map { it.second })
        val currentTestIndex = testPrayers.indexOfFirst { it.first == preferences.getTestPrayerName() }
        if (currentTestIndex >= 0) spinnerTestPrayer.setSelection(currentTestIndex)

        timePickerTest.setIs24HourView(true)
        timePickerTest.hour = preferences.getTestPrayerHour()
        timePickerTest.minute = preferences.getTestPrayerMinute()

        // Listeners
        switchBlocking.setOnCheckedChangeListener { _, isChecked ->
            authenticate {
                preferences.setBlockingEnabled(isChecked)
                updateUI()
            }
            if (!isChecked) switchBlocking.isChecked = true
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            authenticate { preferences.setNotificationsEnabled(isChecked) }
            if (!isChecked) switchNotifications.isChecked = true
        }

        switchTestMode.setOnCheckedChangeListener { _, isChecked ->
            authenticate {
                preferences.setTestModeEnabled(isChecked)
                testModeControls.visibility = if (isChecked) View.VISIBLE else View.GONE
                updateUI()
            }
            if (!isChecked) switchTestMode.isChecked = true
        }

        switchAuth.setOnCheckedChangeListener { _, isChecked ->
            authenticate { preferences.setAuthRequired(isChecked) }
            if (!isChecked) switchAuth.isChecked = true
        }

        seekBarWindow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress.coerceAtLeast(5)
                tvWindowMinutes.text = "$minutes دقيقة"
                if (fromUser) {
                    authenticate { preferences.setPrayerWindowMinutes(minutes) }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnEnableAccessibility.setOnClickListener {
            authenticate { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, com.Blackbox.muslim.ui.SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        btnAthkar.setOnClickListener {
            val intent = Intent(this, AthkarActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        btnHadith.setOnClickListener {
            val intent = Intent(this, com.Blackbox.muslim.ui.HadithActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        }

        btnBlockedApps.setOnClickListener {
            authenticate {
                val intent = Intent(this, AppsListActivity::class.java)
                intent.putExtra("mode", "blocked")
                appsListLauncher.launch(intent)
            }
        }

        val btnDonate = findViewById<Button>(R.id.btnDonate)
        btnDonate.setOnClickListener {
            startActivity(Intent(this, DonateActivity::class.java))
        }

        spinnerTestPrayer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferences.setTestPrayerName(testPrayers[position].first)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnApplyTestTime.setOnClickListener {
            authenticate {
                preferences.setTestPrayerHour(timePickerTest.hour)
                preferences.setTestPrayerMinute(timePickerTest.minute)
                Toast.makeText(this, "تم تطبيق وقت التجربة", Toast.LENGTH_SHORT).show()
                updateUI()
            }
        }

        val btnTestAdhan = findViewById<Button>(R.id.btnTestAdhan)
        btnTestAdhan.setOnClickListener {
            val testPrayerName = preferences.getTestPrayerName()
            val testPrayerArabic = when (testPrayerName) {
                "Fajr" -> "الفجر"
                "Dhuhr" -> "الظهر"
                "Asr" -> "العصر"
                "Maghrib" -> "المغرب"
                "Isha" -> "العشاء"
                else -> "الصلاة"
            }
            Toast.makeText(this, "اختبار: أذان $testPrayerArabic", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, com.Blackbox.muslim.ui.AdhanAlarmActivity::class.java).apply {
                putExtra("prayer_name", testPrayerName)
                putExtra("prayer_name_arabic", testPrayerArabic)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        // Load location on first run
        if (preferences.getLocationName().isEmpty()) {
            getCurrentLocation()
        }
    }

    private fun loadSettings() {
        val tvLocationName = findViewById<TextView>(R.id.tvLocationName)
        tvLocationName.text = preferences.getLocationName().ifEmpty { "جاري تحديد الموقع..." }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            return
        }

        locationHelper.getCurrentLocation { result ->
            runOnUiThread {
                if (result != null) {
                    preferences.setLatitude(result.latitude)
                    preferences.setLongitude(result.longitude)
                    val locationName = "${result.cityName}, ${result.countryName}".trim { it == ',' || it == ' ' }
                    preferences.setLocationName(locationName)
                    findViewById<TextView>(R.id.tvLocationName).text = locationName
                    loadSettings()
                    updateUI()
                    Toast.makeText(this, "تم تحديد الموقع: $locationName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "لم يتم العثور على الموقع", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    private fun getTestOverrides(): Map<String, Date>? {
        if (!preferences.isTestModeEnabled()) return null
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, preferences.getTestPrayerHour())
            set(Calendar.MINUTE, preferences.getTestPrayerMinute())
            set(Calendar.SECOND, 0)
        }
        return mapOf(preferences.getTestPrayerName() to cal.time)
    }

    private fun updateQuote() {
        val tvQuote = findViewById<TextView>(R.id.tvQuote) ?: return
        val tvQuoteTranslation = findViewById<TextView>(R.id.tvQuoteTranslation) ?: return
        val quote = AthkarData.quotes.random()
        val parts = quote.split(" - ")
        tvQuote.text = parts[0]
        tvQuoteTranslation.text = if (parts.size > 1) parts[1] else ""
    }

    private fun updateUI() {
        val lat = preferences.getLatitude()
        val lng = preferences.getLongitude()
        val method = preferences.getCalculationMethod()
        val window = preferences.getPrayerWindowMinutes()
        val testOverrides = getTestOverrides()

        val enabledPrayers = mutableSetOf<String>()
        if (preferences.isFajrEnabled()) enabledPrayers.add("Fajr")
        if (preferences.isDhuhrEnabled()) enabledPrayers.add("Dhuhr")
        if (preferences.isAsrEnabled()) enabledPrayers.add("Asr")
        if (preferences.isMaghribEnabled()) enabledPrayers.add("Maghrib")
        if (preferences.isIshaEnabled()) enabledPrayers.add("Isha")

        // Hero Card
        val tvNextPrayerName = findViewById<TextView>(R.id.tvNextPrayerName) ?: return
        val tvNextPrayerTime = findViewById<TextView>(R.id.tvNextPrayerTime)
        val tvNextPrayerNameArabic = findViewById<TextView>(R.id.tvNextPrayerNameArabic)
        val tvCountdown = findViewById<TextView>(R.id.tvCountdown)
        val tvBlockingWindow = findViewById<TextView>(R.id.tvBlockingWindow)

        val nextPrayer = prayerManager.getNextPrayerTime(lat, lng, method, testOverrides)
        if (nextPrayer != null) {
            tvNextPrayerName.text = nextPrayer.name
            tvNextPrayerTime.text = prayerManager.formatTime(nextPrayer.time)
            tvNextPrayerNameArabic.text = nextPrayer.nameArabic

            val remainingMillis = nextPrayer.time.time - System.currentTimeMillis()
            if (remainingMillis > 0) {
                tvCountdown.text = "متبقي ${prayerManager.formatCountdown(remainingMillis)}"
                tvCountdown.setTextColor(getColor(R.color.teal_400))
            } else {
                tvCountdown.text = "وقت الصلاة"
                tvCountdown.setTextColor(getColor(R.color.gold_400))
            }
            tvBlockingWindow.text = "حظر لمدة $window دقيقة"
        }

        // Status
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val statusDot = findViewById<View>(R.id.statusDot)
        val isPrayerTime = prayerManager.isCurrentlyPrayerTime(lat, lng, window, method, enabledPrayers, testOverrides)

        if (isPrayerTime && preferences.isBlockingEnabled()) {
            val remaining = prayerManager.getRemainingPrayerMinutes(lat, lng, window, method, enabledPrayers, testOverrides)
            tvStatus.text = "حظر نشط - $remaining دقيقة"
            tvStatus.setTextColor(getColor(R.color.gold_400))
        } else {
            tvStatus.text = "نشط"
            tvStatus.setTextColor(getColor(R.color.teal_400))
        }

        // Prayer Tracking (inline)
        val prayerTrackingContainer = findViewById<LinearLayout>(R.id.prayerTrackingContainer) ?: return
        prayerTrackingContainer.removeAllViews()

        val prayerList = prayerManager.getPrayerTimesList(lat, lng, method, enabledPrayers, testOverrides)
        val now = Date()
        val tvTodayScore = findViewById<TextView>(R.id.tvTodayScore)
        tvTodayScore.text = "${getTodayMarkedCount()} / ${prayerList.size}"

        for (prayer in prayerList) {
            val itemView = layoutInflater.inflate(R.layout.item_prayer_tracking, prayerTrackingContainer, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvTrackingPrayerName)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTrackingPrayerTime)
            val btnToggle = itemView.findViewById<View>(R.id.btnTogglePrayer)
            val tvCheckIcon = itemView.findViewById<TextView>(R.id.tvCheckIcon)
            val prayerDot = itemView.findViewById<View>(R.id.prayerDot)

            tvName.text = prayer.nameArabic
            tvTime.text = prayerManager.formatTime(prayer.time)

            val isMarked = isPrayerMarkedToday(prayer.name)
            val isPast = prayer.time.before(now)

            if (isMarked) {
                tvCheckIcon.visibility = View.VISIBLE
                tvCheckIcon.setTextColor(getColor(R.color.teal_400))
                tvName.setTextColor(getColor(R.color.teal_400))
                tvTime.setTextColor(getColor(R.color.teal_400))
                prayerDot.visibility = View.VISIBLE
            } else if (isPast) {
                tvCheckIcon.visibility = View.GONE
                tvName.setTextColor(getColor(R.color.text_muted))
                tvTime.setTextColor(getColor(R.color.text_muted))
                prayerDot.visibility = View.INVISIBLE
            } else {
                tvCheckIcon.visibility = View.GONE
                tvName.setTextColor(getColor(R.color.text_primary))
                tvTime.setTextColor(getColor(R.color.gold_400))
                prayerDot.visibility = View.INVISIBLE
            }

            btnToggle.setOnClickListener {
                val newState = !isPrayerMarkedToday(prayer.name)
                setPrayerMarkedToday(prayer.name, newState)
                // Haptic feedback
                btnToggle.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                updateUI()
            }

            prayerTrackingContainer.addView(itemView)
        }

        // Rotate quote every 30 seconds
        handler.postDelayed({ updateQuote() }, 120000)
    }
}
