package com.example.notefire_local.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notefire_local.data.local.NotesEntity

@Database(entities = [NotesEntity::class], version = 4)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
}