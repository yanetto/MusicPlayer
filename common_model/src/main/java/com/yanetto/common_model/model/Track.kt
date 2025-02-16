package com.yanetto.common_model.model

import kotlinx.serialization.Serializable

@Serializable
data class Track (
    val id: Long,
    val title: String,
    val artist: String,
    val mediaUri: String,
    val albumTitle: String,
    val albumCoverUri: String?
)
