package com.example.project2026

import android.app.Application
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.Repository
import com.example.project2026.worker.SostaBackgroundWorker

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

    override fun onCreate() {
        super.onCreate()
        // Avvia il monitoraggio periodico in background tramite WorkManager.
        // Questo garantisce che le sessioni attive vengano controllate
        // anche se l'utente non apre mai l'app dopo l'avvio.
        SostaBackgroundWorker.avviaMonitoraggioPeriodico(this)
    }
}