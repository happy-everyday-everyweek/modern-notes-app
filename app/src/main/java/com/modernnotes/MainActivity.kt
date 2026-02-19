package com.modernnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.modernnotes.ui.screens.*
import com.modernnotes.ui.theme.ModernNotesTheme

// 动画时长常量
private const val ANIM_DURATION = 350

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesNavHost()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Edit : Screen("edit/{noteId}") {
        fun createRoute(noteId: Long?) = "edit/${noteId ?: "new"}"
    }
    object Categories : Screen("categories")
    object Settings : Screen("settings")
}

@Composable
fun NotesNavHost() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        // 默认进入动画：从右滑入 + 淡入
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIM_DURATION)
            ) + fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        // 默认退出动画：淡出
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION / 2))
        },
        // 返回时进入动画：淡入
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION / 2))
        },
        // 返回时退出动画：向右滑出 + 淡出
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIM_DURATION)
            ) + fadeOut(animationSpec = tween(ANIM_DURATION))
        }
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToEdit = { noteId ->
                    navController.navigate(Screen.Edit.createRoute(noteId))
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Edit.route) { backStackEntry ->
            val noteIdStr = backStackEntry.arguments?.getString("noteId")
            val noteId = if (noteIdStr == "new" || noteIdStr == null) null else noteIdStr.toLongOrNull()
            
            EditScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
