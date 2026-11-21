package com.zaurh.notefirelocal.presentation.navigation

import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zaurh.notefirelocal.presentation.notes.editor.NoteEditorRoute as NoteEditorScreenRoute
import com.zaurh.notefirelocal.presentation.notes.list.NotesRoute as NotesScreenRoute

private const val ANIM_MS = 350

/** Back-stack result key: the editor asks the notes list to delete a note (so "Undo" lives there). */
private const val DELETE_NOTE_RESULT_KEY = "delete_note_id"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NotefireNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = NotesRoute,
                enterTransition = { fadeIn(tween(ANIM_MS)) },
                exitTransition = { fadeOut(tween(ANIM_MS)) },
                popEnterTransition = { fadeIn(tween(ANIM_MS)) },
                popExitTransition = { fadeOut(tween(ANIM_MS)) },
            ) {
                composable<NotesRoute> { entry ->
                    val deleteRequestId by entry.savedStateHandle
                        .getStateFlow<Long?>(DELETE_NOTE_RESULT_KEY, null)
                        .collectAsStateWithLifecycle()
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        NotesScreenRoute(
                            onOpenNote = { id -> navController.navigate(NoteEditorRoute(id)) { launchSingleTop = true } },
                            deleteRequestId = deleteRequestId,
                            onDeleteRequestHandled = { entry.savedStateHandle[DELETE_NOTE_RESULT_KEY] = null },
                        )
                    }
                }
                composable<NoteEditorRoute> {
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                        NoteEditorScreenRoute(
                            onClose = { navController.popBackStack() },
                            onCloseAndDelete = { id ->
                                navController.previousBackStackEntry?.savedStateHandle?.set(DELETE_NOTE_RESULT_KEY, id)
                                navController.popBackStack()
                            },
                            onShowMessage = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                        )
                    }
                }
            }
        }
    }
}
