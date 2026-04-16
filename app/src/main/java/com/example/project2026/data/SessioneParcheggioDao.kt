package com.example.project2026.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

interface SessioneParcheggioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvaSessione(sessione: SessioneParcheggio)

    @Delete
    suspend fun eliminaSessione(sessione: SessioneParcheggio)

    @Query("SELECT * FROM sessioni_parcheggio")
    fun ottieniTutteLeSessioni(): Flow<List<SessioneParcheggio>>

    @Query("SELECT * FROM sessioni_parcheggio WHERE id = :id")
    suspend fun ottieniSessionePerId(id: Int): SessioneParcheggio?

    //solo i veicoli attualmente parcheggiati
    @Query("SELECT * FROM sessioni_parcheggio WHERE fine IS NULL")
    fun ottieniSessioniAttive(): Flow<List<SessioneParcheggio>>

    //cronologia ordinata
    @Query("SELECT * FROM sessioni_parcheggio ORDER BY inizio DESC")
    fun ottieniCronologia(): Flow<List<SessioneParcheggio>>

    //sessione attiva per determinato veicolo
    @Query("SELECT * FROM sessioni_parcheggio WHERE idVeicolo = :id AND fine IS NULL lIMIT 1")
    suspend fun ottieniSessioneAttivaPerVeicolo(id: Int): SessioneParcheggio?

    // filtrare per veicolo e intervallo di tempo
    @Query(""" SELECT * FROM sessioni_parcheggio
              WHERE idVeicolo = :id 
              AND ((inizio >= :startTime AND inizio <= :endTime) 
                   OR (fine IS NOT NULL AND fine >= :startTime AND fine <= :endTime))""")
    fun ottieniSessioniPerVeicoloEIntervallo(id: Int, startTime: Long, endTime: Long): Flow<List<SessioneParcheggio>>
}