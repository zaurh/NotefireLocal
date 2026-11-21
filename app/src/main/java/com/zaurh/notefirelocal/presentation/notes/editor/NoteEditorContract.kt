package com.zaurh.notefirelocal.presentation.notes.editor

import androidx.annotation.StringRes
import com.zaurh.notefirelocal.domain.model.TextFormat

data class NoteEditorState(
    /** 0 until the note is first saved. */
    val noteId: Long,
    /** Shared-element key of what the editor grew out of (a card, or the "New note" button). */
    val sharedKey: String,
    val isNew: Boolean,
    val isLoading: Boolean = false,
    val title: String = "",
    val content: String = "",
    /** ARGB, or null for the theme's default note color. */
    val backgroundColor: Int? = null,
    val titleFormat: TextFormat = TextFormat.DefaultTitle,
    val contentFormat: TextFormat = TextFormat.DefaultContent,
    val isPinned: Boolean = false,
    /** Epoch millis of the last edit; 0 if never saved. */
    val updatedAt: Long = 0,
    /** Which formatting panel is open in the bottom toolbar, if any. */
    val openPanel: EditorPanel? = null,
    /** Which part the text-style controls apply to; follows the field being edited. */
    val formatTarget: FormatTarget = FormatTarget.Content,
    /** Non-null while the color wheel dialog is open. */
    val colorWheel: ColorWheelTarget? = null,
) {
    val isSaved: Boolean get() = noteId != 0L
    val hasText: Boolean get() = title.isNotBlank() || content.isNotBlank()
    val targetFormat: TextFormat get() = if (formatTarget == FormatTarget.Title) titleFormat else contentFormat
}

enum class EditorPanel { TextStyle, Background }

enum class FormatTarget { Title, Content }

enum class ColorWheelTarget { Background, TextColor }

sealed interface NoteEditorIntent {
    data class TitleChanged(val value: String) : NoteEditorIntent
    data class ContentChanged(val value: String) : NoteEditorIntent

    // Formatting
    data class TogglePanel(val panel: EditorPanel) : NoteEditorIntent
    data class FieldFocused(val target: FormatTarget) : NoteEditorIntent
    data class SelectFormatTarget(val target: FormatTarget) : NoteEditorIntent
    data object ToggleBold : NoteEditorIntent
    data object ToggleItalic : NoteEditorIntent
    data object ToggleUnderline : NoteEditorIntent
    data object ToggleLineThrough : NoteEditorIntent
    data class FontSizeChanged(val size: Float) : NoteEditorIntent
    /** Null means "automatic" (readable on the background). */
    data class TextColorSelected(val argb: Int?) : NoteEditorIntent
    /** Null means the theme's default note color. */
    data class BackgroundSelected(val argb: Int?) : NoteEditorIntent
    data class OpenColorWheel(val target: ColorWheelTarget) : NoteEditorIntent
    data class ColorWheelPicked(val argb: Int) : NoteEditorIntent
    data object DismissColorWheel : NoteEditorIntent

    data object TogglePin : NoteEditorIntent
    data object Share : NoteEditorIntent
    data object Delete : NoteEditorIntent
    /** Back arrow: saves pending changes, then closes. (System back closes directly; saving still happens.) */
    data object Close : NoteEditorIntent
    /** The screen left the foreground: write pending changes right away. */
    data object ScreenPaused : NoteEditorIntent
}

sealed interface NoteEditorEffect {
    data object Close : NoteEditorEffect
    /** The notes list performs the delete so it can offer "Undo". */
    data class CloseAndDelete(val noteId: Long) : NoteEditorEffect
    data class Share(val text: String) : NoteEditorEffect
    data class ShowError(@StringRes val messageRes: Int) : NoteEditorEffect
}
