package com.zaurh.notefirelocal.domain.repository

import com.zaurh.notefirelocal.domain.model.Note
import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    /** All notes, pinned first, then most recently edited. Null until the first load finishes. */
    val notes: StateFlow<List<Note>?>

    suspend fun getNote(id: Long): Note?

    /** Inserts when [Note.id] is 0, otherwise inserts-or-replaces. Returns the note's id. */
    suspend fun saveNote(note: Note): Long

    suspend fun setPinned(id: Long, pinned: Boolean)

    suspend fun deleteNote(id: Long)
}
