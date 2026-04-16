package com.example.project2026.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posizioni_salvate")
data class PosizioneSalvata (
    @PrimaryKey(autoGenerate = true)val id: Int=0,
    @ColumnInfo(name = "nome")val nome: String,
    @ColumnInfo(name = "latitudine")val latitudine: Double,
    @ColumnInfo(name = "longitudine")val longitudine: Double,
)