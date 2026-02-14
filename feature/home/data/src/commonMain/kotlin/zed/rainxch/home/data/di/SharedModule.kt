package zed.rainxch.home.data.di

import org.koin.dsl.module
import zed.rainxch.home.data.repository.HomeRepositoryImpl
import zed.rainxch.home.domain.repository.HomeRepository

val homeModule = module {
    single<HomeRepository> { HomeRepositoryImpl(componentRepository = get()) }
}
