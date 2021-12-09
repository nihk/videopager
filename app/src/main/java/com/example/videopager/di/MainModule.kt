package com.example.videopager.di

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import coil.imageLoader
import com.example.videopager.data.RecyclerViewVideoDataUpdater
import com.example.videopager.data.repositories.RedditVideoDataRepository
import com.example.videopager.players.ExoAppPlayer
import com.example.videopager.ui.ExoAppPlayerView
import com.example.videopager.ui.MainFragment
import com.example.videopager.vm.MainViewModel
import kotlinx.coroutines.Dispatchers

class MainModule(activity: ComponentActivity) {
    val fragmentFactory: FragmentFactory = object : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return when (loadFragmentClass(classLoader, className)) {
                MainFragment::class.java -> MainFragment(
                    viewModelFactory = MainViewModel.Factory(
                        repository = RedditVideoDataRepository(),
                        appPlayerFactory = ExoAppPlayer.Factory(
                            context = activity.applicationContext,
                            updater = RecyclerViewVideoDataUpdater(diffingContext = Dispatchers.Default)
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
