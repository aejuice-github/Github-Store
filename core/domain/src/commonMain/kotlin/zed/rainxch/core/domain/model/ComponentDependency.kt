package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ComponentDependency(
    val id: String,
    val version: String = ""
)
