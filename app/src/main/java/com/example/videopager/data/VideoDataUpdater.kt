package com.example.videopager.data

import com.example.videopager.models.VideoData
import com.google.android.exoplayer2.ExoPlayer

interface VideoDataUpdater {
    suspend fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>)
}
