package com.example.videopager.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.videopager.R
import com.example.videopager.databinding.PlayerViewBinding
import com.example.videopager.players.AppPlayer
import com.example.videopager.players.ExoAppPlayer
import com.example.videopager.ui.extensions.layoutInflater

/**
 * An implementation of AppPlayerView that uses ExoPlayer APIs,
 * namely [com.google.android.exoplayer2.ui.PlayerView]
 */
class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    override val view: View = layoutInflater.inflate(R.layout.player_view, null)
    private val binding = PlayerViewBinding.bind(view)

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
