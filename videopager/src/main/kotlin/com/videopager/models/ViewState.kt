package com.videopager.models

import com.player.models.VideoData
import com.player.players.AppPlayer

internal data class ViewState(
    val appPlayer: AppPlayer? = null,
    val attachPlayer: Boolean = false,
    val page: Int = 0,
    val showPlayer: Boolean = false,
    val videoData: List<VideoData>? = null
)
