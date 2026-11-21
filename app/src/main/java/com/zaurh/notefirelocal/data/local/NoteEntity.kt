package com.zaurh.notefirelocal.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.TextFormat

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "background_color") val backgroundColor: Int?,
    @ColumnInfo(name = "title_color") val titleColor: Int?,
    @ColumnInfo(name = "title_font_size") val titleFontSize: Float,
    @ColumnInfo(name = "title_bold") val titleBold: Boolean,
    @ColumnInfo(name = "title_italic") val titleItalic: Boolean,
    @ColumnInfo(name = "title_underline") val titleUnderline: Boolean,
    @ColumnInfo(name = "title_line_through") val titleLineThrough: Boolean,
    @ColumnInfo(name = "content_color") val contentColor: Int?,
    @ColumnInfo(name = "content_font_size") val contentFontSize: Float,
    @ColumnInfo(name = "content_bold") val contentBold: Boolean,
    @ColumnInfo(name = "content_italic") val contentItalic: Boolean,
    @ColumnInfo(name = "content_underline") val contentUnderline: Boolean,
    @ColumnInfo(name = "content_line_through") val contentLineThrough: Boolean,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    backgroundColor = backgroundColor,
    titleFormat = TextFormat(titleColor, titleFontSize, titleBold, titleItalic, titleUnderline, titleLineThrough),
    contentFormat = TextFormat(contentColor, contentFontSize, contentBold, contentItalic, contentUnderline, contentLineThrough),
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    backgroundColor = backgroundColor,
    titleColor = titleFormat.color,
    titleFontSize = titleFormat.fontSize,
    titleBold = titleFormat.bold,
    titleItalic = titleFormat.italic,
    titleUnderline = titleFormat.underline,
    titleLineThrough = titleFormat.lineThrough,
    contentColor = contentFormat.color,
    contentFontSize = contentFormat.fontSize,
    contentBold = contentFormat.bold,
    contentItalic = contentFormat.italic,
    contentUnderline = contentFormat.underline,
    contentLineThrough = contentFormat.lineThrough,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
