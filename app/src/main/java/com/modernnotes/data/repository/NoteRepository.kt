package com.modernnotes.data.repository

import com.modernnotes.data.local.NoteDao
import com.modernnotes.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNotesByCategory(categoryId: Long): Flow<List<Note>> = 
        noteDao.getNotesByCategory(categoryId)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    fun getNoteCountByCategory(categoryId: Long): Flow<Int> = 
        noteDao.getNoteCountByCategory(categoryId)
}
