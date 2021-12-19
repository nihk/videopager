package com.videopager.models

internal sealed class ViewEvent

internal object LoadVideoDataEvent : ViewEvent()

internal sealed class PlayerLifecycleEvent : ViewEvent() {
    object Start : PlayerLifecycleEvent()
    data class Stop(val isChangingConfigurations: Boolean) : PlayerLifecycleEvent()
}

internal object TappedPlayerEvent : ViewEvent()

internal data class OnPageSettledEvent(val page: Int) : ViewEvent()

internal object PauseVideoEvent : ViewEvent()
