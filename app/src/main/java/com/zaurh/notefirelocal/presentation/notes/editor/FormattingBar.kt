package com.zaurh.notefirelocal.presentation.notes.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zaurh.notefirelocal.R
import com.zaurh.notefirelocal.domain.model.TextFormat
import com.zaurh.notefirelocal.presentation.theme.BackgroundSwatches
import com.zaurh.notefirelocal.presentation.theme.TextColorSwatches
import com.zaurh.notefirelocal.presentation.util.formatNoteDateTime

/** Bottom toolbar: "Text style" and "Background" buttons, each opening a panel above them. */
@Composable
fun FormattingBar(
    state: NoteEditorState,
    onIntent: (NoteEditorIntent) -> Unit,
) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .imePadding()
        ) {
            AnimatedContent(
                targetState = state.openPanel,
                transitionSpec = {
                    (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically())
                },
                label = "formatPanel",
            ) { panel ->
                when (panel) {
                    EditorPanel.TextStyle -> TextStylePanel(state, onIntent)
                    EditorPanel.Background -> BackgroundPanel(state, onIntent)
                    null -> Spacer(Modifier.fillMaxWidth())
                }
            }
            if (state.openPanel != null) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                PanelButton(
                    icon = Icons.Outlined.TextFields,
                    label = stringResource(R.string.text_style),
                    checked = state.openPanel == EditorPanel.TextStyle,
                    onClick = { onIntent(NoteEditorIntent.TogglePanel(EditorPanel.TextStyle)) },
                )
                PanelButton(
                    icon = Icons.Outlined.Palette,
                    label = stringResource(R.string.background),
                    checked = state.openPanel == EditorPanel.Background,
                    onClick = { onIntent(NoteEditorIntent.TogglePanel(EditorPanel.Background)) },
                )
                Spacer(Modifier.weight(1f))
                if (state.updatedAt > 0) {
                    Text(
                        text = stringResource(R.string.edited_at, formatNoteDateTime(context, state.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelButton(icon: ImageVector, label: String, checked: Boolean, onClick: () -> Unit) {
    IconToggleButton(checked = checked, onCheckedChange = { onClick() }) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TextStylePanel(state: NoteEditorState, onIntent: (NoteEditorIntent) -> Unit) {
    val format = state.targetFormat
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SingleChoiceSegmentedButtonRow(Modifier.widthIn(max = 200.dp)) {
                FormatTarget.entries.forEachIndexed { index, target ->
                    SegmentedButton(
                        selected = state.formatTarget == target,
                        onClick = { onIntent(NoteEditorIntent.SelectFormatTarget(target)) },
                        shape = SegmentedButtonDefaults.itemShape(index, FormatTarget.entries.size),
                        icon = {},
                    ) {
                        Text(stringResource(if (target == FormatTarget.Title) R.string.target_title else R.string.target_text))
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            StyleToggle(Icons.Outlined.FormatBold, R.string.bold, format.bold) { onIntent(NoteEditorIntent.ToggleBold) }
            StyleToggle(Icons.Outlined.FormatItalic, R.string.italic, format.italic) { onIntent(NoteEditorIntent.ToggleItalic) }
            StyleToggle(Icons.Outlined.FormatUnderlined, R.string.underline, format.underline) { onIntent(NoteEditorIntent.ToggleUnderline) }
            StyleToggle(Icons.Outlined.FormatStrikethrough, R.string.strikethrough, format.lineThrough) {
                onIntent(NoteEditorIntent.ToggleLineThrough)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.FormatSize,
                contentDescription = stringResource(R.string.font_size),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Slider(
                value = format.fontSize,
                onValueChange = { onIntent(NoteEditorIntent.FontSizeChanged(it)) },
                valueRange = TextFormat.MIN_FONT_SIZE..TextFormat.MAX_FONT_SIZE,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = format.fontSize.toInt().toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .width(28.dp),
            )
        }
        ColorSwatchRow(
            selected = format.color,
            swatches = TextColorSwatches,
            noneLabel = stringResource(R.string.automatic_color),
            onSelect = { onIntent(NoteEditorIntent.TextColorSelected(it)) },
            onCustom = { onIntent(NoteEditorIntent.OpenColorWheel(ColorWheelTarget.TextColor)) },
        )
    }
}

@Composable
private fun StyleToggle(icon: ImageVector, labelRes: Int, checked: Boolean, onToggle: () -> Unit) {
    FilledIconToggleButton(checked = checked, onCheckedChange = { onToggle() }) {
        Icon(icon, contentDescription = stringResource(labelRes))
    }
}

@Composable
private fun BackgroundPanel(state: NoteEditorState, onIntent: (NoteEditorIntent) -> Unit) {
    ColorSwatchRow(
        selected = state.backgroundColor,
        swatches = BackgroundSwatches,
        noneLabel = stringResource(R.string.default_background),
        onSelect = { onIntent(NoteEditorIntent.BackgroundSelected(it)) },
        onCustom = { onIntent(NoteEditorIntent.OpenColorWheel(ColorWheelTarget.Background)) },
        modifier = Modifier.padding(vertical = 14.dp),
    )
}

/** "None/automatic", the quick picks, then a rainbow circle that opens the color wheel. */
@Composable
private fun ColorSwatchRow(
    selected: Int?,
    swatches: List<Int>,
    noneLabel: String,
    onSelect: (Int?) -> Unit,
    onCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCustom = selected != null && selected !in swatches
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Swatch(
            fill = MaterialTheme.colorScheme.surfaceContainerLowest,
            selected = selected == null,
            label = noneLabel,
            onClick = { onSelect(null) },
        ) {
            Icon(
                Icons.Outlined.FormatColorReset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        swatches.forEach { argb ->
            Swatch(
                fill = Color(argb),
                selected = argb == selected,
                label = "#%06X".format(argb and 0xFFFFFF),
                onClick = { onSelect(argb) },
            )
        }
        Swatch(
            fill = if (isCustom) Color(selected!!) else null,
            selected = isCustom,
            label = stringResource(R.string.custom_color),
            onClick = onCustom,
        ) {
            if (!isCustom) {
                Icon(Icons.Outlined.Colorize, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private val RainbowBrush = Brush.sweepGradient(
    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
)

@Composable
private fun Swatch(
    fill: Color?,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    val checkTint = if ((fill ?: Color.Gray).luminance() > 0.5f) Color.Black else Color.White
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(if (fill != null) Modifier.background(fill) else Modifier.background(RainbowBrush))
            .border(
                BorderStroke(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
                CircleShape,
            )
            .clickable(onClickLabel = label, role = Role.RadioButton, onClick = onClick),
    ) {
        if (selected) {
            Icon(Icons.Outlined.Check, contentDescription = label, tint = checkTint, modifier = Modifier.size(20.dp))
        } else {
            content()
        }
    }
}
