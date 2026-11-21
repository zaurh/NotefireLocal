package com.zaurh.notefirelocal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaurh.notefirelocal.domain.model.ThemeMode
import com.zaurh.notefirelocal.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    /** Null until the saved theme is read; the splash screen stays up until then. */
    val themeMode: StateFlow<ThemeMode?> = settings.settings
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
