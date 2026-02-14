package zed.rainxch.core.data.mappers

import zed.rainxch.core.data.local.db.entities.FavoriteRepoEntity
import zed.rainxch.core.domain.model.ComponentType
import zed.rainxch.core.domain.model.FavoriteRepo

fun FavoriteRepo.toEntity(): FavoriteRepoEntity {
    return FavoriteRepoEntity(
        componentId = componentId,
        name = name,
        author = author,
        icon = icon,
        description = description,
        category = category,
        type = type.name.lowercase(),
        isInstalled = isInstalled,
        latestVersion = latestVersion,
        addedAt = addedAt,
        lastSyncedAt = lastSyncedAt
    )
}

fun FavoriteRepoEntity.toDomain(): FavoriteRepo {
    return FavoriteRepo(
        componentId = componentId,
        name = name,
        author = author,
        icon = icon,
        description = description,
        category = category,
        type = try {
            ComponentType.valueOf(type.uppercase())
        } catch (_: Exception) {
            ComponentType.PLUGIN
        },
        isInstalled = isInstalled,
        latestVersion = latestVersion,
        addedAt = addedAt,
        lastSyncedAt = lastSyncedAt
    )
}
