package com.example.random.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.random.data.local.NotesEntity

@Database(entities = [NotesEntity::class], version = 4)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}