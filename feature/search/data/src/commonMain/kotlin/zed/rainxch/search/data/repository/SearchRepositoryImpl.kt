package zed.rainxch.search.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val componentRepository: ComponentRepository
) : SearchRepository {

    override fun searchComponents(query: String): Flow<List<Component>> = flow {
        val results = componentRepository.searchComponents(query)
        emit(results)
    }
}
