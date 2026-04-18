package com.example.project2026.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.Repository
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.data.Veicolo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VeicoloViewModel(private val repository: Repository) : ViewModel() {
    // Trasformiamo il Flow del database in uno StateFlow che la UI può leggere facilmente
    val listaVeicoli: StateFlow<List<Veicolo>> = repository.tuttiIVeicoli.stateIn(
        scope=viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Funzione per aggiungere un veicolo
    fun aggingiNuovoVeicolo(nome: String, tipo: TipoVeicolo) {
        val nuovoVeicolo = Veicolo(nome = nome, tipoVeicolo = tipo)
        viewModelScope.launch {
            repository.inserisciVeicolo(nuovoVeicolo)
        }
    }

    // Funzione per cancellare un veicolo
    fun eliminaVeicolo(veicolo: Veicolo) {
        viewModelScope.launch {
            repository.cancellaVeicolo(veicolo)
        }
    }

}