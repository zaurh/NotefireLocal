package com.zaurh.notefirelocal.presentation.notes.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaurh.notefirelocal.R
import com.zaurh.notefirelocal.domain.model.TextFormat
import com.zaurh.notefirelocal.presentation.navigation.noteSharedBounds
import com.zaurh.notefirelocal.presentation.theme.NotefireTheme
import com.zaurh.notefirelocal.presentation.theme.applyTo
import com.zaurh.notefirelocal.presentation.theme.noteBackground
import com.zaurh.notefirelocal.presentation.theme.noteContentColor
import com.zaurh.notefirelocal.presentation.util.ObserveEffects
import com.zaurh.notefirelocal.presentation.util.shareText

@Composable
fun NoteEditorRoute(
    onClose: () -> Unit,
    onCloseAndDelete: (noteId: Long) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            NoteEditorEffect.Close -> onClose()
            is NoteEditorEffect.CloseAndDelete -> onCloseAndDelete(effect.noteId)
            is NoteEditorEffect.Share -> context.shareText(effect.text, context.getString(R.string.share_note))
            is NoteEditorEffect.ShowError -> onShowMessage(context.getString(effect.messageRes))
        }
    }

    // Fires when leaving the app and at the start of the back navigation.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.onIntent(NoteEditorIntent.ScreenPaused) }

    NoteEditorScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onIntent: (NoteEditorIntent) -> Unit,
) {
    val background by animateColorAsState(noteBackground(state.backgroundColor), label = "noteBackground")
    val contentColor = noteContentColor(state.backgroundColor)
    val titleFocus = remember { FocusRequester() }
    val contentFocus = remember { FocusRequester() }

    if (state.isNew) {
        LaunchedEffect(Unit) { titleFocus.requestFocus() }
    }

    Scaffold(
        containerColor = background,
        contentColor = contentColor,
        modifier = Modifier.noteSharedBounds(state.sharedKey, RectangleShape),
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
                ),
                navigationIcon = {
                    IconButton(onClick = { onIntent(NoteEditorIntent.Close) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(NoteEditorIntent.TogglePin) }) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(if (state.isPinned) R.string.unpin else R.string.pin),
                        )
                    }
                    if (state.hasText) {
                        IconButton(onClick = { onIntent(NoteEditorIntent.Share) }) {
                            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share_note))
                        }
                    }
                    if (state.isSaved) {
                        IconButton(onClick = { onIntent(NoteEditorIntent.Delete) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
        bottomBar = { FormattingBar(state = state, onIntent = onIntent) },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            EditorField(
                value = state.title,
                // Titles stay on one logical line (wrapping visually); Enter moves on to the text.
                onValueChange = { value ->
                    if ('\n' in value) {
                        onIntent(NoteEditorIntent.TitleChanged(value.replace("\n", "")))
                        contentFocus.requestFocus()
                    } else {
                        onIntent(NoteEditorIntent.TitleChanged(value))
                    }
                },
                placeholder = stringResource(R.string.title_hint),
                textStyle = state.titleFormat.applyTo(MaterialTheme.typography.bodyLarge, contentColor),
                imeAction = ImeAction.Next,
                onImeAction = { contentFocus.requestFocus() },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocus)
                    .onFocusChanged { if (it.isFocused) onIntent(NoteEditorIntent.FieldFocused(FormatTarget.Title)) },
            )
            Spacer(Modifier.height(12.dp))
            EditorField(
                value = state.content,
                onValueChange = { onIntent(NoteEditorIntent.ContentChanged(it)) },
                placeholder = stringResource(R.string.content_hint),
                textStyle = state.contentFormat.applyTo(MaterialTheme.typography.bodyLarge, contentColor),
                imeAction = ImeAction.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .focusRequester(contentFocus)
                    .onFocusChanged { if (it.isFocused) onIntent(NoteEditorIntent.FieldFocused(FormatTarget.Content)) },
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    state.colorWheel?.let { target ->
        val initial = when (target) {
            ColorWheelTarget.Background -> background
            ColorWheelTarget.TextColor -> state.targetFormat.color?.let(::Color) ?: contentColor
        }
        ColorWheelDialog(
            initialColor = initial,
            onPick = { onIntent(NoteEditorIntent.ColorWheelPicked(it)) },
            onDismiss = { onIntent(NoteEditorIntent.DismissColorWheel) },
        )
    }
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    onImeAction: (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        cursorBrush = SolidColor(textStyle.color),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onNext = { onImeAction?.invoke() }),
        modifier = modifier,
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle.copy(color = textStyle.color.copy(alpha = 0.45f)))
                }
                inner()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorScreenPreview() {
    NotefireTheme {
        NoteEditorScreen(
            state = NoteEditorState(
                noteId = 1, sharedKey = "note-1", isNew = false, title = "Groceries",
                content = "Milk\nEggs\nBread", backgroundColor = 0xFFFFF3B8.toInt(),
                contentFormat = TextFormat.DefaultContent.copy(italic = true),
                openPanel = EditorPanel.TextStyle, updatedAt = 1,
            ),
            onIntent = {},
        )
    }
}
