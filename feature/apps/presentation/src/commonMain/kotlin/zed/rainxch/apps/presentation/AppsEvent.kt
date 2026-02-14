package zed.rainxch.apps.presentation

sealed interface AppsEvent {
    data class ShowError(val message: String) : AppsEvent
    data class ShowSuccess(val message: String) : AppsEvent
    data class NavigateToComponent(val componentId: String) : AppsEvent
}
