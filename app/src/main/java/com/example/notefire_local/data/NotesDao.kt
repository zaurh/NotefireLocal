package com.example.notefire_local.data

import androidx.room.*
import com.example.notefire_local.data.local.NotesEntity

@Dao
interface NotesDao {
    @Insert
    suspend fun insert(note: NotesEntity)

    @Delete
    suspend fun delete(note: NotesEntity)

    @Query("SELECT * FROM notes")
    suspend fun allNotes(): List<NotesEntity>

    @Query("SELECT * FROM notes WHERE note_title || note_description like '%' || :query || '%'")
    suspend fun searchNote(query: String): List<NotesEntity>
}