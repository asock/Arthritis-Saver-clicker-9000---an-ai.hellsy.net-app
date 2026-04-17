package com.assclk9000.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile

@Database(
    entities = [ClickProfile::class, ClickAction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}
