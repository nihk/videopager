package com.example.exo_viewpager_fun.models

sealed class ViewEvent

object LoadVideoDataEvent : ViewEvent()

sealed class PlayerLifecycleEvent : ViewEvent() {
    object Start : PlayerLifecycleEvent()
    data class Stop(val isChangingConfigurations: Boolean) : PlayerLifecycleEvent()
}

object TappedPlayerEvent : ViewEvent()

data class OnPageSettledEvent(val page: Int) : ViewEvent()

object OnPageChangedEvent : ViewEvent()
