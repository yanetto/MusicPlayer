package com.yanetto.remote_tracks.domain.network

import com.yanetto.remote_tracks.domain.model.ChartResponse
import com.yanetto.remote_tracks.domain.model.TracksData
import retrofit2.http.GET
import retrofit2.http.Url

interface Api {
    @GET("chart")
    suspend fun getTracks(): ChartResponse

    @GET
    suspend fun searchTracks(@Url url: String): TracksData
}
