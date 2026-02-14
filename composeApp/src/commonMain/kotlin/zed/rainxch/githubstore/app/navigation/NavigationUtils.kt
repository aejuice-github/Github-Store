package zed.rainxch.githubstore.app.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute

fun NavBackStackEntry?.getCurrentScreen(): GithubStoreGraph? {
    if (this == null) return null
    val route = destination.route ?: return null

    return when {
        route.contains("HomeScreen") -> GithubStoreGraph.HomeScreen
        route.contains("SearchScreen") -> GithubStoreGraph.SearchScreen
        route.contains("DetailsScreen") -> toRoute<GithubStoreGraph.DetailsScreen>()
        route.contains("SettingsScreen") -> GithubStoreGraph.SettingsScreen
        route.contains("FavouritesScreen") -> GithubStoreGraph.FavouritesScreen
        route.contains("AppsScreen") -> GithubStoreGraph.AppsScreen
        else -> null
    }
}
