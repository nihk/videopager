package com.example.videopager.models

import com.example.videopager.players.AppPlayer

data class ViewState(
    val appPlayer: AppPlayer? = null,
    val attachPlayer: Boolean = false,
    val page: Int = 0,
    val showPlayer: Boolean = false,
    val videoData: List<VideoData>? = null
)
