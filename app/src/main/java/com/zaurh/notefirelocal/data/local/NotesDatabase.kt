package com.zaurh.notefirelocal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version history:
 *  1 — app 1.0.x: rich-text styling columns, HSV background color, time stored as text.
 *  2 — app 2.0: palette color, pinning, numeric created/updated timestamps. See [MIGRATION_1_2].
 */
@Database(entities = [NoteEntity::class], version = 2, exportSchema = true)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val NAME = "notefire_local"
    }
}
