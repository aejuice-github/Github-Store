package zed.rainxch.core.domain.model

sealed class InstallResult {
    data class Success(val componentId: String, val version: String) : InstallResult()
    data class NeedsAdmin(val componentId: String, val installPath: String) : InstallResult()
    data class MissingDependencies(val missing: List<ComponentDependency>) : InstallResult()
    data class HashMismatch(val expected: String, val actual: String) : InstallResult()
    data class Error(val message: String, val cause: Throwable? = null) : InstallResult()
}
