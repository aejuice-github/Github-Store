package zed.rainxch.core.data.mappers

import zed.rainxch.core.data.local.db.entities.InstalledAppEntity
import zed.rainxch.core.domain.model.ComponentType
import zed.rainxch.core.domain.model.InstalledApp

fun InstalledApp.toEntity(): InstalledAppEntity {
    return InstalledAppEntity(
        componentId = componentId,
        name = name,
        type = type.name.lowercase(),
        description = description,
        author = author,
        category = category,
        icon = icon,
        installedVersion = installedVersion,
        latestVersion = latestVersion,
        isUpdateAvailable = isUpdateAvailable,
        installPath = installPath,
        files = files.joinToString("|"),
        sha256 = sha256,
        installedAt = installedAt,
        lastCheckedAt = lastCheckedAt,
        lastUpdatedAt = lastUpdatedAt,
        runnable = runnable,
        runCommand = runCommand,
        releaseNotes = releaseNotes,
        isPendingInstall = isPendingInstall
    )
}

fun InstalledAppEntity.toDomain(): InstalledApp {
    return InstalledApp(
        componentId = componentId,
        name = name,
        type = try {
            ComponentType.valueOf(type.uppercase())
        } catch (_: Exception) {
            ComponentType.PLUGIN
        },
        description = description,
        author = author,
        category = category,
        icon = icon,
        installedVersion = installedVersion,
        latestVersion = latestVersion,
        isUpdateAvailable = isUpdateAvailable,
        installPath = installPath,
        files = if (files.isBlank()) emptyList() else files.split("|"),
        sha256 = sha256,
        installedAt = installedAt,
        lastCheckedAt = lastCheckedAt,
        lastUpdatedAt = lastUpdatedAt,
        runnable = runnable,
        runCommand = runCommand,
        releaseNotes = releaseNotes,
        isPendingInstall = isPendingInstall
    )
}
