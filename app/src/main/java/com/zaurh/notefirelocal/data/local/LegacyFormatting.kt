package com.zaurh.notefirelocal.data.local

// Version 1 stored every color explicitly: white background and black text by default.
// Those defaults become "follow the theme" (so old notes are readable in dark mode);
// any color the user actually picked is kept as is.

const val LEGACY_WHITE = 0xFFFFFFFF.toInt()
const val LEGACY_BLACK = 0xFF000000.toInt()

/** Null for the old default white; otherwise the color, made opaque the way it looked over white. */
fun migrateLegacyBackground(argb: Int): Int? {
    val opaque = compositeOverWhite(argb)
    return if (opaque == LEGACY_WHITE) null else opaque
}

/** Null for default black text on a default white note; otherwise the color unchanged. */
fun migrateLegacyTextColor(textArgb: Int, backgroundArgb: Int): Int? =
    if (textArgb == LEGACY_BLACK && migrateLegacyBackground(backgroundArgb) == null) null else textArgb

/** The old app drew backgrounds over white, so a translucent color looked lighter. */
private fun compositeOverWhite(argb: Int): Int {
    val a = (argb ushr 24) and 0xFF
    if (a == 0xFF) return argb
    fun channel(shift: Int): Int {
        val c = (argb shr shift) and 0xFF
        return (c * a + 0xFF * (0xFF - a) + 127) / 0xFF
    }
    return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}
