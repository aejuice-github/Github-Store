package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlatformAsset(
    val url: String,
    val sha256: String = "",
    val size: Long = 0,
    val installPath: String = "",
    val requiresAdmin: Boolean = false
)
