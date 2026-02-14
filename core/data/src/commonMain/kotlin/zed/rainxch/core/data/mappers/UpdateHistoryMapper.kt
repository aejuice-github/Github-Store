package zed.rainxch.core.data.mappers

import zed.rainxch.core.data.local.db.entities.UpdateHistoryEntity
import zed.rainxch.core.domain.model.UpdateHistory

fun UpdateHistory.toEntity(): UpdateHistoryEntity {
    return UpdateHistoryEntity(
        id = id,
        componentId = componentId,
        name = name,
        fromVersion = fromVersion,
        toVersion = toVersion,
        updatedAt = updatedAt,
        success = success,
        errorMessage = errorMessage
    )
}

fun UpdateHistoryEntity.toDomain(): UpdateHistory {
    return UpdateHistory(
        id = id,
        componentId = componentId,
        name = name,
        fromVersion = fromVersion,
        toVersion = toVersion,
        updatedAt = updatedAt,
        success = success,
        errorMessage = errorMessage
    )
}
