package com.zaurh.notefirelocal.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.zaurh.notefirelocal.core.Clock
import com.zaurh.notefirelocal.data.local.MIGRATION_1_2
import com.zaurh.notefirelocal.data.local.NoteDao
import com.zaurh.notefirelocal.data.local.NotesDatabase
import com.zaurh.notefirelocal.data.repository.DataStoreSettingsRepository
import com.zaurh.notefirelocal.data.repository.RoomNoteRepository
import com.zaurh.notefirelocal.domain.repository.NoteRepository
import com.zaurh.notefirelocal.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun provideClock(): Clock = Clock { System.currentTimeMillis() }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotesDatabase =
        // Same file name as v1 of the app, so existing notes are migrated, never wiped.
        Room.databaseBuilder(context, NotesDatabase::class.java, NotesDatabase.NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideNoteDao(db: NotesDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): DataStore<Preferences> =
        // Same file the old app used, so the saved theme carries over.
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("notefire_local") }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindNoteRepository(impl: RoomNoteRepository): NoteRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
