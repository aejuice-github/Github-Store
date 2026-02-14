package zed.rainxch.details.data.repository

import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.details.domain.repository.DetailsRepository

class DetailsRepositoryImpl(
    private val componentRepository: ComponentRepository
) : DetailsRepository {

    override suspend fun getComponentById(id: String): Component? {
        return componentRepository.getComponentById(id)
    }
}
