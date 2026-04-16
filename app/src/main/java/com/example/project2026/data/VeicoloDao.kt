package com.example.project2026.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VeicoloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Se esiste già, sovrascrivi
    suspend fun inserisciVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli")
    fun ottieniTuttiIVeicoli(): Flow<List<Veicolo>> // Flow permette l'aggiornamento automatico della UI

    @Delete
    suspend fun cancelloVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli WHERE id = :id")
    suspend fun ottieniVeicoloPerId(id: Int): Veicolo?
}