package com.example.random.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.random.domain.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repository: Repository
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

    fun addNote(title: String, description: String) {
        viewModelScope.launch {
            repository.addNote(title, description)
        }
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

