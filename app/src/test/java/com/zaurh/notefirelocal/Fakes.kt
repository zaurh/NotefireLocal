package com.zaurh.notefirelocal

import com.zaurh.notefirelocal.core.Clock
import com.zaurh.notefirelocal.domain.model.AppSettings
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode
import com.zaurh.notefirelocal.domain.repository.NoteRepository
import com.zaurh.notefirelocal.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeNoteRepository(initial: List<Note> = emptyList()) : NoteRepository {
    private val _notes = MutableStateFlow<List<Note>?>(sort(initial))
    override val notes: StateFlow<List<Note>?> = _notes

    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1
    var saveCount = 0
        private set
    val deletedIds = mutableListOf<Long>()

    val all: List<Note> get() = _notes.value.orEmpty()
    fun byId(id: Long): Note? = all.firstOrNull { it.id == id }

    override suspend fun getNote(id: Long): Note? = byId(id)

    override suspend fun saveNote(note: Note): Long {
        saveCount++
        val saved = if (note.id == 0L) note.copy(id = nextId++) else note
        _notes.update { list -> sort(list.orEmpty().filterNot { it.id == saved.id } + saved) }
        return saved.id
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        _notes.update { list -> sort(list.orEmpty().map { if (it.id == id) it.copy(isPinned = pinned) else it }) }
    }

    override suspend fun deleteNote(id: Long) {
        deletedIds += id
        _notes.update { list -> list.orEmpty().filterNot { it.id == id } }
    }

    private fun sort(list: List<Note>) =
        list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
}

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = state
    val current: AppSettings get() = state.value

    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setNotesLayout(layout: NotesLayout) = state.update { it.copy(notesLayout = layout) }
}

class FakeClock(var time: Long = 1_000L) : Clock {
    override fun now(): Long = time
}
