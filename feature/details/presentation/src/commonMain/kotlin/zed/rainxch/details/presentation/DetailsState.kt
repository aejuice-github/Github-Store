package zed.rainxch.details.presentation

import zed.rainxch.core.domain.model.Component

data class DetailsState(
    val component: Component? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavourite: Boolean = false,
    val isInstalled: Boolean = false,
    val installedVersion: String? = null,
    val isUpdateAvailable: Boolean = false,
    val installProgress: Float? = null,
    val isInstalling: Boolean = false,
    val isUninstalling: Boolean = false,
)
