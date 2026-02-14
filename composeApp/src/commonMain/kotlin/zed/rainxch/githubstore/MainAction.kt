package zed.rainxch.githubstore

sealed interface MainAction {
    data class OnFilesDropped(val filePaths: List<String>) : MainAction
    data object OnDragEnter : MainAction
    data object OnDragExit : MainAction
    data object DismissDragDropMessage : MainAction
    data class SwitchMode(val mode: AppMode) : MainAction
}
