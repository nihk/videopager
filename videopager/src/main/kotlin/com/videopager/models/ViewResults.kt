package com.videopager.models

import androidx.annotation.DrawableRes
import com.videopager.players.AppPlayer

sealed class ViewResult

object NoOpResult : ViewResult()

data class LoadVideoDataResult(val videoData: List<VideoData>) : ViewResult()

data class CreatePlayerResult(val appPlayer: AppPlayer) : ViewResult()

object TearDownPlayerResult : ViewResult()

data class TappedPlayerResult(@DrawableRes val drawable: Int) : ViewResult()

data class OnNewPageSettledResult(val page: Int) : ViewResult()

object OnPlayerRenderingResult : ViewResult()

data class AttachPlayerToViewResult(val doAttach: Boolean) : ViewResult()

data class PlayerErrorResult(val throwable: Throwable) : ViewResult()
