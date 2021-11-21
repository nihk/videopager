package com.example.exo_viewpager_fun.models

sealed interface ViewEvent
object LoadVideoDataEvent : ViewEvent
sealed class PlayerLifecycleEvent : ViewEvent {
    object Start : PlayerLifecycleEvent()
    data class Stop(val isChangingConfigurations: Boolean) : PlayerLifecycleEvent()
}
data class AttachPlayerToViewEvent(val doAttach: Boolean) : ViewEvent
object TappedPlayerEvent : ViewEvent
data class OnPageSettledEvent(val page: Int) : ViewEvent
