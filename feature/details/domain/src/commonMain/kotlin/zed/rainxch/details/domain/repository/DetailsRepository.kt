package zed.rainxch.details.domain.repository

import zed.rainxch.core.domain.model.Component

interface DetailsRepository {
    suspend fun getComponentById(id: String): Component?
}
