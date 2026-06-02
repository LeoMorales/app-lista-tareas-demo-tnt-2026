package com.example.demolistatareas.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.demolistatareas.data.db.dao.TareaDao
import com.example.demolistatareas.data.db.entity.TareaEntity

@Database(entities = [TareaEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
}