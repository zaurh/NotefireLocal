package com.zaurh.notefirelocal

import com.zaurh.notefirelocal.domain.model.AppSettings
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode
import com.zaurh.notefirelocal.presentation.notes.list.NotesEffect
import com.zaurh.notefirelocal.presentation.notes.list.NotesIntent
import com.zaurh.notefirelocal.presentation.notes.list.NotesViewModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotesViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val groceries = Note(1, "Groceries", "milk, eggs", isPinned = true, updatedAt = 10)
    private val ideas = Note(2, "Ideas", "offline notes app", updatedAt = 30)
    private val call = Note(3, "", "Call mom", updatedAt = 20)

    private val notes = FakeNoteRepository(listOf(groceries, ideas, call))
    private val settings = FakeSettingsRepository()

    private fun viewModel() = NotesViewModel(notes, settings)

    @Test
    fun `splits pinned notes from the rest, newest first`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf(groceries), state.pinnedNotes)
        assertEquals(listOf(ideas, call), state.otherNotes)
    }

    @Test
    fun `search filters title and content, ignoring case`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(NotesIntent.QueryChanged("MOM"))
        assertEquals(listOf(call), vm.state.value.otherNotes)
        assertTrue(vm.state.value.pinnedNotes.isEmpty())

        vm.onIntent(NotesIntent.QueryChanged("nothing like this"))
        assertTrue(vm.state.value.isEmpty)

        vm.onIntent(NotesIntent.ClearQuery)
        assertEquals(3, vm.state.value.pinnedNotes.size + vm.state.value.otherNotes.size)
    }

    @Test
    fun `delete offers undo that restores the same note`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        val effects = mutableListOf<NotesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        advanceUntilIdle()

        vm.onIntent(NotesIntent.DeleteNote(ideas.id))
        advanceUntilIdle()
        assertEquals(null, notes.byId(ideas.id))
        val deleted = effects.filterIsInstance<NotesEffect.ShowNoteDeleted>().single().note
        assertEquals(ideas, deleted)

        vm.onIntent(NotesIntent.UndoDelete(deleted))
        advanceUntilIdle()
        assertEquals(ideas, notes.byId(ideas.id))
        assertTrue(ideas in vm.state.value.otherNotes)
    }

    @Test
    fun `toggle pin moves a note between sections`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(NotesIntent.TogglePin(ideas))
        advanceUntilIdle()
        assertNotNull(vm.state.value.pinnedNotes.firstOrNull { it.id == ideas.id })
    }

    @Test
    fun `layout toggle and theme choice are saved`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onIntent(NotesIntent.ToggleLayout)
        advanceUntilIdle()
        assertEquals(NotesLayout.List, settings.current.notesLayout)
        assertEquals(NotesLayout.List, vm.state.value.layout)

        vm.onIntent(NotesIntent.ThemeClicked)
        assertTrue(vm.state.value.isThemeDialogVisible)
        vm.onIntent(NotesIntent.ThemeSelected(ThemeMode.Dark))
        advanceUntilIdle()
        assertFalse(vm.state.value.isThemeDialogVisible)
        assertEquals(AppSettings(ThemeMode.Dark, NotesLayout.List), settings.current)
    }

    @Test
    fun `new note and note clicks open the editor`() = runTest(mainRule.dispatcher) {
        val vm = viewModel()
        val effects = mutableListOf<NotesEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }

        vm.onIntent(NotesIntent.NewNoteClicked)
        vm.onIntent(NotesIntent.NoteClicked(3))
        advanceUntilIdle()
        assertEquals(listOf(NotesEffect.OpenNote(0), NotesEffect.OpenNote(3)), effects)
    }
}
