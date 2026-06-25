package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.PosizioneSalvata
import com.example.project2026.geofence.GeofenceManager
import com.example.project2026.utility.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PosizioneSalvataViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).posizioneSalvataDao()
    private val geofenceManager = GeofenceManager(application)

    init {
        SessionManager(application)
    }

    // Lista posizioni REATTIVA: si aggiorna automaticamente al login/logout
    val tutteLePosizioni: StateFlow<List<PosizioneSalvata>> = SessionManager.utenteCorrente
        .flatMapLatest { id -> dao.ottieniTutteLePosizioni(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Salva o aggiorna una posizione associata all'utente corrente.
     */
    fun salvaPosizione(nome: String, lat: Double, lng: Double, id: Int = 0) {
        viewModelScope.launch {
            val posizione = PosizioneSalvata(
                id = id,
                idUtente = SessionManager.utenteCorrente.value,
                nome = nome,
                latitudine = lat,
                longitudine = lng
            )
            dao.inserisciPosizione(posizione)
            geofenceManager.aggiungiGeofence(posizione)
        }
    }

    fun eliminaPosizione(posizione: PosizioneSalvata) {
        viewModelScope.launch {
            dao.cancellaPosizione(posizione)
            geofenceManager.rimuoviGeofence(posizione.id)
        }
    }
}
