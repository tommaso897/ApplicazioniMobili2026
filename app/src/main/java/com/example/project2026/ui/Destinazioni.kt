package com.example.project2026.ui

sealed class Destinazione(val rotta: String) {
    object ListaVeicoli : Destinazione("lista_veicoli")
    object AggiungiVeicolo : Destinazione("aggiungi_veicolo")
    object ModificaVeicolo : Destinazione("modifica_veicolo/{veicoloId}")
    object Home : Destinazione("home")
    object Cronologia : Destinazione("history")
    object Statistiche : Destinazione("stats")
    object PosizioniSalvate : Destinazione("posizioni_salvate")
}
