package zed.rainxch.core.data.network

actual fun loadBundledManifestJson(): String? {
    return Thread.currentThread().contextClassLoader
        ?.getResourceAsStream("bundled-manifest.json")
        ?.bufferedReader()
        ?.use { it.readText() }
}
