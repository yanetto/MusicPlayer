package com.yanetto.local_tracks.data.repository
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.yanetto.common_model.model.Track
import com.yanetto.common_model.repository.TracksRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreTracksRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TracksRepository {

    override suspend fun getTracks(): List<Track> = withContext(Dispatchers.IO) {
        queryTracks()
    }

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        queryTracks(query)
    }

    private fun queryTracks(searchQuery: String? = null): List<Track> {
        val tracks = mutableListOf<Track>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = if (searchQuery.isNullOrBlank()) {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        } else {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                    "(${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        }

        val selectionArgs = if (searchQuery.isNullOrBlank()) {
            null
        } else {
            arrayOf("%$searchQuery%", "%$searchQuery%")
        }

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val artist = cursor.getString(artistColumn)
                val filePath = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val albumCoverUri = getAlbumArtUri(albumId)

                tracks.add(Track(id, name, artist, filePath, albumCoverUri))
            }
        }
        return tracks
    }

    private fun getAlbumArtUri(albumId: Long): String? {
        val albumArtUri = Uri.withAppendedPath(
            Uri.parse("content://media/external/audio/albumart"), albumId.toString()
        )

        return try {
            context.contentResolver.openInputStream(albumArtUri)?.close()
            albumArtUri.toString()
        } catch (e: Exception) {
            null
        }
    }
}