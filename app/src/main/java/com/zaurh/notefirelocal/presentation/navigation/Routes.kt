package com.zaurh.notefirelocal.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.Serializable

// Type-safe navigation destinations.
//
// ViewModels read arguments with `from(handle)` instead of `toRoute()`: Navigation stores each
// argument in the SavedStateHandle under its property name, and reading it directly keeps
// ViewModels unit-testable on the plain JVM.

@Serializable data object NotesRoute

/** [noteId] is 0 for a brand-new note. */
@Serializable
data class NoteEditorRoute(val noteId: Long = 0L) {
    companion object {
        fun from(handle: SavedStateHandle) = NoteEditorRoute(handle.get<Long>("noteId") ?: 0L)
    }
}
