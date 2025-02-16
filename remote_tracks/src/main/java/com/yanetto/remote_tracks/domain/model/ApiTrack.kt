package com.yanetto.remote_tracks.domain.model

import com.yanetto.common_model.model.Track
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiTrack(
    val id: Long,
    val title: String,
    @SerialName("preview") val mediaUri: String,
    val artist: Artist,
    val album: Album
) {
    fun toTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist.name,
            mediaUri = mediaUri,
            albumCoverUri = album.cover,
            albumTitle = album.title.toString()
        )
    }
}