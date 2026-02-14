package zed.rainxch.core.data.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

fun createHttpClient(): HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }

        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, response ->
                response.status.value in 500..<600
            }
            retryOnExceptionIf { _, cause ->
                cause is HttpRequestTimeoutException ||
                        cause is UnresolvedAddressException ||
                        cause is IOException
            }
            exponentialDelay()
        }

        expectSuccess = false

        defaultRequest {
            headers.append(HttpHeaders.UserAgent, "AEJuice-ComponentManager/1.0 (KMP)")
        }
    }
}

suspend inline fun <reified T> HttpClient.executeRequest(
    crossinline block: suspend HttpClient.() -> HttpResponse
): Result<T> {
    return try {
        val response = block()
        if (response.status.isSuccess()) {
            Result.success(response.body<T>())
        } else {
            Result.failure(
                Exception("HTTP ${response.status.value}: ${response.status.description}")
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
