package com.example.project2026.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessioneParcheggioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvaSessione(sessione: SessioneParcheggio)

    @Update
    suspend fun aggiornaSessione(sessione: SessioneParcheggio)

    @Delete
    suspend fun eliminaSessione(sessione: SessioneParcheggio)

    @Query("SELECT * FROM sessioni_parcheggio")
    fun ottieniTutteLeSessioni(): Flow<List<SessioneParcheggio>>

    @Query("SELECT * FROM sessioni_parcheggio WHERE id = :id")
    suspend fun ottieniSessionePerId(id: Int): SessioneParcheggio?

    // Solo i veicoli attualmente parcheggiati (attivo = 1)
    @Query("SELECT * FROM sessioni_parcheggio WHERE attivo = 1")
    fun ottieniSessioniAttive(): Flow<List<SessioneParcheggio>>

    // Cronologia ordinata
    @Query("SELECT * FROM sessioni_parcheggio ORDER BY inizio DESC")
    fun ottieniCronologia(): Flow<List<SessioneParcheggio>>

    // Sessione attiva per determinato veicolo
    @Query("SELECT * FROM sessioni_parcheggio WHERE idVeicolo = :id AND attivo = 1 LIMIT 1")
    suspend fun ottieniSessioneAttivaPerVeicolo(id: Int): SessioneParcheggio?

    // Termina la sessione attiva per un veicolo
    @Query("UPDATE sessioni_parcheggio SET fine = :timestampFine, dataFine = :dataFine, attivo = 0 WHERE idVeicolo = :idVeicolo AND attivo = 1")
    suspend fun terminaSessioneAttivaPerVeicolo(idVeicolo: Int, timestampFine: Long, dataFine: String)

    // Filtrare per veicolo e intervallo di tempo
    @Query(""" SELECT * FROM sessioni_parcheggio
              WHERE idVeicolo = :id 
              AND ((inizio >= :startTime AND inizio <= :endTime) 
                   OR (fine IS NOT NULL AND fine >= :startTime AND fine <= :endTime))""")
    fun ottieniSessioniPerVeicoloEIntervallo(id: Int, startTime: Long, endTime: Long): Flow<List<SessioneParcheggio>>
}
