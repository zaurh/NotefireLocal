package com.zaurh.notefirelocal.domain.model

enum class ThemeMode { System, Light, Dark }

enum class NotesLayout { Grid, List }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val notesLayout: NotesLayout = NotesLayout.Grid,
)
