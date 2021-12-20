package com.exo.ui

import android.view.LayoutInflater
import android.view.View
import com.exo.databinding.PlayerViewBinding
import com.exo.players.ExoAppPlayer
import com.player.players.AppPlayer
import com.player.ui.AppPlayerView

/**
 * An implementation of AppPlayerView that uses ExoPlayer APIs,
 * namely [com.google.android.exoplayer2.ui.PlayerView]
 */
internal class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    private val binding = PlayerViewBinding.inflate(layoutInflater)
    override val view: View = binding.root

    override fun attach(appPlayer: AppPlayer) {
        binding.playerView.player = (appPlayer as ExoAppPlayer).player
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun detachPlayer() {
        binding.playerView.player = null
    }
}
