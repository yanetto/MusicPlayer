package com.yanetto.remote_tracks.data.repository

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yanetto.common_model.model.Track
import com.yanetto.common_model.repository.TracksRepository
import com.yanetto.remote_tracks.domain.network.Api
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import javax.inject.Inject

class ApiRepository @Inject constructor(
    private val okHttpClient: OkHttpClient
) : TracksRepository {
    private val baseUrl = "https://api.deezer.com/"
    private val contentType = "application/json".toMediaType()
    private val json = Json{ ignoreUnknownKeys = true }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val api = retrofit.create(Api::class.java)

    override suspend fun getTracks(): List<Track> {
        return try {
            val response = api.getTracks()
            response.tracks.data.map { it.toTrack() }
        } catch (e: Exception) {
            Log.d("ERROR", e.stackTraceToString())
            emptyList()
        }
    }

    override suspend fun searchTracks(query: String): List<Track> {
        val tracks = mutableListOf<Track>()
        var nextUrl: String? = "search?q=$query"

        while (nextUrl != null) {
            try {
                val response = api.searchTracks(nextUrl)
                tracks.addAll(response.data.map { it.toTrack() })
                nextUrl = response.next
            } catch (e: Exception) {
                Log.d("ERROR", e.stackTraceToString())
                break
            }
        }

        return tracks
    }
}
