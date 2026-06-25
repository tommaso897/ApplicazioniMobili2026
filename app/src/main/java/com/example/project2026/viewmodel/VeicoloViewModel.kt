package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.data.Veicolo
import com.example.project2026.utility.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VeicoloViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).veicoloDao()

    init {
        // Crea SessionManager per sincronizzare SessionManager.utenteCorrente
        // con il valore persistito nelle SharedPreferences
        SessionManager(application)
    }

    // Lista veicoli REATTIVA: quando utenteCorrente cambia (login/logout)
    // il flatMapLatest cancella la query precedente e avvia quella nuova
    val listaVeicoli: StateFlow<List<Veicolo>> = SessionManager.utenteCorrente
        .flatMapLatest { id -> dao.ottieniTuttiIVeicoli(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Aggiunge un veicolo associato all'utente corrente */
    fun aggiungiNuovoVeicolo(nome: String, tipo: TipoVeicolo) {
        viewModelScope.launch {
            val nuovoVeicolo = Veicolo(
                nome = nome,
                tipoVeicolo = tipo,
                idUtente = SessionManager.utenteCorrente.value  // sempre il valore aggiornato
            )
            dao.inserisciVeicolo(nuovoVeicolo)
        }
    }

    fun eliminaVeicolo(veicolo: Veicolo) {
        viewModelScope.launch { dao.cancellaVeicolo(veicolo) }
    }

    fun modificaVeicolo(veicolo: Veicolo) {
        viewModelScope.launch { dao.aggiornaVeicolo(veicolo) }
    }
}
