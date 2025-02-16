package com.yanetto.remote_tracks.data.repository

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yanetto.common_model.model.Track
import com.yanetto.common_model.repository.TracksRepository
import com.yanetto.remote_tracks.domain.network.Api
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

class ApiRepository @Inject constructor(
    okHttpClient: OkHttpClient
) : TracksRepository {

    companion object {
        const val TAG = "API_REPOSITORY"
        private const val BASE_URL = "https://api.deezer.com/"
    }

    private val contentType = "application/json".toMediaType()
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val api = retrofit.create(Api::class.java)

    private var nextUrl: String? = null
    private val cachedTracks = mutableListOf<Track>()

    override suspend fun getTracks(): List<Track> {
        return try {
            val response = api.getChart()
            response.tracks.data.map { it.toTrack() }
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }
    }

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            cachedTracks.clear()
            val response = api.searchTracks(query)
            nextUrl = response.next
            val newTracks = response.data.map { it.toTrack() }
            cachedTracks.addAll(newTracks)
            cachedTracks
        } catch (cancellationException: CancellationException) {
            Log.e(TAG, cancellationException.stackTraceToString())
            cachedTracks
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }
    }

    override suspend fun loadNext(): List<Track> {
        val url = nextUrl ?: return cachedTracks

        return try {
            val response = api.loadNext(url)
            nextUrl = response.next

            val newTracks = response.data.map { it.toTrack() }

            cachedTracks.addAll(newTracks)

            cachedTracks
        } catch (cancellationException: CancellationException) {
            Log.e(TAG, cancellationException.stackTraceToString())
            cachedTracks
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }
    }
}

