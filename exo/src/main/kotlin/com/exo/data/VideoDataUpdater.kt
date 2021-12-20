package com.exo.data

import com.google.android.exoplayer2.Player
import com.player.models.VideoData

internal interface VideoDataUpdater {
    suspend fun update(player: Player, incoming: List<VideoData>)
}
