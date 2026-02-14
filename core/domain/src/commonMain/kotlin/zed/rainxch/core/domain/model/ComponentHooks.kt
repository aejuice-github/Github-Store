package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ComponentHooks(
    val postInstall: String? = null,
    val preRemove: String? = null
)
