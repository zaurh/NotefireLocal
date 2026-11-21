package com.zaurh.notefirelocal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zaurh.notefirelocal.data.local.MIGRATION_1_2
import com.zaurh.notefirelocal.data.local.toEntity
import com.zaurh.notefirelocal.data.local.NotesDatabase
import com.zaurh.notefirelocal.data.local.toDomain
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.TextFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Opens a database written by app 1.0.x with the current app, as a Play Store update would. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test"

    @Before
    @After
    fun deleteDb() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun notesFromVersion1KeepTextDatesAndFormatting() = runTest {
        createVersion1Database(
            // Plain note with all the old defaults.
            V1Note(id = 1, title = "Shopping", description = "milk\neggs", time = "1700000000000"),
            // Fully styled note on a picked background.
            V1Note(
                id = 4, title = "Styled", description = "fancy", background = 0xFF4CAF50.toInt(),
                titleColor = 0xFFFFFFFF.toInt(), noteColor = 0xFF000000.toInt(),
                titleSize = 44f, noteSize = 12.5f,
                titleItalic = true, titleBold = false, titleUnderline = true,
                noteBold = true, noteLineThrough = true, time = "1710000000000",
            ),
            // Red text on the default background, unreadable time.
            V1Note(id = 9, title = "Ideas", description = "", noteColor = 0xFFD32F2F.toInt(), time = "not a number"),
        )

        val db = Room.databaseBuilder(context, NotesDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            val notes = db.noteDao().observeAll().first().map { it.toDomain() }.associateBy { it.id }
            assertEquals(setOf(1L, 4L, 9L), notes.keys)

            assertEquals(
                Note(
                    id = 1, title = "Shopping", content = "milk\neggs", backgroundColor = null,
                    titleFormat = TextFormat(color = null, fontSize = 20f, bold = true),
                    contentFormat = TextFormat(color = null, fontSize = 16f),
                    createdAt = 1700000000000, updatedAt = 1700000000000,
                ),
                notes.getValue(1),
            )
            assertEquals(
                Note(
                    id = 4, title = "Styled", content = "fancy", backgroundColor = 0xFF4CAF50.toInt(),
                    titleFormat = TextFormat(0xFFFFFFFF.toInt(), 44f, bold = false, italic = true, underline = true),
                    contentFormat = TextFormat(0xFF000000.toInt(), 12.5f, bold = true, lineThrough = true),
                    createdAt = 1710000000000, updatedAt = 1710000000000,
                ),
                notes.getValue(4),
            )
            val ideas = notes.getValue(9)
            assertEquals(0xFFD32F2F.toInt(), ideas.contentFormat.color)
            assertEquals(null, ideas.titleFormat.color)
            assertTrue(ideas.updatedAt > 0)

            // New notes continue after the highest old id.
            val newId = db.noteDao().insert(Note(title = "New").toEntity())
            assertEquals(10L, newId)
        } finally {
            db.close()
        }
    }

    private data class V1Note(
        val id: Int,
        val title: String,
        val description: String,
        val background: Int = 0xFFFFFFFF.toInt(),
        val titleColor: Int = 0xFF000000.toInt(),
        val noteColor: Int = 0xFF000000.toInt(),
        val titleSize: Float = 20f,
        val noteSize: Float = 16f,
        val titleItalic: Boolean = false,
        val noteItalic: Boolean = false,
        val titleBold: Boolean = true,
        val noteBold: Boolean = false,
        val titleUnderline: Boolean = false,
        val noteUnderline: Boolean = false,
        val titleLineThrough: Boolean = false,
        val noteLineThrough: Boolean = false,
        val time: String,
    )

    /** The exact table Room 2.6.1 created for the 1.0.x NotesEntity. */
    private fun createVersion1Database(vararg notes: V1Note) {
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `notes` (`note_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`note_title` TEXT NOT NULL, `note_description` TEXT NOT NULL, " +
                    "`background_color` INTEGER NOT NULL, `title_color` INTEGER NOT NULL, `note_color` INTEGER NOT NULL, " +
                    "`title_font_size` REAL NOT NULL, `note_font_size` REAL NOT NULL, " +
                    "`title_italic` INTEGER NOT NULL, `note_italic` INTEGER NOT NULL, " +
                    "`title_bold` INTEGER NOT NULL, `note_bold` INTEGER NOT NULL, " +
                    "`title_underline` INTEGER NOT NULL, `note_underline` INTEGER NOT NULL, " +
                    "`title_line_through` INTEGER NOT NULL, `note_line_through` INTEGER NOT NULL, " +
                    "`time` TEXT NOT NULL)"
            )
            notes.forEach { n ->
                db.insert("notes", null, ContentValues().apply {
                    put("note_id", n.id)
                    put("note_title", n.title)
                    put("note_description", n.description)
                    put("background_color", n.background)
                    put("title_color", n.titleColor)
                    put("note_color", n.noteColor)
                    put("title_font_size", n.titleSize)
                    put("note_font_size", n.noteSize)
                    put("title_italic", n.titleItalic); put("note_italic", n.noteItalic)
                    put("title_bold", n.titleBold); put("note_bold", n.noteBold)
                    put("title_underline", n.titleUnderline); put("note_underline", n.noteUnderline)
                    put("title_line_through", n.titleLineThrough); put("note_line_through", n.noteLineThrough)
                    put("time", n.time)
                })
            }
            db.version = 1
        }
    }
}
