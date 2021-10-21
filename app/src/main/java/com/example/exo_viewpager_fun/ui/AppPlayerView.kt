package com.example.exo_viewpager_fun.ui

import android.view.View
import com.example.exo_viewpager_fun.models.ViewEffect
import com.example.exo_viewpager_fun.players.AppPlayer

interface AppPlayerView {
    val view: View

    fun onStart(appPlayer: AppPlayer)
    fun onStop()
    fun renderEffect(viewEffect: ViewEffect)
}
