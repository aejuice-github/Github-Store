package zed.rainxch.core.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import zed.rainxch.core.data.network.ManifestClient
import zed.rainxch.core.data.network.loadBundledManifestJson
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.ComponentManifest
import zed.rainxch.core.domain.repository.ComponentRepository

class ComponentRepositoryImpl(
    private val manifestClient: ManifestClient
) : ComponentRepository {

    private val cachedManifest = MutableStateFlow<ComponentManifest?>(null)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getManifest(): ComponentManifest {
        val cached = cachedManifest.value
        if (cached != null) return cached
        return refreshManifest()
    }

    override suspend fun refreshManifest(): ComponentManifest {
        val result = manifestClient.getManifest()
        val manifest = result.getOrElse {
            Logger.i { "Network manifest unavailable, loading bundled manifest" }
            loadBundledManifest()
        }
        cachedManifest.value = manifest
        return manifest
    }

    private fun loadBundledManifest(): ComponentManifest {
        val jsonString = loadBundledManifestJson()
        if (jsonString == null) {
            Logger.e { "Bundled manifest not found" }
            return ComponentManifest(version = "0", categories = emptyList(), components = emptyList())
        }
        val manifest = json.decodeFromString<ComponentManifest>(jsonString)
        Logger.i { "Loaded bundled manifest: ${manifest.components.size} components" }
        return manifest
    }

    override fun getComponents(): Flow<List<Component>> {
        return cachedManifest.map { manifest ->
            manifest?.components ?: emptyList()
        }
    }

    override suspend fun getComponentById(id: String): Component? {
        return getManifest().components.find { it.id == id }
    }

    override suspend fun getComponentsByCategory(category: String): List<Component> {
        return getManifest().components.filter { it.category == category }
    }

    override suspend fun searchComponents(query: String): List<Component> {
        val lowerQuery = query.lowercase()
        return getManifest().components.filter { component ->
            component.name.lowercase().contains(lowerQuery) ||
                    component.description.lowercase().contains(lowerQuery) ||
                    component.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }

    override suspend fun getCategories(): List<String> {
        return getManifest().categories
    }
}
