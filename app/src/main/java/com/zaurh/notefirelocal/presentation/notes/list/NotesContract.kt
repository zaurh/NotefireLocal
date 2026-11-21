package com.zaurh.notefirelocal.presentation.notes.list

import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode

data class NotesState(
    val isLoading: Boolean = true,
    val allNotes: List<Note> = emptyList(),
    val query: String = "",
    val pinnedNotes: List<Note> = emptyList(),
    val otherNotes: List<Note> = emptyList(),
    val layout: NotesLayout = NotesLayout.Grid,
    val themeMode: ThemeMode = ThemeMode.System,
    val isThemeDialogVisible: Boolean = false,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val isEmpty: Boolean get() = pinnedNotes.isEmpty() && otherNotes.isEmpty()

    /** Re-derives the visible sections whenever the notes or the query change. */
    fun withNotes(notes: List<Note> = allNotes, query: String = this.query): NotesState {
        val visible = notes.filter { it.matches(query) }
        return copy(
            allNotes = notes,
            query = query,
            pinnedNotes = visible.filter { it.isPinned },
            otherNotes = visible.filterNot { it.isPinned },
        )
    }
}

sealed interface NotesIntent {
    data class QueryChanged(val query: String) : NotesIntent
    data object ClearQuery : NotesIntent
    data object NewNoteClicked : NotesIntent
    data class NoteClicked(val noteId: Long) : NotesIntent
    data class TogglePin(val note: Note) : NotesIntent
    data class DeleteNote(val noteId: Long) : NotesIntent
    data class UndoDelete(val note: Note) : NotesIntent
    data object ToggleLayout : NotesIntent

    data object ThemeClicked : NotesIntent
    data class ThemeSelected(val mode: ThemeMode) : NotesIntent
    data object DismissThemeDialog : NotesIntent
    data object RateAppClicked : NotesIntent
    data object ContactClicked : NotesIntent
}

sealed interface NotesEffect {
    /** [noteId] is 0 for a new note. */
    data class OpenNote(val noteId: Long) : NotesEffect
    data class ShowNoteDeleted(val note: Note) : NotesEffect
    data object OpenPlayStore : NotesEffect
    data object EmailDeveloper : NotesEffect
}
