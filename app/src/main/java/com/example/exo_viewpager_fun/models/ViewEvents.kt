package com.example.exo_viewpager_fun.models

sealed class ViewEvent
object LoadVideoDataEvent : ViewEvent()
data class PlayerLifecycleEvent(val type: Type) : ViewEvent() {
    sealed class Type {
        object Start : Type()
        data class Stop(val isChangingConfigurations: Boolean) : Type()
    }
}
data class AttachPlayerToViewEvent(val doAttach: Boolean) : ViewEvent()
object TappedPlayerEvent : ViewEvent()
data class OnPageSettledEvent(val page: Int) : ViewEvent()
