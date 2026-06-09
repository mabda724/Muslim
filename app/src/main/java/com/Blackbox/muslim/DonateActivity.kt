package com.Blackbox.muslim

import android.content.ClipData
import com.Blackbox.muslim.data.AppPreferences
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DonateActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        val btnBack = findViewById<Button>(R.id.btnBackDonate)
        val tvCopyNumber = findViewById<TextView>(R.id.tvCopyNumber)
        val btnCopyNumber = findViewById<Button>(R.id.btnCopyNumber)

        btnBack.setOnClickListener { finish() }

        btnCopyNumber.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Vodafone Cash", "01063448604")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "تم نسخ الرقم", Toast.LENGTH_SHORT).show()
        }

        tvCopyNumber.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Vodafone Cash", "01063448604")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "تم نسخ الرقم", Toast.LENGTH_SHORT).show()
        }
    }
}
