package com.example.random.data.local

import androidx.room.*

@Entity(tableName = "notes")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id") val noteId: Int? = null,
    @ColumnInfo(name = "note_title") val noteTitle: String? = null,
    @ColumnInfo(name = "note_description") val noteDescription: String? = null,
    @ColumnInfo(name = "time") val date: String
)
