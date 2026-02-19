package com.modernnotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modernnotes.NotesApp
import com.modernnotes.data.model.Category
import com.modernnotes.data.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 笔记分组数据类
 * @param title 分组标题（如"今日"、"昨日"、"2024年1月15日"）
 * @param notes 该分组下的笔记列表
 */
data class NoteGroup(val title: String, val notes: List<Note>)

class MainViewModel : ViewModel() {
    
    private val repository = NotesApp.noteRepository
    private val categoryRepository = NotesApp.categoryRepository
    
    val notes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Note>>(emptyList())
    val searchResults: StateFlow<List<Note>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    /**
     * 分组后的笔记列表（用于普通显示）
     */
    val groupedNotes: StateFlow<List<NoteGroup>> = notes
        .combine(_selectedCategoryId) { notesList, categoryId ->
            val filtered = if (categoryId != null) {
                notesList.filter { it.categoryId == categoryId }
            } else {
                notesList
            }
            groupNotesByDate(filtered)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    /**
     * 分组后的搜索结果
     */
    val groupedSearchResults: StateFlow<List<NoteGroup>> = searchResults
        .combine(_selectedCategoryId) { results, categoryId ->
            val filtered = if (categoryId != null) {
                results.filter { it.categoryId == categoryId }
            } else {
                results
            }
            groupNotesByDate(filtered)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    /**
     * 获取当前显示的分组笔记（根据是否在搜索状态）
     */
    val displayNoteGroups: StateFlow<List<NoteGroup>> = combine(
        groupedNotes,
        groupedSearchResults,
        isSearching
    ) { normal, search, searching ->
        if (searching) search else normal
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * 按日期分组笔记
     * 分组逻辑：
     * - 今日：笔记更新时间为今天
     * - 昨日：笔记更新时间为昨天
     * - 更早：按具体日期分组（如"2024年1月15日"）
     */
    private fun groupNotesByDate(notes: List<Note>): List<NoteGroup> {
        if (notes.isEmpty()) return emptyList()
        
        // 按更新时间降序排列
        val sortedNotes = notes.sortedByDescending { it.updatedAt }
        
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
        
        val groups = mutableMapOf<String, MutableList<Note>>()
        val groupOrder = mutableListOf<String>()
        
        for (note in sortedNotes) {
            val noteDate = Calendar.getInstance().apply {
                timeInMillis = note.updatedAt
            }
            
            val title = when {
                noteDate.timeInMillis >= today.timeInMillis -> "今日"
                noteDate.timeInMillis >= yesterday.timeInMillis -> "昨日"
                else -> dateFormat.format(Date(note.updatedAt))
            }
            
            if (!groups.containsKey(title)) {
                groups[title] = mutableListOf()
                groupOrder.add(title)
            }
            groups[title]?.add(note)
        }
        
        // 按照预定义顺序返回分组
        return groupOrder.map { title ->
            NoteGroup(title, groups[title] ?: emptyList())
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            _isSearching.value = true
            viewModelScope.launch {
                repository.searchNotes(query).collect { results ->
                    _searchResults.value = results
                }
            }
        } else {
            _isSearching.value = false
            _searchResults.value = emptyList()
        }
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNoteById(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }
}
