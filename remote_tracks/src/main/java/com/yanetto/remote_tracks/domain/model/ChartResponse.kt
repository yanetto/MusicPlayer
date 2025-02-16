package com.yanetto.remote_tracks.domain.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ChartResponse(
    val tracks: TracksData
)