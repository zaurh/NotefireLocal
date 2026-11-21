package com.zaurh.notefirelocal.data.repository

import com.zaurh.notefirelocal.core.di.ApplicationScope
import com.zaurh.notefirelocal.data.local.NoteDao
import com.zaurh.notefirelocal.data.local.toDomain
import com.zaurh.notefirelocal.data.local.toEntity
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.repository.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class RoomNoteRepository @Inject constructor(
    private val dao: NoteDao,
    @ApplicationScope appScope: CoroutineScope,
) : NoteRepository {

    // Kept hot so the editor can open a note instantly from memory.
    override val notes: StateFlow<List<Note>?> = dao.observeAll()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(appScope, SharingStarted.Eagerly, null)

    override suspend fun getNote(id: Long): Note? = dao.getById(id)?.toDomain()

    override suspend fun saveNote(note: Note): Long {
        if (note.id == 0L) return dao.insert(note.toEntity())
        dao.upsert(note.toEntity())
        return note.id
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) = dao.setPinned(id, pinned)

    override suspend fun deleteNote(id: Long) = dao.deleteById(id)
}
