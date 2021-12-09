package com.example.videopager.models

import com.example.videopager.players.AppPlayer

data class ViewState(
    val appPlayer: AppPlayer? = null,
    val attachPlayer: Boolean = false,
    val page: Int = 0,
    val isLoading: Boolean = true,
    val videoData: List<VideoData>? = null
)
