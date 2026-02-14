package zed.rainxch.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zed.rainxch.core.data.local.db.dao.InstalledAppDao
import zed.rainxch.core.data.local.db.dao.UpdateHistoryDao
import zed.rainxch.core.data.mappers.toDomain
import zed.rainxch.core.data.mappers.toEntity
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.repository.InstalledAppsRepository

class InstalledAppsRepositoryImpl(
    private val installedAppsDao: InstalledAppDao,
    private val historyDao: UpdateHistoryDao
) : InstalledAppsRepository {

    override fun getAllInstalledApps(): Flow<List<InstalledApp>> {
        return installedAppsDao
            .getAllInstalledApps()
            .map { it.map { app -> app.toDomain() } }
    }

    override fun getAppsWithUpdates(): Flow<List<InstalledApp>> {
        return installedAppsDao
            .getAppsWithUpdates()
            .map { it.map { app -> app.toDomain() } }
    }

    override fun getUpdateCount(): Flow<Int> = installedAppsDao.getUpdateCount()

    override suspend fun getByComponentId(componentId: String): InstalledApp? {
        return installedAppsDao.getByComponentId(componentId)?.toDomain()
    }

    override suspend fun isInstalled(componentId: String): Boolean {
        return installedAppsDao.getByComponentId(componentId) != null
    }

    override suspend fun save(app: InstalledApp) {
        installedAppsDao.insert(app.toEntity())
    }

    override suspend fun delete(componentId: String) {
        installedAppsDao.deleteByComponentId(componentId)
    }

    override suspend fun updateApp(app: InstalledApp) {
        installedAppsDao.update(app.toEntity())
    }

    override suspend fun updatePendingStatus(componentId: String, isPending: Boolean) {
        installedAppsDao.updatePendingStatus(componentId, isPending)
    }
}
