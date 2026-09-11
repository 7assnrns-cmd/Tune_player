package com.example.network

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class NetworkLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val url: String,
    val statusCode: Int?,
    val durationMs: Long,
    val errorMessage: String? = null
)

object NetworkLogger {
    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs.asStateFlow()

    fun log(entry: NetworkLogEntry) {
        val current = _logs.value.toMutableList()
        if (current.size > 100) current.removeAt(0)
        current.add(entry)
        _logs.value = current
    }

    fun clear() {
        _logs.value = emptyList()
    }
}

class NetworkClient(private val context: Context) {

    private val cacheSize = 50L * 1024L * 1024L // 50 MB
    private val cacheDir = File(context.cacheDir, "http_cache")

    val networkLogs = mutableListOf<NetworkLogEntry>()

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(Cache(cacheDir, cacheSize))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 500L,
        backoffMultiplier: Double = 2.0,
        requestDescription: String = "Network Request",
        action: suspend (attempt: Int) -> T
    ): Result<T> {
        var currentDelay = initialDelayMs
        val startTime = System.currentTimeMillis()

        for (attempt in 1..maxAttempts) {
            try {
                val result = action(attempt)
                val duration = System.currentTimeMillis() - startTime
                val entry = NetworkLogEntry(
                    method = "GET",
                    url = requestDescription,
                    statusCode = 200,
                    durationMs = duration
                )
                NetworkLogger.log(entry)
                synchronized(networkLogs) {
                    if (networkLogs.size > 100) networkLogs.removeAt(0)
                    networkLogs.add(entry)
                }
                return Result.success(result)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                if (attempt == maxAttempts || e !is IOException) {
                    synchronized(networkLogs) {
                        if (networkLogs.size > 100) networkLogs.removeAt(0)
                        networkLogs.add(
                            NetworkLogEntry(
                                method = "GET",
                                url = requestDescription,
                                statusCode = null,
                                durationMs = duration,
                                errorMessage = e.message
                            )
                        )
                    }
                    return Result.failure(e)
                }
                delay(currentDelay)
                currentDelay = (currentDelay * backoffMultiplier).toLong()
            }
        }
        return Result.failure(IOException("Failed after $maxAttempts attempts"))
    }

    fun clearCache() {
        try {
            okHttpClient.cache?.evictAll()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
