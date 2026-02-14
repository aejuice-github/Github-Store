package zed.rainxch.core.domain.model

data class UpdateHistory(
    val id: Long = 0,
    val componentId: String,
    val name: String,
    val fromVersion: String,
    val toVersion: String,
    val updatedAt: Long,
    val success: Boolean = true,
    val errorMessage: String? = null
)
