package com.Blackbox.muslim.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences

class PinCodeActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private var pinInput = StringBuilder()
    private var isConfirming = false
    private var firstPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code)

        val tvTitle = findViewById<TextView>(R.id.tvPinTitle)
        val tvSubtitle = findViewById<TextView>(R.id.tvPinSubtitle)
        val tvPinDisplay = findViewById<TextView>(R.id.tvPinDisplay)
        val btn0 = findViewById<Button>(R.id.btnPin0)
        val btn1 = findViewById<Button>(R.id.btnPin1)
        val btn2 = findViewById<Button>(R.id.btnPin2)
        val btn3 = findViewById<Button>(R.id.btnPin3)
        val btn4 = findViewById<Button>(R.id.btnPin4)
        val btn5 = findViewById<Button>(R.id.btnPin5)
        val btn6 = findViewById<Button>(R.id.btnPin6)
        val btn7 = findViewById<Button>(R.id.btnPin7)
        val btn8 = findViewById<Button>(R.id.btnPin8)
        val btn9 = findViewById<Button>(R.id.btnPin9)
        val btnDelete = findViewById<Button>(R.id.btnPinDelete)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)

        val existingPin = preferences.getPinCode()
        isConfirming = existingPin.isEmpty()

        if (isConfirming) {
            tvTitle.text = "أنشئ رمز PIN"
            tvSubtitle.text = "أدخل 4 أرقام لحماية الإعدادات"
        } else {
            tvTitle.text = "أدخل الرمز"
            tvSubtitle.text = "أدخل رمز PIN للوصول للإعدادات"
        }

        btnBiometric.setOnClickListener {
            val biometricHelper = BiometricHelper(this)
            if (biometricHelper.isBiometricAvailable()) {
                biometricHelper.showBiometricPrompt(
                    callback = object : BiometricHelper.AuthCallback {
                        override fun onAuthSuccess() {
                            val intent = Intent()
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                        override fun onAuthFailed() {}
                        override fun onAuthError(errorCode: Int, errString: CharSequence) {}
                    }
                )
            } else {
                Toast.makeText(this, "البصمة غير متاحة", Toast.LENGTH_SHORT).show()
            }
        }

        val pinButtons = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)

        pinButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (pinInput.length < 4) {
                    pinInput.append(index)
                    updatePinDisplay(tvPinDisplay)

                    if (pinInput.length == 4) {
                        if (isConfirming) {
                            if (firstPin.isEmpty()) {
                                firstPin = pinInput.toString()
                                pinInput.clear()
                                tvTitle.text = "تأكيد الرمز"
                                tvSubtitle.text = "أدخل الرمز مرة أخرى"
                                updatePinDisplay(tvPinDisplay)
                            } else {
                                if (pinInput.toString() == firstPin) {
                                    preferences.setPinCode(pinInput.toString())
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "الرمز غير متطابق", Toast.LENGTH_SHORT).show()
                                    pinInput.clear()
                                    firstPin = ""
                                    tvTitle.text = "أنشئ رمز PIN"
                                    tvSubtitle.text = "أدخل 4 أرقام لحماية الإعدادات"
                                    updatePinDisplay(tvPinDisplay)
                                }
                            }
                        } else {
                            if (pinInput.toString() == existingPin) {
                                val intent = Intent()
                                setResult(RESULT_OK, intent)
                                finish()
                            } else {
                                Toast.makeText(this, "الرمز خاطئ", Toast.LENGTH_SHORT).show()
                                pinInput.clear()
                                updatePinDisplay(tvPinDisplay)
                            }
                        }
                    }
                }
            }
        }

        btnDelete.setOnClickListener {
            if (pinInput.isNotEmpty()) {
                pinInput.deleteCharAt(pinInput.length - 1)
                updatePinDisplay(tvPinDisplay)
            }
        }
    }

    private fun updatePinDisplay(tvPinDisplay: TextView) {
        val dots = "●".repeat(pinInput.length) + "○".repeat(4 - pinInput.length)
        tvPinDisplay.text = dots
    }
}
