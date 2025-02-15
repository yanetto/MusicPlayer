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

    companion object {
        private const val PAGE_SIZE = 25
    }

    private var lastOffset = 0
    private val cachedTracks = mutableListOf<Track>()

    override suspend fun getTracks(): List<Track> = withContext(Dispatchers.IO) {
        cachedTracks.clear()
        lastOffset = 0
        val newTracks = queryTracks(limit = PAGE_SIZE, offset = lastOffset)
        cachedTracks.addAll(newTracks)
        cachedTracks
    }

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        cachedTracks.clear()
        lastOffset = 0
        val newTracks = queryTracks(searchQuery = query, limit = PAGE_SIZE, offset = lastOffset)
        cachedTracks.addAll(newTracks)
        cachedTracks
    }

    suspend fun loadNext(): List<Track> = withContext(Dispatchers.IO) {
        lastOffset += PAGE_SIZE
        val newTracks = queryTracks(limit = PAGE_SIZE, offset = lastOffset)
        cachedTracks.addAll(newTracks)
        cachedTracks
    }

    private fun queryTracks(searchQuery: String? = null, limit: Int, offset: Int): List<Track> {
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

        val selectionArgs = searchQuery?.let { arrayOf("%$searchQuery%", "%$searchQuery%") }

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            if (cursor.moveToPosition(offset)) {
                var count = 0
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                    val albumCoverUri = getAlbumArtUri(albumId)

                    tracks.add(Track(id, name, artist, filePath, albumCoverUri))
                    count++
                } while (cursor.moveToNext() && count < limit)
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
