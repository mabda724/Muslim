package com.Blackbox.muslim.ui

import android.animation.ValueAnimator
import com.Blackbox.muslim.data.AppPreferences
import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Blackbox.muslim.R

class AdhanAlarmActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = true
    private val handler = Handler(Looper.getMainLooper())
    private var elapsed = 0
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FORCE show over lock screen + turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Apply theme
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        // Make it show over other apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        }

        setContentView(R.layout.activity_adhan_alarm)
        acquireWakeLock()

        val prayerName = intent.getStringExtra("prayer_name") ?: "unknown"
        val prayerNameArabic = intent.getStringExtra("prayer_name_arabic") ?: "الصلاة"

        findViewById<TextView>(R.id.tvPrayerName).text = prayerNameArabic

        val now = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        findViewById<TextView>(R.id.tvPrayerTime).text = now

        animateGlow()
        startAdhan(prayerName)

        findViewById<View>(R.id.btnPlayPause).setOnClickListener {
            togglePlayPause()
        }

        findViewById<View>(R.id.btnStop).setOnClickListener {
            stopAndFinish()
        }

        startProgressUpdater()
    }

    private fun startAdhan(prayerName: String) {
        try {
            val rawResId = if (prayerName.equals("fajr", ignoreCase = true)) R.raw.fajr else R.raw.azan

            val mp = MediaPlayer.create(this, rawResId)

            if (mp != null) {
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mp.isLooping = true
                mp.start()
                mediaPlayer = mp
            } else {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val fallback = MediaPlayer()
                fallback.setDataSource(this, uri)
                fallback.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                fallback.isLooping = true
                fallback.setOnPreparedListener { it.start() }
                fallback.prepareAsync()
                mediaPlayer = fallback
            }

            isPlaying = true
            updatePlayPauseButton()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val mp = MediaPlayer()
                mp.setDataSource(this, uri)
                mp.isLooping = true
                mp.setOnPreparedListener { it.start() }
                mp.prepareAsync()
                mediaPlayer = mp
                isPlaying = true
                updatePlayPauseButton()
            } catch (e2: Exception) { e2.printStackTrace() }
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (isPlaying) { it.pause(); isPlaying = false }
            else { it.start(); isPlaying = true }
            updatePlayPauseButton()
        }
    }

    private fun updatePlayPauseButton() {
        findViewById<TextView>(R.id.tvPlayPause).text = if (isPlaying) "⏸" else "▶️"
    }

    private fun stopAndFinish() {
        mediaPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
        mediaPlayer = null
        releaseWakeLock()
        finish()
    }

    private fun startProgressUpdater() {
        val progress = findViewById<ProgressBar>(R.id.progressAdhan)
        val runnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    elapsed += 100
                    val duration = mediaPlayer?.duration ?: 120000
                    val pct = ((elapsed.toFloat() / duration.toFloat()) * 100).toInt().coerceAtMost(100)
                    progress.progress = pct
                }
                if (elapsed < 180000) handler.postDelayed(this, 100)
            }
        }
        handler.postDelayed(runnable, 100)
    }

    private fun animateGlow() {
        val v = findViewById<TextView>(R.id.tvPrayerName)
        ValueAnimator.ofFloat(1f, 1.05f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { v.scaleX = it.animatedValue as Float; v.scaleY = it.animatedValue as Float }
        }.start()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "muslim:alarm")
        wakeLock?.acquire(3 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let { if (it.isPlaying) it.stop(); it.release() }
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
        releaseWakeLock()
    }
}
