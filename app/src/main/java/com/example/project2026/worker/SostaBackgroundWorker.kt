package com.example.project2026.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.notification.ParcheggioNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Worker che gira periodicamente in background anche quando l'app è chiusa.
 * Si occupa di:
 * 1. Controllare le sessioni di parcheggio attive
 * 2. Inviare/aggiornare le notifiche di sosta in corso
 * 3. Terminare automaticamente i ticket scaduti e inviare la relativa notifica
 * 4. Inviare avvisi di scadenza imminente per i ticket
 */
class SostaBackgroundWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SostaBackgroundWorker"
        private const val WORK_NAME = "monitoraggio_soste_periodico"

        /**
         * Avvia il monitoraggio periodico in background.
         * Viene chiamato da ParkMateApplication all'avvio e dal BootBroadcastReceiver al riavvio.
         */
        fun avviaMonitoraggioPeriodico(context: Context) {
            val request = PeriodicWorkRequestBuilder<SostaBackgroundWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Monitoraggio periodico in background avviato")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Esecuzione del worker in background...")

        val db = AppDatabase.getDatabase(applicationContext)
        val sessioneDao = db.sessioneParcheggioDao()
        val veicoloDao = db.veicoloDao()
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val parcheggioNotificationManager = ParcheggioNotificationManager(
            applicationContext, notificationManager
        )
        val prefs = applicationContext.getSharedPreferences("sessione_parcheggio", Context.MODE_PRIVATE)

        try {
            // Recupera tutte le sessioni attive (query one-shot, non Flow)
            val sessioniAttive = sessioneDao.ottieniTutteLeSessioniAttive()

            if (sessioniAttive.isEmpty()) {
                Log.d(TAG, "Nessuna sessione attiva trovata. Worker terminato.")
                return Result.success()
            }

            Log.d(TAG, "Trovate ${sessioniAttive.size} sessioni attive")

            val oraAttuale = System.currentTimeMillis()

            for (sessione in sessioniAttive) {

                // ── 1. CONTROLLO SCADENZA TICKET ──
                if (sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null) {
                    if (oraAttuale >= sessione.scadenza) {
                        // Ticket scaduto: termina la sessione e invia notifica
                        val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                        veicolo?.let {
                            parcheggioNotificationManager.mostraNotificaScadenza(it.nome, sessione.id)
                        }
                        terminaSessioneInBackground(sessione, sessioneDao, veicoloDao, parcheggioNotificationManager)
                        Log.d(TAG, "Ticket scaduto per sessione ${sessione.id}. Terminata.")
                        continue // Passa alla prossima sessione
                    }
                }

                // ── 2. CONTROLLO AVVISO SCADENZA IMMINENTE ──
                if (sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null) {
                    val minutiAvviso = prefs.getInt("avviso_minuti_${sessione.id}", -1)
                    if (minutiAvviso > 0) {
                        val tempoAvviso = sessione.scadenza - (minutiAvviso * 60 * 1000L)
                        if (oraAttuale >= tempoAvviso && oraAttuale < sessione.scadenza) {
                            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                            veicolo?.let {
                                val tempoRimanente = formattaTempoTrascorso(sessione.scadenza - oraAttuale)
                                parcheggioNotificationManager.mostraAvvisoScadenza(
                                    it.nome, tempoRimanente, sessione.id
                                )
                            }
                            Log.d(TAG, "Avviso scadenza imminente per sessione ${sessione.id}")
                        }
                    }
                }

                // ── 3. NOTIFICA SOSTA IN CORSO ──
                if (sessione.attivo) {
                    val minutiTrascorsi = (oraAttuale - sessione.inizio) / (1000 * 60)
                    if (minutiTrascorsi >= 5) {
                        val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                        veicolo?.let {
                            val tempoTrascorso = formattaTempoTrascorso(oraAttuale - sessione.inizio)
                            val costoAccumulato = calcolaCostoAccumulato(sessione, oraAttuale)
                            parcheggioNotificationManager.mostraNotifica(
                                nomVeicolo = veicolo.nome,
                                tipoVeicolo = veicolo.tipoVeicolo.name,
                                tempoTrascorso = tempoTrascorso,
                                costoAccumulato = costoAccumulato,
                                sessionId = sessione.id,
                                vibrare = false
                            )
                        }
                        Log.d(TAG, "Notifica aggiornata per sessione ${sessione.id}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante l'esecuzione del worker: ${e.message}", e)
            return Result.retry()
        }

        return Result.success()
    }

    /**
     * Termina una sessione direttamente dal worker in background,
     * replicando la logica del ViewModel per il caso in cui l'app non sia aperta.
     */
    private suspend fun terminaSessioneInBackground(
        sessione: SessioneParcheggio,
        sessioneDao: com.example.project2026.data.SessioneParcheggioDao,
        veicoloDao: com.example.project2026.data.VeicoloDao,
        parcheggioNotificationManager: ParcheggioNotificationManager
    ) {
        val timestampFine = System.currentTimeMillis()
        val dataFine = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampFine))

        var costoFinale = sessione.costo ?: 0.0
        if (sessione.tipo == TipoParcheggio.PAID && sessione.tariffa != null) {
            val oreTrascorse = (timestampFine - sessione.inizio) / (1000.0 * 60 * 60)
            costoFinale = oreTrascorse * sessione.tariffa
        }

        val sessioneTerminata = sessione.copy(
            fine = timestampFine,
            dataFine = dataFine,
            costo = costoFinale,
            attivo = false,
            stato = StatoParcheggio.LIBERO
        )
        sessioneDao.aggiornaSessione(sessioneTerminata)

        val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
        veicolo?.let {
            veicoloDao.aggiornaVeicolo(it.copy(statoParcheggio = StatoParcheggio.LIBERO))
        }

        // Rimuove la notifica persistente della sosta
        parcheggioNotificationManager.eliminaNotifica(sessione.id)
    }

    private fun formattaTempoTrascorso(milliseconds: Long): String {
        var secondi = milliseconds / 1000
        val ore = secondi / 3600
        secondi %= 3600
        val minuti = secondi / 60
        secondi %= 60
        return String.format("%02d:%02d:%02d", ore, minuti, secondi)
    }

    private fun calcolaCostoAccumulato(sessione: SessioneParcheggio, oraAttuale: Long): String? {
        return when (sessione.tipo) {
            TipoParcheggio.PAID -> {
                val oreTrascorse = (oraAttuale - sessione.inizio) / (1000.0 * 60 * 60)
                String.format("%.2f€", oreTrascorse * (sessione.tariffa ?: 0.0))
            }
            TipoParcheggio.TICKET -> sessione.costo?.let { String.format("%.2f€", it) }
            TipoParcheggio.FREE -> null
        }
    }
}
