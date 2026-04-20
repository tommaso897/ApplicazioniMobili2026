package com.example.project2026.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VeicoloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Se esiste già, sovrascrivi
    suspend fun inserisciVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli")
    fun ottieniTuttiIVeicoli(): Flow<List<Veicolo>> // Flow permette l'aggiornamento automatico della UI

    @Delete
    suspend fun cancellaVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli WHERE id = :id")
    suspend fun ottieniVeicoloPerId(id: Int): Veicolo?

    @Update
    suspend fun aggiornaVeicolo(veicolo: Veicolo)
}