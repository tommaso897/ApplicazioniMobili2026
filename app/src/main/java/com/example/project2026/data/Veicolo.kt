package com.example.project2026.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "veicoli")
data class Veicolo(
    @PrimaryKey(autoGenerate = true)val id: Int=0,
    @ColumnInfo(name = "nome")val nome: String="",
    @ColumnInfo(name = "tipoVeicolo")val tipoVeicolo: TipoVeicolo,
    @ColumnInfo(name = "statoParcheggio")val statoParcheggio: StatoParcheggio = StatoParcheggio.LIBERO
)