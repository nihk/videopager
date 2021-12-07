package com.example.exo_viewpager_fun.data

import com.example.exo_viewpager_fun.models.VideoData
import com.google.android.exoplayer2.ExoPlayer

interface VideoDataUpdater {
    suspend fun update(exoPlayer: ExoPlayer, incoming: List<VideoData>)
}
