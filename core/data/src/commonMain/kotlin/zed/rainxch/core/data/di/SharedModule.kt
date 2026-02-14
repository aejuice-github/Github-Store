package zed.rainxch.core.data.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import zed.rainxch.core.data.install.DependencyResolver
import zed.rainxch.core.data.install.InstallEngine
import zed.rainxch.core.data.local.db.AppDatabase
import zed.rainxch.core.data.local.db.dao.FavoriteRepoDao
import zed.rainxch.core.data.local.db.dao.InstalledAppDao
import zed.rainxch.core.data.local.db.dao.UpdateHistoryDao
import zed.rainxch.core.data.logging.KermitLogger
import zed.rainxch.core.data.network.ManifestClient
import zed.rainxch.core.data.network.createHttpClient
import zed.rainxch.core.data.repository.ComponentRepositoryImpl
import zed.rainxch.core.data.repository.FavouritesRepositoryImpl
import zed.rainxch.core.data.repository.InstalledAppsRepositoryImpl
import zed.rainxch.core.data.repository.ThemesRepositoryImpl
import zed.rainxch.core.domain.getPlatform
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.core.domain.repository.FavouritesRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.repository.ThemesRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase

val coreModule = module {
    single {
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    single<GitHubStoreLogger> {
        KermitLogger
    }

    single<Platform> {
        getPlatform()
    }

    single<FavouritesRepository> {
        FavouritesRepositoryImpl(
            favoriteRepoDao = get()
        )
    }

    single<InstalledAppsRepository> {
        InstalledAppsRepositoryImpl(
            installedAppsDao = get(),
            historyDao = get()
        )
    }

    single<ComponentRepository> {
        ComponentRepositoryImpl(
            manifestClient = get()
        )
    }

    single<ThemesRepository> {
        ThemesRepositoryImpl(
            preferences = get()
        )
    }

    single<SyncInstalledAppsUseCase> {
        SyncInstalledAppsUseCase(
            packageMonitor = get(),
            installedAppsRepository = get(),
            logger = get()
        )
    }

    single<InstallEngine> {
        InstallEngine(
            platform = get(),
            installer = get(),
            downloader = get(),
            componentRepository = get(),
            installedAppsRepository = get(),
            installedAppsDao = get(),
            historyDao = get()
        )
    }

    single<DependencyResolver> {
        DependencyResolver(
            componentRepository = get(),
            installedAppsRepository = get()
        )
    }
}

val networkModule = module {
    single<HttpClient> {
        createHttpClient()
    }

    single<ManifestClient> {
        ManifestClient(
            httpClient = get(),
            manifestUrl = "https://aejuice-component-store.s3.amazonaws.com/manifest.json"
        )
    }
}

val databaseModule = module {
    single<FavoriteRepoDao> {
        get<AppDatabase>().favoriteRepoDao
    }

    single<InstalledAppDao> {
        get<AppDatabase>().installedAppDao
    }

    single<UpdateHistoryDao> {
        get<AppDatabase>().updateHistoryDao
    }
}
