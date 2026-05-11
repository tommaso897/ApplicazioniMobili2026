package com.example.project2026.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Classi di supporto per evitare errori di compilazione Room
data class CoordinateHeatmap(val latitudine: Double, val longitudine: Double)
data class SpesaVeicolo(val nome: String, val totale: Double)

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

    @Query("SELECT * FROM sessioni_parcheggio WHERE attivo = 1")
    fun ottieniSessioniAttive(): Flow<List<SessioneParcheggio>>

    @Query("SELECT * FROM sessioni_parcheggio ORDER BY inizio DESC")
    fun ottieniCronologia(): Flow<List<SessioneParcheggio>>

    @Query("SELECT * FROM sessioni_parcheggio WHERE attivo = 0 ORDER BY inizio DESC")
    fun ottieniCronologiaTerminate(): Flow<List<SessioneParcheggio>>

    @Query("SELECT * FROM sessioni_parcheggio WHERE idVeicolo = :id AND attivo = 1 LIMIT 1")
    suspend fun ottieniSessioneAttivaPerVeicolo(id: Int): SessioneParcheggio?

    @Query("UPDATE sessioni_parcheggio SET fine = :timestampFine, dataFine = :dataFine, attivo = 0 WHERE idVeicolo = :idVeicolo AND attivo = 1")
    suspend fun terminaSessioneAttivaPerVeicolo(idVeicolo: Int, timestampFine: Long, dataFine: String)

    // Query per HEATMAP (usando la classe di supporto)
    @Query("SELECT latitudine, longitudine FROM sessioni_parcheggio WHERE latitudine IS NOT NULL")
    fun getCoordinatePerHeatMap(): Flow<List<CoordinateHeatmap>>

     // Query per CHART COSTI (usando la classe di supporto e alias espliciti)
     @Query("""
         SELECT v.nome as nome, SUM(s.costo) as totale 
         FROM sessioni_parcheggio s 
         JOIN veicoli v ON s.idVeicolo = v.id 
         WHERE s.costo IS NOT NULL
         GROUP BY s.idVeicolo
     """)
     fun getSpesePerVeicolo(): Flow<List<SpesaVeicolo>>

     // Query per CHART COSTI FILTRATE PER DATA
     @Query("""
         SELECT v.nome as nome, SUM(s.costo) as totale 
         FROM sessioni_parcheggio s 
         JOIN veicoli v ON s.idVeicolo = v.id 
         WHERE s.costo IS NOT NULL AND s.inizio >= :dataInizio AND s.inizio <= :dataFine
         GROUP BY s.idVeicolo
     """)
     fun getSpesePerVeicoloFiltratoPerData(dataInizio: Long, dataFine: Long): Flow<List<SpesaVeicolo>>
}
