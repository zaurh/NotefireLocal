package com.zaurh.notefirelocal.presentation.notes.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.zaurh.notefirelocal.R

/** Any color: the HSV wheel from the original app, plus a brightness slider and a preview. */
@Composable
fun ColorWheelDialog(
    initialColor: Color,
    onPick: (argb: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val controller = rememberColorPickerController()
    var picked by remember { mutableStateOf(initialColor) }
    val shape = RoundedCornerShape(12.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_color)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HsvColorPicker(
                    modifier = Modifier.size(240.dp),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { picked = it.color.copy(alpha = 1f) },
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    controller = controller,
                    initialColor = initialColor,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(picked, shape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(picked.toArgb()) }) { Text(stringResource(R.string.select)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
