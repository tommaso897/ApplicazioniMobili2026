package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.PosizioneSalvata
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PosizioneSalvataViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).posizioneSalvataDao()

    val tutteLePosizioni: StateFlow<List<PosizioneSalvata>> = dao.ottieniTutteLePosizioni()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Salva o aggiorna una posizione.
     * Se l'id è 0 (default), Room ne genererà uno nuovo (Insert).
     * Se l'id è già presente, Room sovrascriverà i dati (Update via REPLACE).
     */
    fun salvaPosizione(nome: String, lat: Double, lng: Double, id: Int = 0) {
        viewModelScope.launch {
            dao.inserisciPosizione(
                PosizioneSalvata(
                    id = id,
                    nome = nome,
                    latitudine = lat,
                    longitudine = lng
                )
            )
        }
    }

    fun eliminaPosizione(posizione: PosizioneSalvata) {
        viewModelScope.launch {
            dao.cancellaPosizione(posizione)
        }
    }
}
