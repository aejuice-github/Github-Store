package zed.rainxch.githubstore

import zed.rainxch.core.data.services.DragDropInstaller

class DesktopDragDropHandler(
    private val dragDropInstaller: DragDropInstaller
) : DragDropHandler {

    override suspend fun installFiles(filePaths: List<String>): List<DragDropResult> {
        return dragDropInstaller.installFiles(filePaths).map { result ->
            DragDropResult(
                fileName = result.fileName,
                type = result.type,
                success = result.success,
                error = result.error
            )
        }
    }
}
