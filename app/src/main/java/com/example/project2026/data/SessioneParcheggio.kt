package com.example.project2026.data

data class SessioneParcheggio(
    val id: Int=0,
    val idVeicolo: Int,
    val tipo: TipoParcheggio,
    val posizione: PosizioneSalvata,
    val tipoParcheggio: TipoParcheggio,
    val inizio: Long,
    val fine: Long? = null,
    val dataInizio: String,
    val dataFine: String? = null,
    val costo: Double? = null,
    val note: String? = null,
    val foto: String? = null
)
