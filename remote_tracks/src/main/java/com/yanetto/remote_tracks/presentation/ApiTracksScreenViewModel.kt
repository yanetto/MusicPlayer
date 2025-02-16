package com.yanetto.remote_tracks.presentation

import com.yanetto.common_tracks.presentation.TracksScreenViewModel
import com.yanetto.music_player.domain.MusicPlayerController
import com.yanetto.remote_tracks.data.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApiTracksScreenViewModel @Inject constructor(
    repository: ApiRepository,
    musicPlayer: MusicPlayerController
) : TracksScreenViewModel(repository, musicPlayer)