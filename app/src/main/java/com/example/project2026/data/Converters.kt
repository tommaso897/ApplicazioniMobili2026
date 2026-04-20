package com.example.project2026.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTipoParcheggio(value: TipoParcheggio): String {
        return value.name
    }

    @TypeConverter
    fun toTipoParcheggio(value: String): TipoParcheggio {
        return TipoParcheggio.valueOf(value)
    }

    @TypeConverter
    fun fromStatoParcheggio(value: StatoParcheggio): String {
        return value.name
    }

    @TypeConverter
    fun toStatoParcheggio(value: String): StatoParcheggio {
        return StatoParcheggio.valueOf(value)
    }
}
