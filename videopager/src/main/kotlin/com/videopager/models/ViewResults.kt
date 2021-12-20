package com.videopager.models

import androidx.annotation.DrawableRes
import com.player.models.VideoData
import com.player.players.AppPlayer

internal sealed class ViewResult

internal object NoOpResult : ViewResult()

internal data class LoadVideoDataResult(val videoData: List<VideoData>, val currentMediaItemIndex: Int) : ViewResult()

internal data class CreatePlayerResult(val appPlayer: AppPlayer) : ViewResult()

internal object TearDownPlayerResult : ViewResult()

internal data class TappedPlayerResult(@DrawableRes val drawable: Int) : ViewResult()

internal data class OnNewPageSettledResult(val page: Int) : ViewResult()

internal object OnPlayerRenderingResult : ViewResult()

internal data class AttachPlayerToViewResult(val doAttach: Boolean) : ViewResult()

internal data class PlayerErrorResult(val throwable: Throwable) : ViewResult()
