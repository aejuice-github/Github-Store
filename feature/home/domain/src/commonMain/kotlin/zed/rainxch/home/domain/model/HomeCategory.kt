package zed.rainxch.home.domain.model

data class HomeCategory(
    val name: String,
    val displayName: String = name
) {
    companion object {
        val ALL = HomeCategory(name = "", displayName = "All")
    }
}
