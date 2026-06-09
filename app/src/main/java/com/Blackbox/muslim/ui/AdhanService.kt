package com.Blackbox.muslim.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.Blackbox.muslim.MainActivity
import com.Blackbox.muslim.R

class AdhanService : Service() {

    companion object {
        const val CHANNEL_ID = "adhan_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_PLAY = "com.Blackbox.muslim.PLAY_ADHAN"
        const val ACTION_STOP = "com.Blackbox.muslim.STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        private const val TAG = "AdhanService"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val volume = intent.getIntExtra("volume", 80)
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: ""
                playAdhan(volume, prayerName)
            }
            ACTION_STOP -> {
                stopAdhan()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "muslim:adhan_wakelock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    private fun playAdhan(volume: Int, prayerName: String) {
        Log.d(TAG, "playAdhan: prayer=$prayerName, volume=$volume")

        val notification = buildNotification("جاري تشغيل الأذان...")
        startForeground(NOTIFICATION_ID, notification)

        acquireWakeLock()

        try {
            val rawResId = if (prayerName == "fajr") {
                R.raw.fajr
            } else {
                R.raw.azan
            }

            Log.d(TAG, "Loading raw resource: $rawResId for prayer: $prayerName")

            val mp = MediaPlayer.create(this, rawResId)

            if (mp == null) {
                Log.e(TAG, "MediaPlayer.create returned null for rawResId=$rawResId")
                fallbackToRingtone(volume)
                return
            }

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_ALARM)
                    .build()
            )

            val volumeFloat = volume / 100f
            mp.setVolume(volumeFloat, volumeFloat)
            mp.isLooping = false

            mp.setOnCompletionListener {
                Log.d(TAG, "Adhan playback completed")
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                true
            }

            mp.start()
            mediaPlayer = mp
            Log.d(TAG, "Adhan started playing")

        } catch (e: Exception) {
            Log.e(TAG, "Error playing adhan", e)
            fallbackToRingtone(volume)
        }
    }

    private fun fallbackToRingtone(volume: Int) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val mp = MediaPlayer()
            mp.setDataSource(this, uri)
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            val volumeFloat = volume / 100f
            mp.setVolume(volumeFloat, volumeFloat)
            mp.isLooping = false
            mp.setOnCompletionListener {
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            mp.setOnErrorListener { _, _, _ ->
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                true
            }
            mp.prepareAsync()
            mp.setOnPreparedListener { it.start() }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e(TAG, "Fallback ringtone also failed", e)
            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopAdhan() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping adhan", e)
        }
        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("أذان الصلاة")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعار الأذان"
                enableVibration(true)
                setBypassDnd(true)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAdhan()
    }
}
