package com.zaurh.notefirelocal.presentation.notes.list

import androidx.lifecycle.viewModelScope
import com.zaurh.notefirelocal.core.mvi.MviViewModel
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.repository.NoteRepository
import com.zaurh.notefirelocal.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
) : MviViewModel<NotesState, NotesIntent, NotesEffect>(NotesState()) {

    init {
        noteRepository.notes
            .filterNotNull()
            .onEach { notes -> setState { withNotes(notes = notes).copy(isLoading = false) } }
            .launchIn(viewModelScope)

        settingsRepository.settings
            .onEach { settings -> setState { copy(layout = settings.notesLayout, themeMode = settings.themeMode) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.QueryChanged -> setState { withNotes(query = intent.query) }
            NotesIntent.ClearQuery -> setState { withNotes(query = "") }
            NotesIntent.NewNoteClicked -> sendEffect(NotesEffect.OpenNote(0L))
            is NotesIntent.NoteClicked -> sendEffect(NotesEffect.OpenNote(intent.noteId))
            is NotesIntent.TogglePin -> viewModelScope.launch {
                noteRepository.setPinned(intent.note.id, !intent.note.isPinned)
            }
            is NotesIntent.DeleteNote -> delete(intent.noteId)
            is NotesIntent.UndoDelete -> viewModelScope.launch { noteRepository.saveNote(intent.note) }
            NotesIntent.ToggleLayout -> viewModelScope.launch {
                val next = if (currentState.layout == NotesLayout.Grid) NotesLayout.List else NotesLayout.Grid
                settingsRepository.setNotesLayout(next)
            }

            NotesIntent.ThemeClicked -> setState { copy(isThemeDialogVisible = true) }
            NotesIntent.DismissThemeDialog -> setState { copy(isThemeDialogVisible = false) }
            is NotesIntent.ThemeSelected -> {
                setState { copy(isThemeDialogVisible = false) }
                viewModelScope.launch { settingsRepository.setThemeMode(intent.mode) }
            }
            NotesIntent.RateAppClicked -> sendEffect(NotesEffect.OpenPlayStore)
            NotesIntent.ContactClicked -> sendEffect(NotesEffect.EmailDeveloper)
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            // Read from the database, not the list, so the snackbar's "Undo" restores the latest version.
            val note = noteRepository.getNote(id) ?: return@launch
            noteRepository.deleteNote(id)
            sendEffect(NotesEffect.ShowNoteDeleted(note))
        }
    }
}
