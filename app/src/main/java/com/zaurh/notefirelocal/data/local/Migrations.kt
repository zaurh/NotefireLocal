package com.zaurh.notefirelocal.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 1.0.x → 2.0: keeps every note with all of its formatting (colors, font sizes, bold, italic,
 * underline, strikethrough) and its date, and adds pinning. See [migrateLegacyBackground] for how
 * the old default colors are treated.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notes_new` (" +
                "`note_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, `content` TEXT NOT NULL, `background_color` INTEGER, " +
                "`title_color` INTEGER, `title_font_size` REAL NOT NULL, `title_bold` INTEGER NOT NULL, " +
                "`title_italic` INTEGER NOT NULL, `title_underline` INTEGER NOT NULL, `title_line_through` INTEGER NOT NULL, " +
                "`content_color` INTEGER, `content_font_size` REAL NOT NULL, `content_bold` INTEGER NOT NULL, " +
                "`content_italic` INTEGER NOT NULL, `content_underline` INTEGER NOT NULL, `content_line_through` INTEGER NOT NULL, " +
                "`is_pinned` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)"
        )
        val now = System.currentTimeMillis()
        db.query(
            "SELECT note_id, note_title, note_description, background_color, title_color, note_color, " +
                "title_font_size, note_font_size, title_bold, title_italic, title_underline, title_line_through, " +
                "note_bold, note_italic, note_underline, note_line_through, time FROM notes"
        ).use { c ->
            while (c.moveToNext()) {
                val background = c.getInt(3)
                val time = c.getString(16)?.trim()?.toLongOrNull() ?: now
                db.execSQL(
                    "INSERT INTO notes_new (note_id, title, content, background_color, " +
                        "title_color, title_font_size, title_bold, title_italic, title_underline, title_line_through, " +
                        "content_color, content_font_size, content_bold, content_italic, content_underline, content_line_through, " +
                        "is_pinned, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)",
                    arrayOf<Any?>(
                        c.getLong(0),
                        c.getString(1).orEmpty(),
                        c.getString(2).orEmpty(),
                        migrateLegacyBackground(background),
                        migrateLegacyTextColor(c.getInt(4), background),
                        c.getFloat(6),
                        c.getInt(8), c.getInt(9), c.getInt(10), c.getInt(11),
                        migrateLegacyTextColor(c.getInt(5), background),
                        c.getFloat(7),
                        c.getInt(12), c.getInt(13), c.getInt(14), c.getInt(15),
                        time,
                        time,
                    ),
                )
            }
        }
        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}
