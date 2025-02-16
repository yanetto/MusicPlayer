package com.yanetto.common_model.repository

import com.yanetto.common_model.model.Track

interface TracksRepository {
    suspend fun getTracks(): List<Track>
    suspend fun searchTracks(query: String): List<Track>
    suspend fun loadNext(): List<Track>
}