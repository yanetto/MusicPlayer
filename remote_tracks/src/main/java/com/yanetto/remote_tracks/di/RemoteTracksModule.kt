package com.yanetto.remote_tracks.di

import com.yanetto.common_model.repository.TracksRepository
import com.yanetto.remote_tracks.data.repository.ApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemoteTracksModule {

    @Provides
    @Singleton
    fun provideApiRepository(client: OkHttpClient): TracksRepository {
        return ApiRepository(client)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
