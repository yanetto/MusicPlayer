package com.yanetto.local_tracks.data.repository
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.yanetto.common_model.model.Track
import com.yanetto.common_model.repository.TracksRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

internal class MediaStoreTracksRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TracksRepository {

    companion object {
        private const val PAGE_SIZE = 25
        private const val TAG = "MEDIA_STORE_REPOSITORY"

        private const val ALL_MUSIC = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        private const val FILTERED_MUSIC = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                "(${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        private const val SORT_ORDER = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
    }

    private var lastOffset = 0
    private val cachedTracks = mutableListOf<Track>()

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM
    )

    override suspend fun getTracks(): List<Track> {
        return try {
            cachedTracks.clear()
            lastOffset = 0
            val newTracks = queryTracks(limit = PAGE_SIZE, offset = lastOffset)
            cachedTracks.addAll(newTracks)
            cachedTracks
        } catch (cancellationException: CancellationException) {
            Log.e(TAG, cancellationException.stackTraceToString())
            cachedTracks
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }

    }

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            cachedTracks.clear()
            lastOffset = 0
            val newTracks = queryTracks(searchQuery = query, limit = PAGE_SIZE, offset = lastOffset)
            cachedTracks.addAll(newTracks)
            cachedTracks
        } catch (cancellationException: CancellationException) {
            Log.e(TAG, cancellationException.stackTraceToString())
            cachedTracks
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }
    }

    override suspend fun loadNext(): List<Track> {
        return try {
            lastOffset += PAGE_SIZE
            val newTracks = queryTracks(limit = PAGE_SIZE, offset = lastOffset)
            cachedTracks.addAll(newTracks)
            cachedTracks
        } catch (cancellationException: CancellationException) {
            Log.e(TAG, cancellationException.stackTraceToString())
            cachedTracks
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            throw e
        }
    }

    private fun queryTracks(searchQuery: String? = null, limit: Int, offset: Int): List<Track> {
        val tracks = mutableListOf<Track>()
        val contentResolver = context.contentResolver

        val selection = if (searchQuery.isNullOrBlank()) {
            ALL_MUSIC
        } else {
            FILTERED_MUSIC
        }

        val selectionArgs = searchQuery?.let { arrayOf("%$searchQuery%", "%$searchQuery%") }

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, SORT_ORDER
        )?.use { cursor ->
            if (cursor.moveToPosition(offset)) {
                var count = 0
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val mediaUri =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val albumTitle =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

                    val albumCoverUri = getAlbumArtUri(albumId)

                    tracks.add(
                        Track(id, name, artist, mediaUri, albumTitle, albumCoverUri)
                    )
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
