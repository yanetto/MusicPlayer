package com.yanetto.remote_tracks.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChartResponse (
    val tracks: TracksData
)