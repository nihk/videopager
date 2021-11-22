package com.example.exo_viewpager_fun.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.exo_viewpager_fun.App
import com.example.exo_viewpager_fun.databinding.MainActivityBinding
import com.example.exo_viewpager_fun.di.MainModule
import com.example.exo_viewpager_fun.models.OnPageSettledEvent
import com.example.exo_viewpager_fun.models.PlayerLifecycleEvent
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.models.TappedPlayerEvent
import com.example.exo_viewpager_fun.models.ViewEvent
import com.example.exo_viewpager_fun.ui.extensions.events
import com.example.exo_viewpager_fun.ui.extensions.pageScrollStateChanges
import com.example.exo_viewpager_fun.vm.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    // Manual dependency injection
    private val mainModule: MainModule by lazy { (application as App).mainModule }
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels { mainModule.viewModelFactory(this) }
    // This single player view instance gets attached to the ViewHolder of the active ViewPager page
    private val appPlayerView: AppPlayerView by lazy { mainModule.appPlayerView(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = PagerAdapter(mainModule.imageLoader())
        binding.viewPager.adapter = adapter

        viewModel.states
            .onEach { state ->
                adapter.submitList(state.videoData)

                if (state.attachPlayer && state.appPlayer != null) {
                    appPlayerView.attach(state.appPlayer)
                } else {
                    appPlayerView.detachPlayer()
                }

                if (adapter.canTurnToPage(state.page)) {
                    binding.viewPager.setCurrentItem(state.page, false)
                }

                // Set the player view on the active page
                adapter.attachPlayerView(appPlayerView, binding.viewPager.currentItem)

                // If the player is ready to be shown (i.e. is rendering frames), then show it
                if (state.showPlayer) {
                    adapter.showPlayerFor(binding.viewPager.currentItem)
                }
            }
            .launchIn(lifecycleScope)

        viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is PlayerViewEffect -> appPlayerView.renderEffect(effect)
                }
            }
            .launchIn(lifecycleScope)

        merge(
            lifecycle.viewEvents(),
            binding.viewPager.viewEvents(),
            appPlayerView.viewEvents()
        )
            .onEach(viewModel::processEvent)
            .launchIn(lifecycleScope)
    }

    private fun Lifecycle.viewEvents(): Flow<ViewEvent> {
        return events()
            .filter { event -> event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_STOP }
            .map { event ->
                when (event) {
                    Lifecycle.Event.ON_START -> PlayerLifecycleEvent.Start
                    Lifecycle.Event.ON_STOP -> PlayerLifecycleEvent.Stop(isChangingConfigurations)
                    else -> error("Unhandled event: $event")
                }
            }
    }

    private fun ViewPager2.viewEvents(): Flow<ViewEvent> {
        // Idling on a page after a scroll is a signal to try and change player playlist positions
        return pageScrollStateChanges()
            .filter { state -> state == ViewPager2.SCROLL_STATE_IDLE }
            .map { OnPageSettledEvent(currentItem) }
    }

    private fun AppPlayerView.viewEvents(): Flow<ViewEvent> {
        // Taps on the player are signals to either play or pause the player, with animation side effects
        return taps().map { TappedPlayerEvent }
    }

    private fun ListAdapter<*, *>.canTurnToPage(page: Int): Boolean {
        return page < itemCount
    }
}
