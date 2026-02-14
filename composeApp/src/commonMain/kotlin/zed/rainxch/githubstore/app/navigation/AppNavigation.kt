package zed.rainxch.githubstore.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import io.github.fletchmckee.liquid.rememberLiquidState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import zed.rainxch.apps.presentation.AppsRoot
import zed.rainxch.core.presentation.locals.LocalBottomNavigationLiquid
import zed.rainxch.details.presentation.DetailsRoot
import zed.rainxch.favourites.presentation.FavouritesRoot
import zed.rainxch.home.presentation.HomeRoot
import zed.rainxch.search.presentation.SearchRoot
import zed.rainxch.settings.presentation.SettingsRoot

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    val liquidState = rememberLiquidState()

    CompositionLocalProvider(
        value = LocalBottomNavigationLiquid provides liquidState
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = GithubStoreGraph.HomeScreen,
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                composable<GithubStoreGraph.HomeScreen> {
                    HomeRoot(
                        onNavigateToSearch = {
                            navController.navigate(GithubStoreGraph.SearchScreen)
                        },
                        onNavigateToSettings = {
                            navController.navigate(GithubStoreGraph.SettingsScreen)
                        },
                        onNavigateToApps = {
                            navController.navigate(GithubStoreGraph.AppsScreen)
                        },
                        onNavigateToDetails = { component ->
                            navController.navigate(
                                GithubStoreGraph.DetailsScreen(
                                    componentId = component.id
                                )
                            )
                        },
                    )
                }

                composable<GithubStoreGraph.SearchScreen> {
                    SearchRoot(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onNavigateToDetails = { component ->
                            navController.navigate(
                                GithubStoreGraph.DetailsScreen(
                                    componentId = component.id
                                )
                            )
                        },
                    )
                }

                composable<GithubStoreGraph.DetailsScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<GithubStoreGraph.DetailsScreen>()
                    DetailsRoot(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        viewModel = koinViewModel {
                            parametersOf(args.componentId)
                        }
                    )
                }

                composable<GithubStoreGraph.FavouritesScreen> {
                    FavouritesRoot(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onNavigateToDetails = { componentId ->
                            navController.navigate(
                                GithubStoreGraph.DetailsScreen(componentId)
                            )
                        },
                    )
                }

                composable<GithubStoreGraph.SettingsScreen> {
                    SettingsRoot(
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }

                composable<GithubStoreGraph.AppsScreen> {
                    AppsRoot(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        onNavigateToComponent = { componentId ->
                            navController.navigate(
                                GithubStoreGraph.DetailsScreen(componentId)
                            )
                        }
                    )
                }
            }

            val currentScreen = navController.currentBackStackEntryAsState().value.getCurrentScreen()

            currentScreen?.let {
                BottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = {
                        navController.navigate(it)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}
