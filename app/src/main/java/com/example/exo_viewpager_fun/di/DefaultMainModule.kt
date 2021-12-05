package com.example.exo_viewpager_fun.di

import android.content.Context
import android.view.LayoutInflater
import androidx.savedstate.SavedStateRegistryOwner
import coil.ImageLoader
import coil.imageLoader
import com.example.exo_viewpager_fun.data.RecyclerViewVideoDataUpdater
import com.example.exo_viewpager_fun.data.repositories.OneShotAssetVideoDataRepository
import com.example.exo_viewpager_fun.players.ExoAppPlayer
import com.example.exo_viewpager_fun.ui.AppPlayerView
import com.example.exo_viewpager_fun.ui.ExoAppPlayerView
import com.example.exo_viewpager_fun.vm.MainViewModel

class DefaultMainModule(private val context: Context) : MainModule {
    override fun viewModelFactory(savedStateRegistryOwner: SavedStateRegistryOwner): MainViewModel.Factory {
        return MainViewModel.Factory(
            repository = OneShotAssetVideoDataRepository(),
            appPlayerFactory = ExoAppPlayer.Factory(
                context = context,
                updater = RecyclerViewVideoDataUpdater()
            ),
            savedStateRegistryOwner = savedStateRegistryOwner
        )
    }

    override fun appPlayerView(layoutInflater: LayoutInflater): AppPlayerView {
        return ExoAppPlayerView(layoutInflater)
    }

    override fun imageLoader(): ImageLoader {
        return context.imageLoader
    }
}
