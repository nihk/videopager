package com.example.videopager.models

import androidx.annotation.DrawableRes

sealed class ViewEffect

sealed class PageEffect : ViewEffect()

data class AnimationEffect(@DrawableRes val drawable: Int) : PageEffect()

object ResetAnimationsEffect : PageEffect()

data class PlayerErrorEffect(val throwable: Throwable) : ViewEffect()
