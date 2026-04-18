package com.example.project2026

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.Repository

class ParkMateApplication : Application() {
    // Usiamo 'lazy' così il database viene creato solo quando serve davvero
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        Repository(
            database.veicoloDao(),
            database.posizioneSalvataDao(),
            database.sessioneParcheggioDao()
        )
    }
}