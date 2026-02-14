package zed.rainxch.core.presentation.model

import zed.rainxch.core.domain.model.Component

data class DiscoveryRepository(
    val isInstalled: Boolean,
    val isUpdateAvailable: Boolean,
    val isFavourite: Boolean,
    val component: Component,
)
