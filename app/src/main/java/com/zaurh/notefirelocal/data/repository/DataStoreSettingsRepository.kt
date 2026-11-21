package com.zaurh.notefirelocal.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zaurh.notefirelocal.domain.model.AppSettings
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode
import com.zaurh.notefirelocal.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            AppSettings(
                themeMode = prefs[THEME_MODE]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                    // Carry over the old on/off dark mode switch.
                    ?: prefs[LEGACY_DARK_MODE]?.let { if (it) ThemeMode.Dark else ThemeMode.Light }
                    ?: ThemeMode.System,
                notesLayout = prefs[NOTES_LAYOUT]?.let { name -> NotesLayout.entries.firstOrNull { it.name == name } }
                    // The old "grid count" of 1 was a single-column list.
                    ?: if (prefs[LEGACY_GRID_CELLS] == 1) NotesLayout.List else NotesLayout.Grid,
            )
        }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setNotesLayout(layout: NotesLayout) {
        dataStore.edit { it[NOTES_LAYOUT] = layout.name }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTES_LAYOUT = stringPreferencesKey("notes_layout")
        val LEGACY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val LEGACY_GRID_CELLS = intPreferencesKey("grid_cells")
    }
}
