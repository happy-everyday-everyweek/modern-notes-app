package com.modernnotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modernnotes.NotesApp
import com.modernnotes.data.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditViewModel : ViewModel() {
    
    private val repository = NotesApp.noteRepository
    
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()
    
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()
    
    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()
    
    private val _noteId = MutableStateFlow<Long?>(null)
    val noteId: StateFlow<Long?> = _noteId.asStateFlow()
    
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            note?.let {
                _noteId.value = it.id
                _title.value = it.title
                _content.value = it.content
                _categoryId.value = it.categoryId
            }
        }
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setContent(content: String) {
        _content.value = content
    }

    fun setCategory(categoryId: Long?) {
        _categoryId.value = categoryId
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentNoteId = _noteId.value
            val note = Note(
                id = currentNoteId ?: 0,
                title = _title.value,
                content = _content.value,
                categoryId = _categoryId.value,
                updatedAt = System.currentTimeMillis()
            )
            
            if (currentNoteId != null) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }
            _isSaved.value = true
        }
    }

    fun resetState() {
        _noteId.value = null
        _title.value = ""
        _content.value = ""
        _categoryId.value = null
        _isSaved.value = false
    }
}
