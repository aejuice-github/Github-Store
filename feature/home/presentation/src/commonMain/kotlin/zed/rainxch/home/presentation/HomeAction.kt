package zed.rainxch.home.presentation

import zed.rainxch.core.domain.model.Component
import zed.rainxch.home.domain.model.HomeCategory

sealed interface HomeAction {
    data object Refresh : HomeAction
    data object Retry : HomeAction
    data object OnSearchClick : HomeAction
    data object OnSettingsClick : HomeAction
    data object OnAppsClick : HomeAction
    data object OnFavouritesClick : HomeAction
    data class SwitchCategory(val category: HomeCategory) : HomeAction
    data class OnComponentClick(val component: Component) : HomeAction
    data class SwitchApp(val app: String) : HomeAction
    data class SwitchPriceFilter(val filter: String) : HomeAction
    data object OnInstallModeClick : HomeAction
}
