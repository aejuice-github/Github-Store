package zed.rainxch.settings.data.repository

import zed.rainxch.feature.settings.data.BuildKonfig
import zed.rainxch.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl : SettingsRepository {
    override fun getVersionName(): String {
        return BuildKonfig.VERSION_NAME
    }
}
