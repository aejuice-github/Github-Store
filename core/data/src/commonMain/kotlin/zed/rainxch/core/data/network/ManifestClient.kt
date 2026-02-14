package zed.rainxch.core.data.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import zed.rainxch.core.domain.model.ComponentManifest

class ManifestClient(
    private val httpClient: HttpClient,
    private val manifestUrl: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getManifest(): Result<ComponentManifest> {
        return try {
            val result = httpClient.executeRequest<ComponentManifest> {
                get(manifestUrl)
            }
            result
        } catch (e: Exception) {
            Logger.e { "Failed to fetch manifest from $manifestUrl: ${e.message}" }
            Result.failure(e)
        }
    }
}
