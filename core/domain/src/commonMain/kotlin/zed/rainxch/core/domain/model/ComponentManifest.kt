package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ComponentManifest(
    val version: String,
    val categories: List<String> = emptyList(),
    val components: List<Component> = emptyList()
)
