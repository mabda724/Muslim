package com.Blackbox.muslim.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.Blackbox.muslim.MainActivity
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.DailyDhikrData
import java.text.SimpleDateFormat
import java.util.*

class DhikrWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dhikr)

            // Get daily dhikr
            val dailyDhikr = DailyDhikrData.getDailyDhikr()

            // Update widget views
            views.setTextViewText(R.id.widgetDhikrArabic, dailyDhikr.arabic)
            views.setTextViewText(R.id.widgetDhikrTranslation, dailyDhikr.translation)

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
