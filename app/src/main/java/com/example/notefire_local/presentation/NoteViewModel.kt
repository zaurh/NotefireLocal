package com.example.notefire_local.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notefire_local.data.local.NotesEntity
import com.example.notefire_local.domain.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    val noteData = repository.noteData

    init {
        getNotes()
    }

    private fun getNotes() {
        viewModelScope.launch {
            repository.getNotes()
        }
    }

    fun addNote(noteData: NotesEntity) {
        viewModelScope.launch {
            repository.addNote(noteData)
        }
        getNotes()
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            getNotes()
        }
    }

    fun searchNote(query: String) {
        viewModelScope.launch {
            repository.searchNote(query)
        }
    }

    fun clearSearch() {
        getNotes()
    }

}

