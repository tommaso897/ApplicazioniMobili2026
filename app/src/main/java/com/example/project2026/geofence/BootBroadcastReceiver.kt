package com.example.project2026.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.project2026.data.AppDatabase
import com.example.project2026.utility.SessionManager
import com.example.project2026.worker.SostaBackgroundWorker
import com.example.project2026.worker.TicketScadutoWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver che si attiva al riavvio del dispositivo.
 * Si occupa di:
 * 1. Ripristinare tutti i recinti Geofence registrati
 * 2. Riavviare il monitoraggio periodico delle soste (WorkManager)
 * 3. Riprogrammare le scadenze esatte dei ticket attivi
 */
class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Rilevato riavvio del sistema. Ripristino servizi in background...")

            // ── 1. RIAVVIA IL MONITORAGGIO PERIODICO DELLE SOSTE ──
            SostaBackgroundWorker.avviaMonitoraggioPeriodico(context)

            // Inizializza SessionManager per caricare l'utente loggato dalle SharedPreferences
            val sessionManager = SessionManager(context)
            val idUtente = sessionManager.idUtente

            if (idUtente != null) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)

                        // ── 2. RIPRISTINO GEOFENCE ──
                        val posizioneSalvataDao = db.posizioneSalvataDao()
                        val geofenceManager = GeofenceManager(context)
                        val posizioni = posizioneSalvataDao.ottieniTutteLePosizioni(idUtente).first()

                        Log.d(TAG, "Trovate ${posizioni.size} posizioni da ripristinare per l'utente $idUtente")
                        for (posizione in posizioni) {
                            geofenceManager.aggiungiGeofence(posizione)
                        }

                        // ── 3. RIPROGRAMMAZIONE SCADENZE TICKET ──
                        val sessioneDao = db.sessioneParcheggioDao()
                        val sessioniAttive = sessioneDao.ottieniTutteLeSessioniAttive()
                        val oraAttuale = System.currentTimeMillis()

                        for (sessione in sessioniAttive) {
                            if (sessione.tipo == com.example.project2026.data.TipoParcheggio.TICKET
                                && sessione.scadenza != null
                                && sessione.scadenza > oraAttuale
                            ) {
                                TicketScadutoWorker.programmaScadenza(context, sessione.id, sessione.scadenza)
                                Log.d(TAG, "Riprogrammata scadenza ticket per sessione ${sessione.id}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Errore durante il ripristino al boot: ${e.message}", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                Log.d(TAG, "Nessun utente loggato rilevato al boot.")
            }
        }
    }

    companion object {
        private const val TAG = "BootBroadcastReceiver"
    }
}
