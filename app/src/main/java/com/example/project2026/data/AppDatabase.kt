package com.example.project2026.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Qui elenchiamo tutte le tabelle (Entity) del database
@Database(
    entities = [Veicolo::class, PosizioneSalvata::class, SessioneParcheggio::class],
    version = 1,
    exportSchema = false
)
// 2. Se abbiamo dei convertitori (es. per le date o gli enum), li mettiamo qui
// @TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 3. Colleghiamo i DAO
    abstract fun veicoloDao(): VeicoloDao
    abstract fun posizioneSalvataDao(): PosizioneSalvataDao
    abstract fun sessioneParcheggioDao(): SessioneParcheggioDao

    // 4. Il "Singleton": garantisce che esista una sola istanza del database in tutta l'app
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Se l'istanza esiste già, la restituiamo. Altrimenti lo creiamo.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parkmate_database" // Questo sarà il nome del file sul telefono
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}