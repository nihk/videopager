package com.example.exo_viewpager_fun.models

import androidx.annotation.DrawableRes
import com.example.exo_viewpager_fun.players.AppPlayer

sealed class ViewResult

data class LoadVideoDataResult(
    val videoData: List<VideoData>
) : ViewResult()

data class CreatePlayerResult(val appPlayer: AppPlayer) : ViewResult()

object TearDownPlayerResult : ViewResult()

data class TappedPlayerResult(@DrawableRes val drawable: Int) : ViewResult()

data class OnPageSettledResult(
    val page: Int,
    val didChangeVideo: Boolean
) : ViewResult()

data class PlayerRenderingResult(val isPlayerRendering: Boolean) : ViewResult()

data class AttachPlayerToViewResult(val doAttach: Boolean) : ViewResult()

data class PlayerErrorResult(val throwable: Throwable) : ViewResult()
