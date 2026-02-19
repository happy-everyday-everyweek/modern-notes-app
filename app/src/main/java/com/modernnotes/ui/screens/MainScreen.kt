package com.modernnotes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modernnotes.data.model.Category
import com.modernnotes.data.model.Note
import com.modernnotes.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val displayNoteGroups by viewModel.displayNoteGroups.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("搜索笔记") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    } else {
                        Text("笔记")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Folder, contentDescription = "分类")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加笔记")
            }
        }
    ) { paddingValues ->
        if (displayNoteGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.NoteAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        if (isSearching && searchQuery.isNotEmpty()) "未找到相关笔记" else "暂无笔记",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        if (isSearching && searchQuery.isNotEmpty()) "尝试其他关键词" else "点击下方按钮添加新笔记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                displayNoteGroups.forEach { group ->
                    // 粘性头部
                    stickyHeader(key = group.title) {
                        GroupHeader(title = group.title)
                    }
                    
                    // 分组内的笔记卡片
                    items(group.notes, key = { "note_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            categories = categories,
                            onClick = { onNavigateToEdit(note.id) },
                            onLongClick = { showDeleteDialog = note }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条笔记吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteNote(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 分组标题组件
 * 使用粘性头部效果，滚动时固定在顶部
 */
@Composable
fun GroupHeader(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    categories: List<Category>,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    
    // 获取笔记对应的分类
    val category = remember(note.categoryId, categories) {
        note.categoryId?.let { id ->
            categories.find { it.id == id }
        }
    }
    
    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按压时的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "card_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // 使用自定义缩放效果
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行（包含标题和分类标签）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "无标题" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // 分类标签
                category?.let { cat ->
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryTag(category = cat)
                }
            }
            
            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun CategoryTag(category: Category) {
    val categoryColor = Color(category.color)
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = categoryColor.copy(alpha = 0.15f),
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 小圆点
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor)
            )
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
