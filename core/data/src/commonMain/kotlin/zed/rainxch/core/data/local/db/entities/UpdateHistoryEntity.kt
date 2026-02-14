package zed.rainxch.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "update_history")
data class UpdateHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val componentId: String,
    val name: String,
    val fromVersion: String,
    val toVersion: String,
    val updatedAt: Long,
    val success: Boolean = true,
    val errorMessage: String? = null
)
