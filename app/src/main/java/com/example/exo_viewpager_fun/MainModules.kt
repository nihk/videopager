package com.example.exo_viewpager_fun

import android.content.Context
import android.view.LayoutInflater
import androidx.savedstate.SavedStateRegistryOwner
import coil.ImageLoader
import coil.imageLoader

interface MainModule {
    fun viewModelFactory(savedStateRegistryOwner: SavedStateRegistryOwner): MainViewModel.Factory
    fun appPlayerView(layoutInflater: LayoutInflater): AppPlayerView
    fun imageLoader(): ImageLoader
}

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
