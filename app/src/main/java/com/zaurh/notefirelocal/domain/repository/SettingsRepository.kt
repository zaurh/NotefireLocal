package com.zaurh.notefirelocal.domain.repository

import com.zaurh.notefirelocal.domain.model.AppSettings
import com.zaurh.notefirelocal.domain.model.NotesLayout
import com.zaurh.notefirelocal.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setNotesLayout(layout: NotesLayout)
}
