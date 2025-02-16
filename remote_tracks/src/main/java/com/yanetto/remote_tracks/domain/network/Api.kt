package com.yanetto.remote_tracks.domain.network

import com.yanetto.remote_tracks.domain.model.ChartResponse
import com.yanetto.remote_tracks.domain.model.TracksData
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

internal interface Api {
    @GET("chart")
    suspend fun getChart(): ChartResponse

    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): TracksData

    @GET
    suspend fun loadNext(@Url url: String): TracksData
}
