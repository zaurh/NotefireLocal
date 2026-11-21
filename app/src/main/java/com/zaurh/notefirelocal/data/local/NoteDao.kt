package com.zaurh.notefirelocal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE note_id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET is_pinned = :pinned WHERE note_id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM notes WHERE note_id = :id")
    suspend fun deleteById(id: Long)
}
