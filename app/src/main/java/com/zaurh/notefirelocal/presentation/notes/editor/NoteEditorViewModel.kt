package com.zaurh.notefirelocal.presentation.notes.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zaurh.notefirelocal.R
import com.zaurh.notefirelocal.core.Clock
import com.zaurh.notefirelocal.core.di.ApplicationScope
import com.zaurh.notefirelocal.core.mvi.MviViewModel
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.TextFormat
import com.zaurh.notefirelocal.domain.repository.NoteRepository
import com.zaurh.notefirelocal.presentation.navigation.NoteEditorRoute
import com.zaurh.notefirelocal.presentation.navigation.noteSharedKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Notes save themselves: text and font size are written shortly after the user stops, other
 * formatting and pin changes right away,
 * and anything pending is flushed when the screen pauses or closes. A note left empty is removed.
 */
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
    private val clock: Clock,
    @ApplicationScope private val appScope: CoroutineScope,
) : MviViewModel<NoteEditorState, NoteEditorIntent, NoteEditorEffect>(initialState(savedStateHandle)) {

    /** What is currently stored in the database, or null if nothing is yet. */
    private var lastSaved: Note? = null
    private val saveMutex = Mutex()
    private var saveJob: Job? = null
    private var isDeleted = false
    private var isClosing = false

    init {
        val id = currentState.noteId
        if (id != 0L) {
            // Usually already in memory, so the editor opens with no loading flash.
            val cached = repository.notes.value?.firstOrNull { it.id == id }
            if (cached != null) onLoaded(cached) else load(id)
        }
    }

    override fun onIntent(intent: NoteEditorIntent) {
        when (intent) {
            is NoteEditorIntent.TitleChanged -> {
                savedStateHandle[KEY_TITLE] = intent.value
                setState { copy(title = intent.value) }
                scheduleSave(TYPING_DEBOUNCE_MS)
            }
            is NoteEditorIntent.ContentChanged -> {
                savedStateHandle[KEY_CONTENT] = intent.value
                setState { copy(content = intent.value) }
                scheduleSave(TYPING_DEBOUNCE_MS)
            }
            NoteEditorIntent.TogglePin -> {
                setState { copy(isPinned = !isPinned) }
                scheduleSave(0)
            }

            is NoteEditorIntent.TogglePanel -> setState {
                copy(openPanel = if (openPanel == intent.panel) null else intent.panel)
            }
            is NoteEditorIntent.FieldFocused -> setState { copy(formatTarget = intent.target) }
            is NoteEditorIntent.SelectFormatTarget -> setState { copy(formatTarget = intent.target) }
            NoteEditorIntent.ToggleBold -> updateFormat { copy(bold = !bold) }
            NoteEditorIntent.ToggleItalic -> updateFormat { copy(italic = !italic) }
            NoteEditorIntent.ToggleUnderline -> updateFormat { copy(underline = !underline) }
            NoteEditorIntent.ToggleLineThrough -> updateFormat { copy(lineThrough = !lineThrough) }
            is NoteEditorIntent.FontSizeChanged -> updateFormat(saveDelayMs = TYPING_DEBOUNCE_MS) {
                copy(fontSize = intent.size.coerceIn(TextFormat.MIN_FONT_SIZE, TextFormat.MAX_FONT_SIZE))
            }
            is NoteEditorIntent.TextColorSelected -> updateFormat { copy(color = intent.argb) }
            is NoteEditorIntent.BackgroundSelected -> {
                setState { copy(backgroundColor = intent.argb) }
                scheduleSave(0)
            }
            is NoteEditorIntent.OpenColorWheel -> setState { copy(colorWheel = intent.target) }
            NoteEditorIntent.DismissColorWheel -> setState { copy(colorWheel = null) }
            is NoteEditorIntent.ColorWheelPicked -> {
                val target = currentState.colorWheel ?: return
                setState { copy(colorWheel = null) }
                when (target) {
                    ColorWheelTarget.Background -> onIntent(NoteEditorIntent.BackgroundSelected(intent.argb))
                    ColorWheelTarget.TextColor -> onIntent(NoteEditorIntent.TextColorSelected(intent.argb))
                }
            }

            NoteEditorIntent.Share -> if (currentState.hasText) {
                sendEffect(NoteEditorEffect.Share(currentState.toNote().toShareText()))
            }
            NoteEditorIntent.Delete -> delete()
            NoteEditorIntent.Close -> close()
            NoteEditorIntent.ScreenPaused -> {
                saveJob?.cancel()
                appScope.launch { persist(isFinal = false) }
            }
        }
    }

    override fun onCleared() {
        saveJob?.cancel()
        // The screen is gone (back pressed or closed): write the final version and drop empty notes.
        appScope.launch { persist(isFinal = true) }
    }

    private fun load(id: Long) {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val note = try {
                repository.getNote(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (note == null) {
                sendEffect(NoteEditorEffect.ShowError(R.string.note_not_found))
                isDeleted = true // nothing to save
                closeOnce(NoteEditorEffect.Close)
            } else {
                onLoaded(note)
            }
        }
    }

    private fun onLoaded(note: Note) {
        lastSaved = note
        // A draft restored after process death wins over the stored note.
        val draftTitle = savedStateHandle.get<String>(KEY_TITLE)
        val draftContent = savedStateHandle.get<String>(KEY_CONTENT)
        setState {
            copy(
                isLoading = false,
                title = draftTitle ?: note.title,
                content = draftContent ?: note.content,
                backgroundColor = note.backgroundColor,
                titleFormat = note.titleFormat,
                contentFormat = note.contentFormat,
                isPinned = note.isPinned,
                updatedAt = note.updatedAt,
            )
        }
        if (draftTitle != null || draftContent != null) scheduleSave(0)
    }

    /** Changes the format of the part being styled (title or text) and saves it. */
    private fun updateFormat(saveDelayMs: Long = 0, transform: TextFormat.() -> TextFormat) {
        setState {
            when (formatTarget) {
                FormatTarget.Title -> copy(titleFormat = titleFormat.transform())
                FormatTarget.Content -> copy(contentFormat = contentFormat.transform())
            }
        }
        scheduleSave(saveDelayMs)
    }

    private fun scheduleSave(delayMs: Long) {
        if (isDeleted) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(delayMs)
            persist(isFinal = false)
        }
    }

    private fun close() {
        if (isClosing) return
        saveJob?.cancel()
        viewModelScope.launch {
            persist(isFinal = true)
            closeOnce(NoteEditorEffect.Close)
        }
    }

    private fun delete() {
        if (isClosing) return
        saveJob?.cancel()
        viewModelScope.launch {
            // Wait for any write in flight so it can't bring the note back after the delete.
            saveMutex.withLock { isDeleted = true }
            val id = lastSaved?.id ?: currentState.noteId
            closeOnce(if (id != 0L) NoteEditorEffect.CloseAndDelete(id) else NoteEditorEffect.Close)
        }
    }

    private fun closeOnce(effect: NoteEditorEffect) {
        if (isClosing) return
        isClosing = true
        sendEffect(effect)
    }

    /**
     * Writes the current state if it differs from what is stored. Blank notes are not written;
     * when [isFinal], a stored note that became blank is deleted.
     */
    private suspend fun persist(isFinal: Boolean) = saveMutex.withLock {
        withContext(NonCancellable) {
            if (isDeleted || currentState.isLoading) return@withContext
            val draft = currentState.toNote()
            val before = lastSaved
            if (draft.isBlank) {
                if (isFinal && before != null) {
                    runCatching { repository.deleteNote(before.id) }
                    lastSaved = null
                }
                return@withContext
            }
            if (before != null && draft.sameUserContentAs(before)) return@withContext

            // Pinning isn't an edit, so it keeps the "edited" time; any other change updates it.
            val onlyPinChanged = before != null && draft.copy(isPinned = before.isPinned).sameUserContentAs(before)
            val now = clock.now()
            val toSave = draft.copy(
                createdAt = before?.createdAt ?: now,
                updatedAt = if (before != null && onlyPinChanged) before.updatedAt else now,
            )
            try {
                val id = repository.saveNote(toSave)
                lastSaved = toSave.copy(id = id)
                savedStateHandle[KEY_ID] = id
                savedStateHandle.remove<String>(KEY_TITLE)
                savedStateHandle.remove<String>(KEY_CONTENT)
                setState { copy(noteId = id, updatedAt = toSave.updatedAt) }
            } catch (e: Exception) {
                sendEffect(NoteEditorEffect.ShowError(R.string.note_save_failed))
            }
        }
    }

    private fun NoteEditorState.toNote() = Note(
        id = lastSaved?.id ?: noteId,
        title = title,
        content = content,
        backgroundColor = backgroundColor,
        titleFormat = titleFormat,
        contentFormat = contentFormat,
        isPinned = isPinned,
    )

    /** Same everything the user can change (ignores id and timestamps). */
    private fun Note.sameUserContentAs(other: Note) =
        copy(id = 0, createdAt = 0, updatedAt = 0) == other.copy(id = 0, createdAt = 0, updatedAt = 0)

    companion object {
        const val TYPING_DEBOUNCE_MS = 400L

        private const val KEY_ID = "saved_note_id"
        private const val KEY_TITLE = "draft_title"
        private const val KEY_CONTENT = "draft_content"

        private fun initialState(handle: SavedStateHandle): NoteEditorState {
            val routeId = NoteEditorRoute.from(handle).noteId
            // After process death a new note may already have been saved under a real id.
            val id = handle.get<Long>(KEY_ID) ?: routeId
            return NoteEditorState(
                noteId = id,
                sharedKey = noteSharedKey(routeId),
                isNew = routeId == 0L,
                title = if (id == 0L) handle.get<String>(KEY_TITLE).orEmpty() else "",
                content = if (id == 0L) handle.get<String>(KEY_CONTENT).orEmpty() else "",
            )
        }
    }
}
