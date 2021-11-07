package com.example.exo_viewpager_fun.ui

import android.view.LayoutInflater
import android.view.View
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.models.ShowPauseAnimation
import com.example.exo_viewpager_fun.models.ShowPlayAnimation
import com.example.exo_viewpager_fun.databinding.PlayerViewBinding
import com.example.exo_viewpager_fun.models.ResetAnyPlayPauseAnimations
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.players.AppPlayer
import com.example.exo_viewpager_fun.players.ExoAppPlayer
import kotlinx.coroutines.flow.Flow

/**
 * An implementation of AppPlayerView that uses ExoPlayer APIs,
 * namely [com.google.android.exoplayer2.ui.PlayerView]
 */
class ExoAppPlayerView(layoutInflater: LayoutInflater) : AppPlayerView {
    override val view: View = layoutInflater.inflate(R.layout.player_view, null)
    private val binding = PlayerViewBinding.bind(view)
    private val animationEffect = FadeInThenOutAnimationEffect(binding.playPause)

    override fun onStart(appPlayer: AppPlayer) {
        binding.playerView.player = (appPlayer as? ExoAppPlayer)?.exoPlayer
    }

    // ExoPlayer and PlayerView hold circular ref's to each other, so avoid leaking
    // Activity here by nulling it out.
    override fun onStop() {
        binding.playerView.player = null
    }

    override fun renderEffect(playerViewEffect: PlayerViewEffect) {
        when (playerViewEffect) {
            ResetAnyPlayPauseAnimations -> animationEffect.reset()
            ShowPauseAnimation, ShowPlayAnimation -> {
                val drawableRes = if (playerViewEffect is ShowPlayAnimation) {
                    R.drawable.play
                } else {
                    R.drawable.pause
                }
                binding.playPause.setImageResource(drawableRes)
                animationEffect.go()
            }
        }
    }

    override fun taps(): Flow<Unit> = view.taps()
}
