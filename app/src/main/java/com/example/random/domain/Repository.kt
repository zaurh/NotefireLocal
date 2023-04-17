package com.example.random.domain

import androidx.lifecycle.MutableLiveData
import com.example.random.data.NotesDao
import com.example.random.data.local.NotesEntity
import javax.inject.Inject

class Repository @Inject constructor(
    private val dao: NotesDao
) {
    var noteData = MutableLiveData<List<NotesEntity>>()

    init {
        noteData = MutableLiveData()
    }

    suspend fun getNotes() {
        noteData.value = dao.allNotes()
    }

    suspend fun addNote(title: String, description: String) {
        val newNote = NotesEntity(
            noteTitle = title,
            noteDescription = description,
            date = System.currentTimeMillis().toString()
        )
        dao.insert(newNote)
    }

    suspend fun deleteNote(noteId: Int) {
        val noteDeletion = NotesEntity(
            noteId = noteId,
            date = System.currentTimeMillis().toString()
        )
        dao.delete(noteDeletion)
    }

    suspend fun searchNote(query: String) {
        noteData.value = dao.searchNote(query)
    }
}