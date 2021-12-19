package com.example.videopager.di

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import coil.imageLoader
import com.exo.data.RecyclerViewVideoDataUpdater
import com.example.videopager.data.RedditVideoDataRepository
import com.exo.players.ExoAppPlayer
import com.exo.ui.ExoAppPlayerView
import com.videopager.ui.VideoPagerFragment
import com.videopager.vm.VideoPagerViewModel
import kotlinx.coroutines.Dispatchers

class MainModule(activity: ComponentActivity) {
    val fragmentFactory: FragmentFactory = object : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return when (loadFragmentClass(classLoader, className)) {
                VideoPagerFragment::class.java -> VideoPagerFragment(
                    viewModelFactory = VideoPagerViewModel.Factory(
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
