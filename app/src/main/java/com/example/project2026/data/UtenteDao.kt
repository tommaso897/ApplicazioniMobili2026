package com.example.project2026.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UtenteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registra(utente: Utente): Long

    @Query("SELECT * FROM utenti WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): Utente?

    @Query("SELECT * FROM utenti WHERE username = :username LIMIT 1")
    suspend fun trovaPerId(username: String): Utente?

    @Query("SELECT * FROM utenti WHERE id = :id LIMIT 1")
    suspend fun ottieniPerId(id: Int): Utente?
}
