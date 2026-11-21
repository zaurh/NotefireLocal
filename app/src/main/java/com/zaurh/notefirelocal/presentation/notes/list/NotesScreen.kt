package com.zaurh.notefirelocal.presentation.notes.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zaurh.notefirelocal.R
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.TextFormat
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode
import com.zaurh.notefirelocal.presentation.components.EmptyState
import com.zaurh.notefirelocal.presentation.components.NoteCard
import com.zaurh.notefirelocal.presentation.navigation.noteSharedBounds
import com.zaurh.notefirelocal.presentation.navigation.noteSharedKey
import com.zaurh.notefirelocal.presentation.theme.FlameOrange
import com.zaurh.notefirelocal.presentation.theme.NotefireTheme
import com.zaurh.notefirelocal.presentation.theme.OnFlameOrange
import com.zaurh.notefirelocal.presentation.util.ObserveEffects
import com.zaurh.notefirelocal.presentation.util.emailDeveloper
import com.zaurh.notefirelocal.presentation.util.openPlayStorePage

@Composable
fun NotesRoute(
    onOpenNote: (noteId: Long) -> Unit,
    deleteRequestId: Long?,
    onDeleteRequestHandled: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Result from the editor ("delete this note"), turned into a regular intent.
    LaunchedEffect(deleteRequestId) {
        if (deleteRequestId != null) {
            viewModel.onIntent(NotesIntent.DeleteNote(deleteRequestId))
            onDeleteRequestHandled()
        }
    }

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is NotesEffect.OpenNote -> onOpenNote(effect.noteId)
            NotesEffect.OpenPlayStore -> context.openPlayStorePage()
            NotesEffect.EmailDeveloper -> context.emailDeveloper(context.getString(R.string.app_name))
            is NotesEffect.ShowNoteDeleted -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.note_deleted),
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onIntent(NotesIntent.UndoDelete(effect.note))
                }
            }
        }
    }

    NotesScreen(state = state, onIntent = viewModel::onIntent, snackbarHostState = snackbarHostState)

    if (state.isThemeDialogVisible) {
        ThemeDialog(
            selected = state.themeMode,
            onSelect = { viewModel.onIntent(NotesIntent.ThemeSelected(it)) },
            onDismiss = { viewModel.onIntent(NotesIntent.DismissThemeDialog) },
        )
    }
}

