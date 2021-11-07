package com.example.exo_viewpager_fun.ui

import android.view.View
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.players.AppPlayer
import kotlinx.coroutines.flow.Flow

// Abstraction over the player view. This facilitates testing and hides implementation details.
interface AppPlayerView {
    val view: View

    fun onStart(appPlayer: AppPlayer)
    fun onStop()
    fun renderEffect(playerViewEffect: PlayerViewEffect)
    fun taps(): Flow<Unit>
}
