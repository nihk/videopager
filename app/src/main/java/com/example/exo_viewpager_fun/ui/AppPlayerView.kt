package com.example.exo_viewpager_fun.ui

import android.content.Context
import android.view.View
import com.example.exo_viewpager_fun.players.AppPlayer

// Abstraction over the player view. This facilitates testing and hides implementation details.
interface AppPlayerView {
    val view: View

    fun attach(appPlayer: AppPlayer)
    fun detachPlayer()

    interface Factory {
        fun create(context: Context): AppPlayerView
    }
}
