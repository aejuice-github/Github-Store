package zed.rainxch.details.presentation

sealed interface DetailsAction {
    data object OnNavigateBack : DetailsAction
    data object OnInstall : DetailsAction
    data object OnUninstall : DetailsAction
    data object OnUpdate : DetailsAction
    data object OnToggleFavourite : DetailsAction
    data object OnRun : DetailsAction
    data object Retry : DetailsAction
}
