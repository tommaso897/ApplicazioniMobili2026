package com.example.project2026.ui

sealed class Destinazione(val rotta: String) {
    object ListaVeicoli : Destinazione("lista_veicoli")
    object AggiungiVeicolo : Destinazione("aggiungi_veicolo")
    object Mappa : Destinazione("mappa")
    object Cronologia : Destinazione("history")
    object Statistiche : Destinazione("stats")
}