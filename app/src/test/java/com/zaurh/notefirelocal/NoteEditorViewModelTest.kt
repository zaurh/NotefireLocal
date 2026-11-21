package com.zaurh.notefirelocal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import com.zaurh.notefirelocal.domain.model.Note
import com.zaurh.notefirelocal.domain.model.TextFormat
import com.zaurh.notefirelocal.presentation.notes.editor.ColorWheelTarget
import com.zaurh.notefirelocal.presentation.notes.editor.FormatTarget
import com.zaurh.notefirelocal.presentation.notes.editor.NoteEditorEffect
import com.zaurh.notefirelocal.presentation.notes.editor.NoteEditorIntent
import com.zaurh.notefirelocal.presentation.notes.editor.NoteEditorViewModel
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NoteEditorViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val yellow = 0xFFFFF3B8.toInt()
    private val existing = Note(7, "Groceries", "milk", yellow, createdAt = 100, updatedAt = 200)
    private val repo = FakeNoteRepository(listOf(existing))
    private val clock = FakeClock(time = 5_000)
    private val store = ViewModelStore()

    /** Created through a ViewModelStore so tests can clear it like Navigation does on back. */
    private fun TestScope.editor(noteId: Long): NoteEditorViewModel {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
                NoteEditorViewModel(
                    savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId)),
                    repository = repo,
                    clock = clock,
                    appScope = CoroutineScope(mainRule.dispatcher),
                ) as T
        }
        return ViewModelProvider.create(store, factory)[NoteEditorViewModel::class]
    }

    private fun TestScope.effectsOf(vm: NoteEditorViewModel): List<NoteEditorEffect> {
        val effects = mutableListOf<NoteEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        return effects
    }

    @Test
    fun `new note is saved once after typing stops`() = runTest(mainRule.dispatcher) {
        val vm = editor(0)
        vm.onIntent(NoteEditorIntent.TitleChanged("H"))
        vm.onIntent(NoteEditorIntent.TitleChanged("Hi"))
        vm.onIntent(NoteEditorIntent.ContentChanged("there"))
        advanceTimeBy(NoteEditorViewModel.TYPING_DEBOUNCE_MS - 1)
        assertEquals(0, repo.saveCount)

        advanceUntilIdle()
        assertEquals(1, repo.saveCount)
        val saved = repo.byId(vm.state.value.noteId)!!
        assertEquals(Note(saved.id, "Hi", "there", createdAt = 5_000, updatedAt = 5_000), saved)
        assertTrue(vm.state.value.isSaved)
    }

    @Test
    fun `further typing updates the same note instead of creating another`() = runTest(mainRule.dispatcher) {
        val vm = editor(0)
        vm.onIntent(NoteEditorIntent.TitleChanged("One"))
        advanceUntilIdle()
        clock.time = 9_000
        vm.onIntent(NoteEditorIntent.TitleChanged("One more"))
        advanceUntilIdle()

        assertEquals(2, repo.all.size) // the existing note + this one
        val saved = repo.byId(vm.state.value.noteId)!!
        assertEquals("One more", saved.title)
        assertEquals(5_000, saved.createdAt)
        assertEquals(9_000, saved.updatedAt)
    }

    @Test
    fun `closing an empty new note saves nothing`() = runTest(mainRule.dispatcher) {
        val vm = editor(0)
        val effects = effectsOf(vm)
        vm.onIntent(NoteEditorIntent.TitleChanged("   "))
        vm.onIntent(NoteEditorIntent.Close)
        advanceUntilIdle()

        assertEquals(0, repo.saveCount)
        assertEquals(listOf(NoteEditorEffect.Close), effects)
    }

    @Test
    fun `existing note opens instantly from memory`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("Groceries", state.title)
        assertEquals(yellow, state.backgroundColor)
        assertEquals("note-7", state.sharedKey)
    }

    @Test
    fun `pinning keeps the edit time`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.TogglePin)
        advanceUntilIdle()

        val saved = repo.byId(existing.id)!!
        assertTrue(saved.isPinned)
        assertEquals(200, saved.updatedAt)
    }

    @Test
    fun `text style applies to the focused part and is saved`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.FieldFocused(FormatTarget.Title))
        vm.onIntent(NoteEditorIntent.ToggleItalic)
        vm.onIntent(NoteEditorIntent.ToggleUnderline)
        vm.onIntent(NoteEditorIntent.TextColorSelected(0xFFD32F2F.toInt()))

        vm.onIntent(NoteEditorIntent.SelectFormatTarget(FormatTarget.Content))
        vm.onIntent(NoteEditorIntent.ToggleBold)
        vm.onIntent(NoteEditorIntent.ToggleLineThrough)
        vm.onIntent(NoteEditorIntent.FontSizeChanged(30f))
        advanceUntilIdle()

        val saved = repo.byId(existing.id)!!
        assertEquals(
            TextFormat.DefaultTitle.copy(italic = true, underline = true, color = 0xFFD32F2F.toInt()),
            saved.titleFormat,
        )
        assertEquals(
            TextFormat.DefaultContent.copy(bold = true, lineThrough = true, fontSize = 30f),
            saved.contentFormat,
        )
        assertEquals(clock.time, saved.updatedAt) // formatting counts as an edit
    }

    @Test
    fun `font size is clamped and dragging the slider saves once`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        val before = repo.saveCount
        listOf(20f, 30f, 40f, 500f).forEach { vm.onIntent(NoteEditorIntent.FontSizeChanged(it)) }
        advanceUntilIdle()

        assertEquals(before + 1, repo.saveCount)
        assertEquals(TextFormat.MAX_FONT_SIZE, repo.byId(existing.id)!!.contentFormat.fontSize)
    }

    @Test
    fun `background can be a preset, any wheel color, or the default`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.OpenColorWheel(ColorWheelTarget.Background))
        assertEquals(ColorWheelTarget.Background, vm.state.value.colorWheel)
        vm.onIntent(NoteEditorIntent.ColorWheelPicked(0xFF123456.toInt()))
        advanceUntilIdle()
        assertNull(vm.state.value.colorWheel)
        assertEquals(0xFF123456.toInt(), repo.byId(existing.id)!!.backgroundColor)

        vm.onIntent(NoteEditorIntent.BackgroundSelected(null))
        advanceUntilIdle()
        assertNull(repo.byId(existing.id)!!.backgroundColor)
    }

    @Test
    fun `wheel color for text goes to the part being styled`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.SelectFormatTarget(FormatTarget.Title))
        vm.onIntent(NoteEditorIntent.OpenColorWheel(ColorWheelTarget.TextColor))
        vm.onIntent(NoteEditorIntent.ColorWheelPicked(0xFF00FF00.toInt()))
        advanceUntilIdle()

        val saved = repo.byId(existing.id)!!
        assertEquals(0xFF00FF00.toInt(), saved.titleFormat.color)
        assertNull(saved.contentFormat.color)
        assertEquals(existing.backgroundColor, saved.backgroundColor)
    }

    @Test
    fun `system back still saves pending typing`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.ContentChanged("milk, eggs"))
        store.clear() // what Navigation does when the screen is popped
        advanceUntilIdle()

        assertEquals("milk, eggs", repo.byId(existing.id)!!.content)
    }

    @Test
    fun `a note emptied by the user is removed when the editor closes`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        vm.onIntent(NoteEditorIntent.TitleChanged(""))
        vm.onIntent(NoteEditorIntent.ContentChanged(""))
        advanceUntilIdle()
        assertTrue(repo.byId(existing.id) != null) // not while still editing

        store.clear()
        advanceUntilIdle()
        assertNull(repo.byId(existing.id))
    }

    @Test
    fun `delete hands the note to the list and is not undone by the final save`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        val effects = effectsOf(vm)
        vm.onIntent(NoteEditorIntent.ContentChanged("changed"))
        vm.onIntent(NoteEditorIntent.Delete)
        advanceUntilIdle()
        assertEquals(listOf(NoteEditorEffect.CloseAndDelete(existing.id)), effects)

        repo.deleteNote(existing.id) // the list performs the delete
        store.clear()
        advanceUntilIdle()
        assertNull(repo.byId(existing.id))
    }

    @Test
    fun `missing note shows an error and closes`() = runTest(mainRule.dispatcher) {
        val vm = editor(404)
        val effects = effectsOf(vm)
        advanceUntilIdle()
        assertTrue(effects.first() is NoteEditorEffect.ShowError)
        assertEquals(NoteEditorEffect.Close, effects.last())
        store.clear()
        advanceUntilIdle()
        assertEquals(0, repo.saveCount)
    }

    @Test
    fun `share sends title and text`() = runTest(mainRule.dispatcher) {
        val vm = editor(existing.id)
        val effects = effectsOf(vm)
        vm.onIntent(NoteEditorIntent.Share)
        advanceUntilIdle()
        assertEquals(listOf(NoteEditorEffect.Share("Groceries\n\nmilk")), effects)
    }
}
