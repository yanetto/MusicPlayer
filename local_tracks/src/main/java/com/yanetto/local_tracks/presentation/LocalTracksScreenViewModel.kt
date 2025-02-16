package com.yanetto.local_tracks.presentation

import com.yanetto.common_tracks.presentation.TracksScreenViewModel
import com.yanetto.local_tracks.data.repository.MediaStoreTracksRepository
import com.yanetto.music_player.domain.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocalTracksScreenViewModel @Inject constructor(
    repository: MediaStoreTracksRepository,
    musicPlayer: MusicPlayerController
) : TracksScreenViewModel(repository, musicPlayer)
