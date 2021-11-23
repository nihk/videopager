package com.example.exo_viewpager_fun.models

import androidx.annotation.DrawableRes

sealed class ViewEffect
sealed class PlayerViewEffect : ViewEffect()
data class AnimationEffect(@DrawableRes val drawable: Int) : PlayerViewEffect()
object ResetAnimationsEffect : PlayerViewEffect()
