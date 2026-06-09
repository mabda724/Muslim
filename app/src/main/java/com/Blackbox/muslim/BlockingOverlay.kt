package com.Blackbox.muslim

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class BlockingOverlay(private val context: Context) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    fun show(prayerName: String, prayerNameArabic: String, remainingMinutes: Int) {
        if (overlayView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.overlay_blocking, null)

        val tvPrayerName = overlayView!!.findViewById<TextView>(R.id.tvOverlayPrayerName)
        val tvPrayerNameArabic = overlayView!!.findViewById<TextView>(R.id.tvOverlayPrayerNameArabic)
        val tvRemaining = overlayView!!.findViewById<TextView>(R.id.tvOverlayRemaining)
        val btnDismiss = overlayView!!.findViewById<Button>(R.id.btnOverlayDismiss)

        tvPrayerName.text = prayerName
        tvPrayerNameArabic.text = "صلاة $prayerNameArabic"
        tvRemaining.text = "الوقت المتبقي: $remainingMinutes دقيقة"

        btnDismiss.setOnClickListener {
            dismiss()
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            intent.addCategory(android.content.Intent.CATEGORY_HOME)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
            windowManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isShowing(): Boolean = overlayView != null
}
