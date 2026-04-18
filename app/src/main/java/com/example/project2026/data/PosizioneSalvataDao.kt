package com.example.project2026.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface PosizioneSalvataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciPosizione(posizione: PosizioneSalvata)

    @Delete
    suspend fun cancellaPosizione(posizione: PosizioneSalvata)

    @Query("SELECT * FROM posizioni_salvate")
    fun ottieniTutteLePosizioni(): Flow<List<PosizioneSalvata>>
}