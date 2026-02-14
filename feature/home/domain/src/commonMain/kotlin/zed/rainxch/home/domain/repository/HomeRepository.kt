package zed.rainxch.home.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.Component

interface HomeRepository {
    fun getComponentsByCategory(category: String): Flow<List<Component>>
    fun getAllComponents(): Flow<List<Component>>
    suspend fun getCategories(): List<String>
}
