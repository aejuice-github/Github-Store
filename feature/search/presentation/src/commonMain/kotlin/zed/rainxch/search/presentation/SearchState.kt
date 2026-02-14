package zed.rainxch.search.presentation

import zed.rainxch.core.presentation.model.DiscoveryRepository

data class SearchState(
    val query: String = "",
    val results: List<DiscoveryRepository> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
