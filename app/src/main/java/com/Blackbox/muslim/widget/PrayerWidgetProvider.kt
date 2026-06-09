package com.Blackbox.muslim.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.Blackbox.muslim.MainActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.PrayerTracker
import com.Blackbox.muslim.data.GamificationManager
import java.text.SimpleDateFormat
import java.util.*

class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer)

            // Get prayer data
            val prayerTracker = PrayerTracker(context)
            val gamificationManager = GamificationManager(context)
            val todayStats = prayerTracker.getDayStats()
            val totalPoints = gamificationManager.getTotalPoints()
            val currentLevel = gamificationManager.getCurrentLevel()

            // Update widget views
  views.setTextViewText(R.id.widgetPrayerCount, "${todayStats.prayedCount} / 5 (${todayStats.percentage}%)")
  views.setTextViewText(R.id.widgetPoints, "$totalPoints نقطة")
            views.setTextViewText(R.id.widgetLevel, "${currentLevel.level} - ${currentLevel.title}")
            views.setTextViewText(R.id.widgetDate, SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date()))

            // Update progress bar
            views.setProgressBar(R.id.widgetProgress, 100, todayStats.percentage, false)

            // Create click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent)

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
