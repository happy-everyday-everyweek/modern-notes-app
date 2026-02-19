package com.modernnotes

import android.app.Application
import com.modernnotes.data.local.NotesDatabase
import com.modernnotes.data.repository.CategoryRepository
import com.modernnotes.data.repository.NoteRepository

class NotesApp : Application() {

    companion object {
        lateinit var database: NotesDatabase
            private set
        lateinit var noteRepository: NoteRepository
            private set
        lateinit var categoryRepository: CategoryRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = NotesDatabase.getDatabase(this)
        noteRepository = NoteRepository(database.noteDao())
        categoryRepository = CategoryRepository(database.categoryDao())
    }
}
