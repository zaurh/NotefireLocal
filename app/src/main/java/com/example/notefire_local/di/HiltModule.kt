package com.example.notefire_local.di

import android.app.Application
import androidx.room.Room
import com.example.notefire_local.data.NotesDatabase
import com.example.notefire_local.domain.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Room
    @Singleton
    @Provides
    fun provideRoomDatabase(app: Application): NotesDatabase {
        return Room.databaseBuilder(
            app,
            NotesDatabase::class.java,
            "database_name"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRepository(db: NotesDatabase) = NoteRepository(db.notesDao())
}