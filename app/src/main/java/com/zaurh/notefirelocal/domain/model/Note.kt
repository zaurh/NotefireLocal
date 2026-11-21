package com.zaurh.notefirelocal.domain.model

data class Note(
    /** 0 means "not saved yet"; the database assigns the real id. */
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    /** ARGB background, or null for the theme's default note color. */
    val backgroundColor: Int? = null,
    val titleFormat: TextFormat = TextFormat.DefaultTitle,
    val contentFormat: TextFormat = TextFormat.DefaultContent,
    val isPinned: Boolean = false,
    /** Epoch millis. */
    val createdAt: Long = 0,
    /** Epoch millis of the last edit. */
    val updatedAt: Long = 0,
) {
    val isBlank: Boolean get() = title.isBlank() && content.isBlank()

    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return title.contains(q, ignoreCase = true) || content.contains(q, ignoreCase = true)
    }

    /** Plain text used when sharing a note to another app. */
    fun toShareText(): String = listOf(title.trim(), content.trim()).filter { it.isNotEmpty() }.joinToString("\n\n")
}

/** How the title or the text of a note is drawn. */
data class TextFormat(
    /** ARGB, or null to pick a readable color automatically. */
    val color: Int? = null,
    /** In sp. */
    val fontSize: Float,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val lineThrough: Boolean = false,
) {
    companion object {
        const val MIN_FONT_SIZE = 8f
        const val MAX_FONT_SIZE = 80f

        val DefaultTitle = TextFormat(fontSize = 22f, bold = true)
        val DefaultContent = TextFormat(fontSize = 16f)
    }
}
