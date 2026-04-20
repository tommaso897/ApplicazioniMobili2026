package com.example.project2026.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessioni_parcheggio")
data class SessioneParcheggio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "idVeicolo") val idVeicolo: Int,
    @ColumnInfo(name = "tipo") val tipo: TipoParcheggio,
    @Embedded(prefix = "pos_") val posizioneSalvata: PosizioneSalvata? = null,
    @ColumnInfo(name = "latitudine") val latitudine: Double? = null,
    @ColumnInfo(name = "longitudine") val longitudine: Double? = null,
    @ColumnInfo(name = "inizio") val inizio: Long,
    @ColumnInfo(name = "fine") val fine: Long? = null,
    @ColumnInfo(name = "scadenza") val scadenza: Long? = null,
    @ColumnInfo(name = "dataInizio") val dataInizio: String = "",
    @ColumnInfo(name = "dataFine") val dataFine: String? = null,
    @ColumnInfo(name = "costo") val costo: Double? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "foto") val foto: String? = null,
    @ColumnInfo(name = "tariffa") val tariffa: Double? = null,
    @ColumnInfo(name = "stato") val stato: StatoParcheggio,
    @ColumnInfo(name = "attivo") val attivo: Boolean = true
)
