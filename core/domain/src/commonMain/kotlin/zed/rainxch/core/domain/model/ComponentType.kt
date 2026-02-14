package zed.rainxch.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ComponentType {
    @SerialName("plugin") PLUGIN,
    @SerialName("script") SCRIPT,
    @SerialName("extension") EXTENSION,
    @SerialName("software") SOFTWARE
}
