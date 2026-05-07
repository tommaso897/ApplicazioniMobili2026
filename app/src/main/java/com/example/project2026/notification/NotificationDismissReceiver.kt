package com.example.project2026.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Riceve l'evento quando l'utente dismissisce (elimina) una notifica di parcheggio
 */
class NotificationDismissReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_NOTIFICA_DISMISSATA = "com.example.project2026.NOTIFICA_DISMISSATA"
        const val EXTRA_SESSION_ID = "session_id"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_NOTIFICA_DISMISSATA) {
            val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
            if (sessionId != -1) {
                // Memorizza il timestamp in cui è stata dismissata - usa la stessa chiave del ViewModel
                val prefs = context?.getSharedPreferences("sessione_parcheggio", Context.MODE_PRIVATE)
                prefs?.edit()?.putLong("dismissed_$sessionId", System.currentTimeMillis())?.apply()
            }
        }
    }
}

