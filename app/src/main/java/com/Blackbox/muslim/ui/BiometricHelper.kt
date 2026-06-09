package com.Blackbox.muslim.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

  interface AuthCallback {
    fun onAuthSuccess()
    fun onAuthFailed()
    fun onAuthError(errorCode: Int, errString: CharSequence)
  }

  fun isBiometricAvailable(): Boolean {
    val biometricManager = BiometricManager.from(activity)
    return biometricManager.canAuthenticate(
      BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS
  }

  fun showBiometricPrompt(
    title: String = "تأكيد الهوية",
    subtitle: String = "استخدم بصمة الإصبع أو نمط القفل لتأكيد التغيير",
    callback: AuthCallback
  ) {
    val executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(activity, executor,
      object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
          callback.onAuthSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
          callback.onAuthError(errorCode, errString)
        }

        override fun onAuthenticationFailed() {
          callback.onAuthFailed()
        }
      })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(title)
      .setSubtitle(subtitle)
      .setAllowedAuthenticators(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
          BiometricManager.Authenticators.DEVICE_CREDENTIAL
      )
      .build()

    biometricPrompt.authenticate(promptInfo)
  }
}
