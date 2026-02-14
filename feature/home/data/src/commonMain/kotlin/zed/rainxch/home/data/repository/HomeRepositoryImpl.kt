package zed.rainxch.home.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.home.domain.repository.HomeRepository

class HomeRepositoryImpl(
    private val componentRepository: ComponentRepository
) : HomeRepository {

    override fun getComponentsByCategory(category: String): Flow<List<Component>> = flow {
        val manifest = componentRepository.getManifest()
        emit(manifest.components.filter { it.category.equals(category, ignoreCase = true) })
    }

    override fun getAllComponents(): Flow<List<Component>> = flow {
        val manifest = componentRepository.getManifest()
        emit(manifest.components)
    }

    override suspend fun getCategories(): List<String> {
        return componentRepository.getCategories()
    }
}