@Composable
fun NotesScreen(
    state: NotesState,
    onIntent: (NotesIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val gridState = rememberLazyStaggeredGridState()
    val fabExpanded by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 } }
    val focusManager = LocalFocusManager.current

    // When the editor closes, Compose hands its focus to the search field; don't leave it there.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!state.isSearching) focusManager.clearFocus()
    }

    BackHandler(enabled = state.isSearching) {
        focusManager.clearFocus()
        onIntent(NotesIntent.ClearQuery)
    }

    Scaffold(
        topBar = {
            NotesTopBar(
                query = state.query,
                layout = state.layout,
                onIntent = onIntent,
            )
        },
        floatingActionButton = {
            val fabShape = FloatingActionButtonDefaults.extendedFabShape
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.new_note)) },
                icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = {
                    focusManager.clearFocus()
                    onIntent(NotesIntent.NewNoteClicked)
                },
                expanded = fabExpanded,
                shape = fabShape,
                containerColor = FlameOrange,
                contentColor = OnFlameOrange,
                modifier = Modifier.noteSharedBounds(noteSharedKey(0L), fabShape),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.isEmpty && state.isSearching -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.empty_search_title),
                    message = stringResource(R.string.empty_search_message),
                    modifier = Modifier.align(Alignment.Center),
                )
                state.isEmpty -> EmptyState(
                    icon = Icons.Outlined.LocalFireDepartment,
                    title = stringResource(R.string.empty_notes_title),
                    message = stringResource(R.string.empty_notes_message),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> NotesGrid(state = state, gridState = gridState, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun NotesGrid(
    state: NotesState,
    gridState: LazyStaggeredGridState,
    onIntent: (NotesIntent) -> Unit,
) {
    val showHeaders = state.pinnedNotes.isNotEmpty()
    val isGrid = state.layout == NotesLayout.Grid
    LazyVerticalStaggeredGrid(
        columns = if (isGrid) StaggeredGridCells.Adaptive(minSize = 160.dp) else StaggeredGridCells.Fixed(1),
        state = gridState,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (showHeaders) {
            item(key = "header_pinned", span = StaggeredGridItemSpan.FullLine) {
                SectionHeader(stringResource(R.string.pinned))
            }
        }
        noteItems(state.pinnedNotes, maxContentLines = if (isGrid) 8 else 4, onIntent = onIntent)
        if (showHeaders && state.otherNotes.isNotEmpty()) {
            item(key = "header_others", span = StaggeredGridItemSpan.FullLine) {
                SectionHeader(stringResource(R.string.others))
            }
        }
        noteItems(state.otherNotes, maxContentLines = if (isGrid) 8 else 4, onIntent = onIntent)
    }
}

private fun LazyStaggeredGridScope.noteItems(
    notes: List<Note>,
    maxContentLines: Int,
    onIntent: (NotesIntent) -> Unit,
) {
    items(notes, key = { it.id }) { note ->
        NoteCard(
            note = note,
            onClick = { onIntent(NotesIntent.NoteClicked(note.id)) },
            onTogglePin = { onIntent(NotesIntent.TogglePin(note)) },
            onDelete = { onIntent(NotesIntent.DeleteNote(note.id)) },
            maxContentLines = maxContentLines,
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun NotesTopBar(
    query: String,
    layout: NotesLayout,
    onIntent: (NotesIntent) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = { onIntent(NotesIntent.QueryChanged(it)) },
        placeholder = { Text(stringResource(R.string.search_notes)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    onIntent(NotesIntent.ClearQuery)
                }) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear_search))
                }
            } else {
                Row {
                    IconButton(onClick = { onIntent(NotesIntent.ToggleLayout) }) {
                        if (layout == NotesLayout.Grid) {
                            Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = stringResource(R.string.show_as_list))
                        } else {
                            Icon(Icons.Outlined.GridView, contentDescription = stringResource(R.string.show_as_grid))
                        }
                    }
                    OverflowMenu(onIntent = onIntent)
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun OverflowMenu(onIntent: (NotesIntent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.theme)) },
                leadingIcon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                onClick = {
                    expanded = false
                    onIntent(NotesIntent.ThemeClicked)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.rate_app)) },
                leadingIcon = { Icon(Icons.Outlined.StarOutline, contentDescription = null) },
                onClick = {
                    expanded = false
                    onIntent(NotesIntent.RateAppClicked)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.contact_developer)) },
                leadingIcon = { Icon(Icons.Outlined.MailOutline, contentDescription = null) },
                onClick = {
                    expanded = false
                    onIntent(NotesIntent.ContactClicked)
                },
            )
        }
    }
}

@Composable
private fun ThemeDialog(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme)) },
        text = {
            androidx.compose.foundation.layout.Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .selectable(selected = mode == selected, role = Role.RadioButton) { onSelect(mode) },
                    ) {
                        RadioButton(selected = mode == selected, onClick = null)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = stringResource(
                                when (mode) {
                                    ThemeMode.System -> R.string.theme_system
                                    ThemeMode.Light -> R.string.theme_light
                                    ThemeMode.Dark -> R.string.theme_dark
                                }
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    val notes = listOf(
        Note(1, "Groceries", "Milk, eggs, bread, coffee beans", 0xFFFFF3B8.toInt(), isPinned = true, updatedAt = 1),
        Note(
            2, "Ideas", "A note app that works offline.\nWith colors!", 0xFF263D63.toInt(),
            titleFormat = TextFormat.DefaultTitle.copy(italic = true), updatedAt = 1,
        ),
        Note(3, "", "Call mom on Sunday", updatedAt = 1),
        Note(
            4, "Books to read", "1. Clean Architecture\n2. Kotlin in Action", 0xFFD9F2D6.toInt(),
            contentFormat = TextFormat.DefaultContent.copy(color = 0xFF1976D2.toInt(), underline = true), updatedAt = 1,
        ),
    )
    NotefireTheme {
        NotesScreen(
            state = NotesState(isLoading = false).withNotes(notes),
            onIntent = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
