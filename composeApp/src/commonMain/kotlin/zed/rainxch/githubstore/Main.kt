package zed.rainxch.githubstore

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.core.presentation.theme.GithubStoreTheme
import zed.rainxch.core.presentation.utils.ApplyAndroidSystemBars
import zed.rainxch.githubstore.app.navigation.AppNavigation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val navBackStack = rememberNavController()

    GithubStoreTheme(
        fontTheme = state.currentFontTheme,
        appTheme = state.currentColorTheme,
        isAmoledTheme = state.isAmoledTheme,
        isDarkTheme = state.isDarkTheme ?: isSystemInDarkTheme()
    ) {
        ApplyAndroidSystemBars(state.isDarkTheme)

        AppNavigation(
            navController = navBackStack
        )
    }
}
