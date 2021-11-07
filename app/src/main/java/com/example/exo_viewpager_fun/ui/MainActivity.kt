package com.example.exo_viewpager_fun.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.exo_viewpager_fun.App
import com.example.exo_viewpager_fun.databinding.MainActivityBinding
import com.example.exo_viewpager_fun.di.MainModule
import com.example.exo_viewpager_fun.models.AttachPlayerViewToPage
import com.example.exo_viewpager_fun.models.PlayerViewEffect
import com.example.exo_viewpager_fun.models.SettledOnPage
import com.example.exo_viewpager_fun.models.TappedPlayer
import com.example.exo_viewpager_fun.vm.MainViewModel
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

        viewModel.viewStates()
            // Only interested in valid (i.e. non-null) video data
            .filter { viewState -> viewState.videoData != null }
            .onEach { viewState ->
                // Video data is present, so populate the ViewPager2 adapter
                adapter.submitList(viewState.videoData)
                // Restore any saved page state, if possible
                val restoredPage = savedInstanceState?.consume<Int>(KEY_PAGE)
                if (restoredPage != null && adapter.canTurnToPage(restoredPage)) {
                    binding.viewPager.setCurrentItem(restoredPage, false)
                }

                // Set the player view on the active page
                adapter.attachPlayerView(appPlayerView, binding.viewPager.currentItem)

                // If the player is ready to be shown (i.e. is rendering frames), then show it
                if (viewState.showPlayer) {
                    adapter.showPlayerFor(binding.viewPager.currentItem)
                }
            }
            .launchIn(lifecycleScope)

        viewModel.viewEffects()
            .onEach { viewEffect ->
                when (viewEffect) {
                    is PlayerViewEffect -> appPlayerView.renderEffect(viewEffect)
                    is AttachPlayerViewToPage -> adapter.attachPlayerView(appPlayerView, viewEffect.page)
                }
            }
            .launchIn(lifecycleScope)

        merge(
            // Idling on a page after a scroll is a signal to try and change player playlist positions
            binding.viewPager.pageScrollStateChanges()
                .filter { state -> state == ViewPager2.SCROLL_STATE_IDLE }
                .map { SettledOnPage(binding.viewPager.currentItem) },
            // Taps on the player are signals to either play or pause the player, with animation side effects
            appPlayerView.taps().map { TappedPlayer }
        )
            .onEach(viewModel::processEvent)
            .launchIn(lifecycleScope)
    }

    private fun ListAdapter<*, *>.canTurnToPage(page: Int): Boolean {
        return page < itemCount
    }

    private fun <T> Bundle.consume(key: String): T? {
        val value = get(key) as? T
        remove(key)
        return value
    }

    override fun onStart() {
        super.onStart()
        appPlayerView.onStart(viewModel.getPlayer())
    }

    override fun onStop() {
        super.onStop()
        appPlayerView.onStop()
        // Keep player resource alive across config changes (screen rotations, theme changes, et al.)
        if (!isChangingConfigurations) {
            viewModel.tearDown()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PAGE, binding.viewPager.currentItem)
    }

    companion object {
        private const val KEY_PAGE = "page"
    }
}
