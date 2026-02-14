package zed.rainxch.core.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.ComponentManifest

interface ComponentRepository {
    suspend fun getManifest(): ComponentManifest
    suspend fun refreshManifest(): ComponentManifest
    fun getComponents(): Flow<List<Component>>
    suspend fun getComponentById(id: String): Component?
    suspend fun getComponentsByCategory(category: String): List<Component>
    suspend fun searchComponents(query: String): List<Component>
    suspend fun getCategories(): List<String>
}
