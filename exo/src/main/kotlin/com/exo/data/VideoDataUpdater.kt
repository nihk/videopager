package com.exo.data

import com.google.android.exoplayer2.ExoPlayer
import com.videopager.models.VideoData

internal interface VideoDataUpdater {
    suspend fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>)
}
