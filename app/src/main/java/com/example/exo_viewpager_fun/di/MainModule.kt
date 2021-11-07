package com.example.exo_viewpager_fun.di

import android.view.LayoutInflater
import androidx.savedstate.SavedStateRegistryOwner
import coil.ImageLoader
import com.example.exo_viewpager_fun.ui.AppPlayerView
import com.example.exo_viewpager_fun.vm.MainViewModel

// The app-scoped dependency injection graph needed for this project.
interface MainModule {
    fun viewModelFactory(savedStateRegistryOwner: SavedStateRegistryOwner): MainViewModel.Factory
    fun appPlayerView(layoutInflater: LayoutInflater): AppPlayerView
    fun imageLoader(): ImageLoader
}
