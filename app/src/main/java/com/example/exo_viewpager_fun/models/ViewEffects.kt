package com.example.exo_viewpager_fun.models

sealed interface ViewEffect
sealed class PlayerViewEffect : ViewEffect

object ShowPauseAnimation : PlayerViewEffect()
object ShowPlayAnimation : PlayerViewEffect()
object ResetAnyPlayPauseAnimations : PlayerViewEffect()

data class AttachPlayerViewToPage(val page: Int) : ViewEffect
