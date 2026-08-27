package com.example.project2026.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.notification.ParcheggioNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Worker one-shot programmato per scattare esattamente allo scadere di un ticket.
 * Viene schedulato nel momento in cui l'utente avvia una sosta di tipo TICKET,
 * con un ritardo iniziale pari al tempo rimanente prima della scadenza.
 * In questo modo, anche se l'app è chiusa, il ticket viene terminato e la notifica inviata.
 */
class TicketScadutoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TicketScadutoWorker"
        private const val KEY_SESSION_ID = "session_id"

        /**
         * Programma un worker one-shot che si attiverà esattamente alla scadenza del ticket.
         * @param context Il contesto dell'applicazione
         * @param sessioneId L'ID della sessione di parcheggio
         * @param scadenza Il timestamp esatto di scadenza del ticket (in millisecondi)
         */
        fun programmaScadenza(context: Context, sessioneId: Int, scadenza: Long) {
            val ritardo = scadenza - System.currentTimeMillis()
            if (ritardo <= 0) return // Il ticket è già scaduto

            val request = OneTimeWorkRequestBuilder<TicketScadutoWorker>()
                .setInitialDelay(ritardo, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(KEY_SESSION_ID to sessioneId))
                .build()

            // Usa REPLACE: se l'utente ha già un timer per questa sessione, lo sostituisce
            WorkManager.getInstance(context).enqueueUniqueWork(
                "ticket_scaduto_$sessioneId",
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Scadenza ticket programmata per sessione $sessioneId tra ${ritardo / 1000}s")
        }

        /**
         * Programma un worker one-shot per l'avviso di scadenza imminente.
         * Si attiva N minuti prima della scadenza effettiva del ticket.
         */
        fun programmaAvviso(context: Context, sessioneId: Int, scadenza: Long, minutiAnticipo: Int) {
            val tempoAvviso = scadenza - (minutiAnticipo * 60 * 1000L)
            val ritardo = tempoAvviso - System.currentTimeMillis()
            if (ritardo <= 0) return // Il momento dell'avviso è già passato

            val request = OneTimeWorkRequestBuilder<TicketScadutoWorker>()
                .setInitialDelay(ritardo, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    KEY_SESSION_ID to sessioneId,
                    "is_avviso" to true
                ))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "ticket_avviso_$sessioneId",
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Avviso scadenza programmato per sessione $sessioneId tra ${ritardo / 1000}s")
        }
    }

    override suspend fun doWork(): Result {
        val sessioneId = inputData.getInt(KEY_SESSION_ID, -1)
        val isAvviso = inputData.getBoolean("is_avviso", false)

        if (sessioneId == -1) {
            Log.e(TAG, "ID sessione non valido")
            return Result.failure()
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val sessioneDao = db.sessioneParcheggioDao()
        val veicoloDao = db.veicoloDao()
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val parcheggioNotificationManager = ParcheggioNotificationManager(
            applicationContext, notificationManager
        )

        try {
            val sessione = sessioneDao.ottieniSessionePerId(sessioneId)
            if (sessione == null || !sessione.attivo) {
                Log.d(TAG, "Sessione $sessioneId non trovata o già terminata")
                return Result.success()
            }

            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)

            if (isAvviso) {
                // ── AVVISO DI SCADENZA IMMINENTE ──
                if (sessione.scadenza != null && System.currentTimeMillis() < sessione.scadenza) {
                    val tempoRimanente = formattaTempoTrascorso(sessione.scadenza - System.currentTimeMillis())
                    veicolo?.let {
                        parcheggioNotificationManager.mostraAvvisoScadenza(it.nome, tempoRimanente, sessione.id)
                    }
                    Log.d(TAG, "Avviso scadenza inviato per sessione $sessioneId")
                }
            } else {
                // ── SCADENZA EFFETTIVA DEL TICKET ──
                veicolo?.let {
                    parcheggioNotificationManager.mostraNotificaScadenza(it.nome, sessione.id)
                }

                // Termina la sessione nel database
                val timestampFine = System.currentTimeMillis()
                val dataFine = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampFine))
                val sessioneTerminata = sessione.copy(
                    fine = timestampFine,
                    dataFine = dataFine,
                    attivo = false,
                    stato = StatoParcheggio.LIBERO
                )
                sessioneDao.aggiornaSessione(sessioneTerminata)

                veicolo?.let {
                    veicoloDao.aggiornaVeicolo(it.copy(statoParcheggio = StatoParcheggio.LIBERO))
                }

                parcheggioNotificationManager.eliminaNotifica(sessione.id)
                Log.d(TAG, "Ticket scaduto. Sessione $sessioneId terminata in background.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel worker: ${e.message}", e)
            return Result.retry()
        }

        return Result.success()
    }

    private fun formattaTempoTrascorso(milliseconds: Long): String {
        var secondi = milliseconds / 1000
        val ore = secondi / 3600
        secondi %= 3600
        val minuti = secondi / 60
        secondi %= 60
        return String.format("%02d:%02d:%02d", ore, minuti, secondi)
    }
}
