package zed.rainxch.core.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.InstalledApp

interface InstalledAppsRepository {
    fun getAllInstalledApps(): Flow<List<InstalledApp>>
    fun getAppsWithUpdates(): Flow<List<InstalledApp>>
    fun getUpdateCount(): Flow<Int>
    suspend fun getByComponentId(componentId: String): InstalledApp?
    suspend fun isInstalled(componentId: String): Boolean

    suspend fun save(app: InstalledApp)
    suspend fun delete(componentId: String)

    suspend fun updateApp(app: InstalledApp)
    suspend fun updatePendingStatus(componentId: String, isPending: Boolean)
}
