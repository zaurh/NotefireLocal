package com.zaurh.notefirelocal.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.zaurh.notefirelocal.domain.model.TextFormat

/** Quick background picks: soft pastels, then deep tones. Any other color comes from the color wheel. */
val BackgroundSwatches: List<Int> = listOf(
    0xFFFFD9D6, 0xFFFFE0C2, 0xFFFFF3B8, 0xFFD9F2D6, 0xFFCCEEEA, 0xFFD8E5FF, 0xFFEADCFB, 0xFFFCDDEC,
    0xFF5C2B2E, 0xFF5E3A1F, 0xFF2C4A33, 0xFF1F4A4A, 0xFF263D63, 0xFF45305E, 0xFF2B2B2B,
).map { it.toInt() }

/** Quick text color picks. */
val TextColorSwatches: List<Int> = listOf(
    0xFF000000, 0xFFFFFFFF, 0xFF757575, 0xFFD32F2F, 0xFFF57C00, 0xFFFBC02D,
    0xFF388E3C, 0xFF00897B, 0xFF1976D2, 0xFF7B1FA2, 0xFFC2185B, 0xFF5D4037,
).map { it.toInt() }

/** The note's own background, or the theme's note color when it has none. */
@Composable
@ReadOnlyComposable
fun noteBackground(argb: Int?): Color =
    argb?.let(::Color) ?: MaterialTheme.colorScheme.surfaceContainerLow

/** Text and icon color that stays readable on [noteBackground]. */
@Composable
@ReadOnlyComposable
fun noteContentColor(argb: Int?): Color = when {
    argb == null -> MaterialTheme.colorScheme.onSurface
    Color(argb).luminance() > 0.4f -> Color(0xFF1F1B16)
    else -> Color(0xFFF5EFEA)
}

/** Applies a note's formatting to [base]; text without its own color uses [autoColor]. */
fun TextFormat.applyTo(
    base: TextStyle,
    autoColor: Color,
    size: TextUnit = fontSize.sp,
): TextStyle = base.copy(
    color = color?.let(::Color) ?: autoColor,
    fontSize = size,
    lineHeight = size * 1.35f,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    textDecoration = TextDecoration.combine(
        listOfNotNull(
            TextDecoration.Underline.takeIf { underline },
            TextDecoration.LineThrough.takeIf { lineThrough },
        )
    ),
)
