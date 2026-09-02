package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.AiVideoStudioTheme
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiVideoStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: StudioViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onCreateNewClick = {
                    navController.navigate("create")
                },
                onProjectClick = { projectId ->
                    navController.navigate("pipeline/$projectId")
                },
                onQuickPromptClick = { prompt, language, tone ->
                    navController.navigate("create?prompt=$prompt&language=$language&tone=$tone")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onProjectsListClick = {
                    navController.navigate("projects")
                }
            )
        }

        composable(
            route = "create?prompt={prompt}&language={language}&tone={tone}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("language") {
                    type = NavType.StringType
                    defaultValue = "Hindi"
                },
                navArgument("tone") {
                    type = NavType.StringType
                    defaultValue = "Motivational"
                }
            )
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt") ?: ""
            val language = backStackEntry.arguments?.getString("language") ?: "Hindi"
            val tone = backStackEntry.arguments?.getString("tone") ?: "Motivational"

            CreatePromptScreen(
                viewModel = viewModel,
                initialPrompt = prompt,
                initialLanguage = language,
                initialTone = tone,
                onBackClick = { navController.popBackStack() },
                onPipelineStarted = { projectId ->
                    navController.navigate("pipeline/$projectId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "pipeline/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PipelineScreen(
                viewModel = viewModel,
                projectId = projectId,
                onBackClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                onViewStoryboardClick = { id -> navController.navigate("storyboard/$id") },
                onViewPreviewClick = { id -> navController.navigate("player/$id") },
                onViewScriptClick = { id -> navController.navigate("script/$id") }
            )
        }

        composable(
            route = "storyboard/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            StoryboardEditorScreen(
                viewModel = viewModel,
                projectId = projectId,
                onBackClick = { navController.popBackStack() },
                onOpenPlayerClick = { id -> navController.navigate("player/$id") }
            )
        }

        composable(
            route = "player/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            VideoPreviewPlayerScreen(
                viewModel = viewModel,
                projectId = projectId,
                onBackClick = { navController.popBackStack() },
                onEditStoryboardClick = { id -> navController.navigate("storyboard/$id") }
            )
        }

        composable(
            route = "script/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ResearchScriptScreen(
                viewModel = viewModel,
                projectId = projectId,
                onBackClick = { navController.popBackStack() },
                onOpenStoryboardClick = { id -> navController.navigate("storyboard/$id") }
            )
        }

        composable("projects") {
            ProjectsListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onProjectClick = { projectId -> navController.navigate("pipeline/$projectId") },
                onCreateNewClick = { navController.navigate("create") }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
