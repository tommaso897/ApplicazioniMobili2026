package com.example.project2026.utility

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Gestisce la sessione dell'utente loggato.
 * Persiste l'id e l'username nelle SharedPreferences così
 * l'utente rimane loggato anche dopo la chiusura dell'app.
 *
 * Il companion object [utenteCorrente] è un Flow reattivo:
 * quando l'utente fa login o logout, tutti i ViewModel che lo
 * osservano aggiornano automaticamente le loro query sul DB.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    init {
        // Sincronizza il flow con il valore persistito.
        // Viene eseguito ogni volta che un ViewModel crea un SessionManager,
        // garantendo che il flow sia sempre aggiornato al riavvio dell'app.
        val persistedId = prefs.getInt("id_utente", -1)
        utenteCorrente.value = if (persistedId == -1) 0 else persistedId
    }

    companion object {
        /**
         * Flow reattivo con l'ID dell'utente corrente.
         * 0 = nessun utente loggato.
         * Tutti i ViewModel osservano questo flow per aggiornare le query DB.
         */
        val utenteCorrente = MutableStateFlow(0)
    }

    /** ID dell'utente loggato, null se nessuno è loggato */
    var idUtente: Int?
        get() {
            val id = prefs.getInt("id_utente", -1)
            return if (id == -1) null else id
        }
        set(value) {
            if (value != null) {
                prefs.edit().putInt("id_utente", value).apply()
            } else {
                prefs.edit().remove("id_utente").apply()
            }
            // Aggiorna il flow: tutti i ViewModel reagiranno
            utenteCorrente.value = value ?: 0
        }

    /** Username dell'utente loggato */
    var usernameUtente: String?
        get() = prefs.getString("username_utente", null)
        set(value) {
            if (value != null) prefs.edit().putString("username_utente", value).apply()
            else prefs.edit().remove("username_utente").apply()
        }

    /** True se c'è un utente loggato */
    val isLoggedIn: Boolean get() = idUtente != null

    /** Esegue il logout cancellando tutti i dati di sessione */
    fun logout() {
        prefs.edit().clear().apply()
        // Notifica tutti i ViewModel → le loro query torneranno vuote
        utenteCorrente.value = 0
    }
}
