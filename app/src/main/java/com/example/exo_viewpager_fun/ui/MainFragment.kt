package com.example.exo_viewpager_fun.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import com.example.exo_viewpager_fun.R
import com.example.exo_viewpager_fun.databinding.MainFragmentBinding
import com.example.exo_viewpager_fun.models.OnPageSettledEvent
import com.example.exo_viewpager_fun.models.PlayerErrorEffect
import com.example.exo_viewpager_fun.models.PlayerLifecycleEvent
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.models.TappedPlayerEvent
import com.example.exo_viewpager_fun.models.ViewEvent
import com.example.exo_viewpager_fun.ui.extensions.events
import com.example.exo_viewpager_fun.ui.extensions.pageScrollStateChanges
import com.example.exo_viewpager_fun.vm.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

class MainFragment(
    private val viewModelFactory: MainViewModel.Factory,
    private val appPlayerViewFactory: AppPlayerView.Factory,
    private val imageLoader: ImageLoader
) : Fragment(R.layout.main_fragment) {
    private val viewModel: MainViewModel by viewModels { viewModelFactory.create(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = MainFragmentBinding.bind(view)
        // This single player view instance gets attached to the ViewHolder of the active ViewPager page
        val appPlayerView = appPlayerViewFactory.create(view.context)

        val adapter = PagerAdapter(imageLoader)
        binding.viewPager.adapter = adapter

        viewModel.states
            .onEach { state ->
                adapter.submitList(state.videoData)

                if (state.attachPlayer && state.appPlayer != null) {
                    appPlayerView.attach(state.appPlayer)
                } else {
                    appPlayerView.detachPlayer()
                }

                if (adapter.hasPage(state.page)) {
                    binding.viewPager.setCurrentItem(state.page, false)
                }

                // Set the player view on the active page
                adapter.attachPlayerView(appPlayerView, binding.viewPager.currentItem)

                // If the player media is not loading (i.e. is rendering frames), then show the player
                if (!state.isLoading) {
                    adapter.showPlayerFor(binding.viewPager.currentItem)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is PlayerViewEffect -> appPlayerView.renderEffect(effect)
                    is PlayerErrorEffect -> Snackbar.make(
                        binding.root,
                        effect.throwable.message ?: "Error",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        merge(
            lifecycle.viewEvents(),
            binding.viewPager.viewEvents(),
            appPlayerView.viewEvents()
        )
            .onEach(viewModel::processEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun Lifecycle.viewEvents(): Flow<ViewEvent> {
        return events()
            .filter { event -> event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_STOP }
            .map { event ->
                when (event) {
                    Lifecycle.Event.ON_START -> PlayerLifecycleEvent.Start
                    Lifecycle.Event.ON_STOP -> PlayerLifecycleEvent.Stop(requireActivity().isChangingConfigurations)
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

    private fun ListAdapter<*, *>.hasPage(page: Int): Boolean {
        return page < itemCount
    }
}