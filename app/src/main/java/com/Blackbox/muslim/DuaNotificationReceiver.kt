package com.Blackbox.muslim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class DuaNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "dua_channel"
        const val NOTIFICATION_ID = 5000
        const val ACTION_SHOW = "com.Blackbox.muslim.SHOW_DUA"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("دعاء للمطور 🤲")
            .setContentText("ادعو للمطور محمد ابراهيم عبدالله")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("اللهم ارحم عبدك محمد ابراهيم عبدالله وزوجته ووالديه وإخوته\n\n" +
                    "اللهم بارك لهم في حياتهم واحتسب خيرهم واغفر ذنوبهم\n\n" +
                    "اللهم شفي عبدك محمد ابراهيم عبدالله كلاء شفاءك ولا تترك به عاهة\n\n" +
                    "اللهم قضِ دينه وأغنه عن الناس وأرزقه من حيث لا يحتسب\n\n" +
                    "اللهم وفقهم لكل خير واجمع شملهم على طاعتك\n\n" +
                    "من فضلك ادّعُ لهم ولو مرة واحدة 🤲"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "دعاء للمطور",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "دعوة خير للمطور محمد ابراهيم عبدالله"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
