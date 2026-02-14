package zed.rainxch.home.presentation

import zed.rainxch.core.presentation.model.DiscoveryRepository
import zed.rainxch.home.domain.model.HomeCategory

data class HomeState(
    val components: List<DiscoveryRepository> = emptyList(),
    val categories: List<HomeCategory> = listOf(HomeCategory.ALL),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentCategory: HomeCategory = HomeCategory.ALL,
    val selectedApp: String = "All",
    val availableApps: List<String> = listOf(
        "All", "After Effects", "Premiere Pro", "DaVinci Resolve", "Vegas", "Final Cut Pro"
    ),
    val priceFilter: String = "All",
)
