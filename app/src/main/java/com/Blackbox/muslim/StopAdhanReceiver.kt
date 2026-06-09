package com.Blackbox.muslim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopAdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("StopAdhan", "Stopping adhan")
        try {
            val stopIntent = Intent(context, com.Blackbox.muslim.ui.AdhanService::class.java).apply {
                action = com.Blackbox.muslim.ui.AdhanService.ACTION_STOP
            }
            context.startService(stopIntent)
        } catch (e: Exception) {
            Log.e("StopAdhan", "Error", e)
        }
    }
}
