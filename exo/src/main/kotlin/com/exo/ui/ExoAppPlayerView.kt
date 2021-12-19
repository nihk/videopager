package com.exo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.exo.databinding.PlayerViewBinding
import com.exo.players.ExoAppPlayer
import com.videopager.players.AppPlayer
import com.videopager.ui.AppPlayerView

/**
 * An implementation of AppPlayerView that uses ExoPlayer APIs,
 * namely [com.google.android.exoplayer2.ui.PlayerView]
 */
class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    private val binding = PlayerViewBinding.inflate(layoutInflater)
    override val view: View = binding.root

    override fun attach(appPlayer: AppPlayer) {
        binding.playerView.player = (appPlayer as ExoAppPlayer).exoPlayer
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun detachPlayer() {
        binding.playerView.player = null
    }

    class Factory : AppPlayerView.Factory {
        override fun create(context: Context): AppPlayerView {
            return ExoAppPlayerView(context.layoutInflater)
        }
    }
}
