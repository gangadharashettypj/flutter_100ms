package com.example.flutter_100ms.api

import android.util.Log
import com.example.flutter_100ms.model.TokenRequestWithCode
import com.example.flutter_100ms.model.TokenRequestWithRoomId
import com.example.flutter_100ms.model.TokenResponse
import com.example.flutter_100ms.util.getTokenEndpointForCode
import com.example.flutter_100ms.util.getTokenEndpointForRoomId
import kotlinx.coroutines.CompletableDeferred
import live.hms.video.utils.GsonUtils
import live.hms.video.utils.HMSLogger
import live.hms.video.utils.toJson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "RetrofitBuilder"

object RetrofitBuilder {
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy { makeClient() }

    private fun makeClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    fun makeTokenWithRoomIdRequest(
        subdomain: String,
        roomId: String,
        role: String,
        environment: String
    ): Request {
        val url = getTokenEndpointForRoomId(environment, subdomain)
        val body = TokenRequestWithRoomId(roomId, UUID.randomUUID().toString(), role)
            .toJson()
            .toRequestBody(JSON)

        return Request.Builder()
            .url(url)
            .addHeader("Accept-Type", "application/json")
            .post(body)
            .build()
    }

    fun makeTokenWithCodeRequest(subdomain: String, code: String, environment: String): Request {
        val url = getTokenEndpointForCode(environment)
        val body = TokenRequestWithCode(code, UUID.randomUUID().toString())
            .toJson()
            .toRequestBody(JSON)

        return Request.Builder()
            .url(url)
            .addHeader("Accept-Type", "application/json")
            .addHeader("subdomain", subdomain)
            .post(body)
            .build()
    }

    fun fetchAuthToken(request: Request): TokenResponse {
        val deferred = CompletableDeferred<TokenResponse>()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                Log.e(TAG, "fetchAuthToken: ${e.message}", e)
                deferred.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "fetchAuthToken: response=$response")
                if (response.code != 200) {
                    val ex =
                        Exception("Expected response code 200 but received ${response.code} [response=$response]")
                    deferred.completeExceptionally(ex)
                    return
                }

                val body = response.body?.string() ?: ""
                try {
                    val token = GsonUtils.gson.fromJson(body, TokenResponse::class.java)

                    // In case the `token` field is absent or null we raise an exception here
                    try {
                        if (token.token == "null" || token.token.isBlank()) {
                            throw Exception("Could not fetch token, check if your environment is correct.")
                        }
                    } catch (ex: NullPointerException) {
                        throw Exception("Could not fetch token, check if your environment is correct.")
                    }

                    HMSLogger.d(TAG, "fetchAuthToken: token=$token")
                    deferred.complete(token)
                } catch (e: Exception) {
                    HMSLogger.e(TAG, "fetchAuthToken: ${e.message}", e)
                    deferred.completeExceptionally(e)
                }
            }
        })

        return deferred.getCompleted()
    }
}