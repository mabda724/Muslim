package com.Blackbox.muslim.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager

class AdhanPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var playing = false

    fun playAdhan(volume: Int = 80) {
        if (playing) return

        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val mp = MediaPlayer()
            mp.setDataSource(context, uri)
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            val volumeFloat = volume / 100f
            mp.setVolume(volumeFloat, volumeFloat)
            mp.isLooping = false
            mp.setOnPreparedListener {
                it.start()
                playing = true
            }
            mp.setOnCompletionListener {
                playing = false
            }
            mp.setOnErrorListener { _, _, _ ->
                playing = false
                true
            }
            mp.prepareAsync()
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            playing = false
        }
    }

    fun stopAdhan() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
            playing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(): Boolean = playing

    fun release() {
        stopAdhan()
    }
}
