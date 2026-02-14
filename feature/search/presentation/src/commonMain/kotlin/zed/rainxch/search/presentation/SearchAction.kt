package zed.rainxch.search.presentation

import zed.rainxch.core.domain.model.Component

sealed interface SearchAction {
    data class OnSearchChange(val query: String) : SearchAction
    data class OnComponentClick(val component: Component) : SearchAction
    data object OnNavigateBackClick : SearchAction
    data object OnSearchImeClick : SearchAction
    data object Retry : SearchAction
}
