package com.example.exo_viewpager_fun

import android.view.LayoutInflater
import android.view.View
import com.google.android.exoplayer2.ui.PlayerView

interface AppPlayerView {
    val view: View

    fun onStart(appPlayer: AppPlayer)
    fun onStop()
}

class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    override val view: PlayerView = layoutInflater.inflate(R.layout.player_view, null) as PlayerView

    override fun onStart(appPlayer: AppPlayer) {
        view.player = (appPlayer as? ExoAppPlayer)?.exoPlayer
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun onStop() {
        view.player = null
    }
}
