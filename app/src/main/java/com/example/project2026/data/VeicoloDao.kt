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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli WHERE idUtente = :idUtente")
    fun ottieniTuttiIVeicoli(idUtente: Int): Flow<List<Veicolo>>

    @Delete
    suspend fun cancellaVeicolo(veicolo: Veicolo)

    @Query("SELECT * FROM veicoli WHERE id = :id")
    suspend fun ottieniVeicoloPerId(id: Int): Veicolo?

    @Update
    suspend fun aggiornaVeicolo(veicolo: Veicolo)
}