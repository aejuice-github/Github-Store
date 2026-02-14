package zed.rainxch.githubstore.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface GithubStoreGraph {
    @Serializable
    data object HomeScreen : GithubStoreGraph

    @Serializable
    data object SearchScreen : GithubStoreGraph

    @Serializable
    data class DetailsScreen(
        val componentId: String
    ) : GithubStoreGraph

    @Serializable
    data object SettingsScreen : GithubStoreGraph

    @Serializable
    data object FavouritesScreen : GithubStoreGraph

    @Serializable
    data object AppsScreen : GithubStoreGraph
}
