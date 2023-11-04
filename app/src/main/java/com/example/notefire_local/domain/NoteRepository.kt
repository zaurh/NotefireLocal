package com.example.notefire_local.domain

import androidx.lifecycle.MutableLiveData
import com.example.notefire_local.data.NotesDao
import com.example.notefire_local.data.local.NotesEntity
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val dao: NotesDao
) {
    var noteData = MutableLiveData<List<NotesEntity>>()

    init {
        noteData = MutableLiveData()
    }

    suspend fun getNotes() {
        noteData.value = dao.allNotes()
    }

    suspend fun addNote(noteData : NotesEntity) {
        dao.insert(noteData)
    }

    suspend fun deleteNote(noteId: Int) {
        val noteDeletion = NotesEntity(
            noteId = noteId
        )
        dao.delete(noteDeletion)
    }

    suspend fun searchNote(query: String) {
        noteData.value = dao.searchNote(query)
    }
}