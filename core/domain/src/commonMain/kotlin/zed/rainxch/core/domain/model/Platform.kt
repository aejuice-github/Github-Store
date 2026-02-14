package zed.rainxch.core.domain.model

enum class Platform {
    WINDOWS,
    MACOS,
    LINUX;

    val manifestKey: String
        get() = when (this) {
            WINDOWS -> "windows"
            MACOS -> "macos"
            LINUX -> "linux"
        }
}
