package zed.rainxch.details.presentation

sealed interface DetailsEvent {
    data class ShowError(val message: String) : DetailsEvent
    data class ShowSuccess(val message: String) : DetailsEvent
    data class ShowLongMessage(val message: String) : DetailsEvent
    data object NavigateBack : DetailsEvent
}
