package com.example.exo_viewpager_fun.models

sealed class ViewEvent
object TappedPlayer : ViewEvent()
data class SettledOnPage(val page: Int) : ViewEvent()
