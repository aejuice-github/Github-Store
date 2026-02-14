package zed.rainxch.githubstore.app.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import zed.rainxch.apps.presentation.AppsViewModel
import zed.rainxch.details.presentation.DetailsViewModel
import zed.rainxch.favourites.presentation.FavouritesViewModel
import zed.rainxch.home.presentation.HomeViewModel
import zed.rainxch.search.presentation.SearchViewModel
import zed.rainxch.settings.presentation.SettingsViewModel

val viewModelsModule = module {
    viewModelOf(::AppsViewModel)
    viewModelOf(::DetailsViewModel)
    viewModelOf(::FavouritesViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
}
