package com.example.project2026.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.project2026.MainActivity
import com.example.project2026.R

object GeofenceNotificationHelper {

    private const val CHANNEL_ID = "geofence_channel"
    private const val CHANNEL_NAME = "Parcheggio nelle vicinanze"
    private const val NOTIFICATION_ID = 1001

    fun inviaNotifica(context: Context, geofenceId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        creaCanaleNotifica(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // sostituisci con la tua icona
            .setContentTitle("Sei vicino a un parcheggio salvato!")
            .setContentText("Vuoi iniziare un nuovo parcheggio qui?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Sei a meno di 100 metri da una posizione salvata. Vuoi iniziare un nuovo parcheggio?")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun creaCanaleNotifica(manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifiche quando sei vicino a un parcheggio salvato"
        }
        manager.createNotificationChannel(channel)
    }
}