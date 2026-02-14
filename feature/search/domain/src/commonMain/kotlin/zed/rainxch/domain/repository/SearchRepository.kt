package zed.rainxch.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.Component

interface SearchRepository {
    fun searchComponents(query: String): Flow<List<Component>>
}
