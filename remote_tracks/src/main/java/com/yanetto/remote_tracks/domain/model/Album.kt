package com.yanetto.remote_tracks.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Album(
    @SerialName("cover_big") val cover: String,
    val title: String?
)