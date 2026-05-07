package com.example.project2026.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.project2026.MainActivity

/**
 * Gestisce le notifiche di parcheggio con pattern Builder
 */
class ParcheggioNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManager
) {

    companion object {
        private const val CHANNEL_ID = "parking_sessions_channel"
        private const val CHANNEL_NAME = "Sessioni di Parcheggio"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_SCADENZA_ID = 1002
        private const val NOTIFICATION_AVVISO_ID = 1003
    }

    init {
        creaCanale()
    }

    /**
     * Crea il canale di notifica (richiesto per Android 8+)
     */
    private fun creaCanale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canale = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifiche per le sessioni di parcheggio attive"
                enableLights(true)
                lightColor = 0xFFFFFF00.toInt() // Giallo
                enableVibration(false) // La vibrazione la controlliamo manualmente
            }
            notificationManager.createNotificationChannel(canale)
        }
    }

    /**
     * Builder per creare la notifica di parcheggio
     */
    fun notificaBuilder(): NotificaBuilder = NotificaBuilder(context, notificationManager)

    /**
     * Mostra una notifica di parcheggio
     */
    fun mostraNotifica(
        nomVeicolo: String,
        tipoVeicolo: String,
        tempoTrascorso: String,
        costoAccumulato: String?,
        sessionId: Int = 0,
        vibrare: Boolean = false
    ) {
        val notifica = notificaBuilder()
            .setNomeVeicolo(nomVeicolo)
            .setTipoVeicolo(tipoVeicolo)
            .setTempoTrascorso(tempoTrascorso)
            .setCostoAccumulato(costoAccumulato)
            .setSessionId(sessionId)
            .setVibrare(vibrare)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notifica)
    }

    /**
     * Elimina la notifica
     */
    fun eliminaNotifica() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Mostra una notifica di scadenza per parcheggio TICKET
     */
    fun mostraNotificaScadenza(nomVeicolo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifica = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Parcheggio Scaduto")
            .setContentText("Il parcheggio di $nomVeicolo è scaduto!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Il tempo di parcheggio a pagamento per $nomVeicolo è scaduto.\nRinnovalo se necessario.")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismissibile
            .setOngoing(false) // Non sticky
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setShowWhen(true)
            .build()

        notificationManager.notify(NOTIFICATION_SCADENZA_ID, notifica)
    }

    /**
     * Mostra un avviso di scadenza imminente
     */
    fun mostraAvvisoScadenza(nomVeicolo: String, tempoRimanente: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifica = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚠ Avviso Scadenza")
            .setContentText("$nomVeicolo scadrà tra: $tempoRimanente")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Il parcheggio di $nomVeicolo scadrà tra: $tempoRimanente")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setShowWhen(true)
            .build()

        notificationManager.notify(NOTIFICATION_AVVISO_ID, notifica)
    }

    /**
     * Builder per costruire la notifica
     */
    class NotificaBuilder(
        private val context: Context,
        private val notificationManager: NotificationManager
    ) {
        private var nomeVeicolo: String = ""
        private var tipoVeicolo: String = ""
        private var tempoTrascorso: String = "00:00:00"
        private var costoAccumulato: String? = null
        private var vibrare: Boolean = false
        private var sessionId: Int = 0

        fun setNomeVeicolo(nome: String) = apply { this.nomeVeicolo = nome }
        fun setTipoVeicolo(tipo: String) = apply { this.tipoVeicolo = tipo }
        fun setTempoTrascorso(tempo: String) = apply { this.tempoTrascorso = tempo }
        fun setCostoAccumulato(costo: String?) = apply { this.costoAccumulato = costo }
        fun setVibrare(vibra: Boolean) = apply { this.vibrare = vibra }
        fun setSessionId(id: Int) = apply { this.sessionId = id }

        fun build(): Notification {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent per tracciare quando la notifica viene dismissata
            val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
                action = NotificationDismissReceiver.ACTION_NOTIFICA_DISMISSATA
                putExtra(NotificationDismissReceiver.EXTRA_SESSION_ID, sessionId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val testoContenuto = buildString {
                append("$nomeVeicolo • $tipoVeicolo\n")
                append("Tempo: $tempoTrascorso")
                if (costoAccumulato != null) {
                    append("\nCosto: $costoAccumulato")
                }
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Parcheggio Attivo")
                .setContentText(testoContenuto)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(testoContenuto)
                )
                .setContentIntent(pendingIntent)
                .setDeleteIntent(dismissPendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)

            // Vibrazione solo se richiesto
            if (vibrare) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(200)
            }

            return builder.build()
        }
    }
}

