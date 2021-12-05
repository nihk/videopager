package com.example.exo_viewpager_fun.di

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import coil.imageLoader
import com.example.exo_viewpager_fun.data.RecyclerViewVideoDataUpdater
import com.example.exo_viewpager_fun.data.repositories.RedditVideoDataRepository
import com.example.exo_viewpager_fun.players.ExoAppPlayer
import com.example.exo_viewpager_fun.ui.ExoAppPlayerView
import com.example.exo_viewpager_fun.ui.MainFragment
import com.example.exo_viewpager_fun.vm.MainViewModel

class MainModule(activity: ComponentActivity) {
    val fragmentFactory: FragmentFactory = object : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return when (loadFragmentClass(classLoader, className)) {
                MainFragment::class.java -> MainFragment(
                    viewModelFactory = MainViewModel.Factory(
                        repository = RedditVideoDataRepository(),
                        appPlayerFactory = ExoAppPlayer.Factory(
                            context = activity.applicationContext,
                            updater = RecyclerViewVideoDataUpdater()
                        )
                    ),
                    appPlayerViewFactory = ExoAppPlayerView.Factory(),
                    imageLoader = activity.imageLoader
                )
                else -> super.instantiate(classLoader, className)
            }
        }
    }
}
