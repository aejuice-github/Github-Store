package zed.rainxch.githubstore.app.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import zed.rainxch.githubstore.DragDropHandler
import zed.rainxch.githubstore.MainViewModel

val mainModule: Module = module {
    viewModel {
        MainViewModel(
            themesRepository = get(),
            installedAppsRepository = get(),
            syncUseCase = get(),
            dragDropHandler = getOrNull()
        )
    }
}
