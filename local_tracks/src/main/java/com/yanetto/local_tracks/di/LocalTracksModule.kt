package com.yanetto.local_tracks.di

import android.content.Context
import com.yanetto.common_model.repository.TracksRepository
import com.yanetto.local_tracks.data.repository.MediaStoreTracksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalTracksModule {

    @Provides
    @Singleton
    fun provideTracksRepository(
        @ApplicationContext context: Context
    ): TracksRepository {
        return MediaStoreTracksRepository(context)
    }
}
