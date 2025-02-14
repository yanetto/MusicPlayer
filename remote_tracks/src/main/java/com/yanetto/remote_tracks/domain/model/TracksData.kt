package com.yanetto.remote_tracks.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TracksData (
    val data: List<ApiTrack> = emptyList(),
    val next: String? = null
)