package com.alfredorosas.vet2myfriend.data.routing

import com.alfredorosas.vet2myfriend.domain.model.RemoteFailure
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal data class HttpPayload(val code: Int, val body: String)

/** Never log requests: URLs contain the API key and may contain a user's position. */
internal class GeoapifyClient(
    private val apiKey: String,
    private val client: OkHttpClient,
    private val baseUrl: HttpUrl,
) {
    suspend fun get(path: String, parameters: Map<String, String>): HttpPayload {
        if (apiKey.isBlank()) throw RemoteException(RemoteFailure.NOT_CONFIGURED)
        val url = baseUrl.newBuilder().addPathSegments(path).apply {
            parameters.forEach { (name, value) -> addQueryParameter(name, value) }
            addQueryParameter("apiKey", apiKey)
        }.build()
        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).awaitPayload()
    }
}

internal class RemoteException(val reason: RemoteFailure) : IOException()

internal fun HttpPayload.requireSuccess(): String {
    if (code in 200..299) return body
    throw RemoteException(
        when (code) {
            400, 404 -> RemoteFailure.NO_RESULTS
            401, 403 -> RemoteFailure.UNAUTHORIZED
            429 -> RemoteFailure.QUOTA
            else -> RemoteFailure.SERVICE
        },
    )
}

private suspend fun Call.awaitPayload(): HttpPayload = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                val result = response.use {
                    val body = it.body ?: throw RemoteException(RemoteFailure.INVALID_RESPONSE)
                    // Bound the decoded body, including chunked responses.
                    val source = body.source()
                    if (source.request(MAX_RESPONSE_BYTES + 1)) {
                        throw RemoteException(RemoteFailure.INVALID_RESPONSE)
                    }
                    HttpPayload(it.code, source.readUtf8())
                }
                if (continuation.isActive) continuation.resume(result)
            } catch (e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
        }
    })
}

private const val MAX_RESPONSE_BYTES = 2L * 1024 * 1024
