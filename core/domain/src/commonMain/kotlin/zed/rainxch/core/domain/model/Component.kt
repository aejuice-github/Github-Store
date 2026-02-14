package zed.rainxch.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Component(
    val id: String,
    val name: String,
    val type: ComponentType,
    val description: String,
    val tooltip: String = "",
    val version: String,
    val author: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val icon: String = "",
    val screenshots: List<String> = emptyList(),
    val platforms: Map<String, PlatformAsset> = emptyMap(),
    val dependencies: List<ComponentDependency> = emptyList(),
    val hooks: ComponentHooks? = null,
    val runnable: Boolean = false,
    val runCommand: String? = null,
    val changelog: String = "",
    val compatibleApps: List<String> = emptyList(),
    val price: Int = 0
)
