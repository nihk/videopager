package com.example.exo_viewpager_fun

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.exo_viewpager_fun.databinding.MainActivityBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    private val mainModule: MainModule by lazy { (application as App).mainModule }
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels { mainModule.viewModelFactory(this) }
    // Use one instance that gets attached to the ViewHolder of the active ViewPager page
    private val appPlayerView: AppPlayerView by lazy { mainModule.appPlayerView(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = PagerAdapter(mainModule.imageLoader())
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val position = binding.viewPager.currentItem
                    viewModel.playMediaAt(position)
                    adapter.attachPlayerView(appPlayerView, position)
                }
            }
        })

        viewModel.videoData
            .onEach { videoData ->
                adapter.submitList(videoData)
                val restoredPage = savedInstanceState?.consume<Int>(KEY_PAGE)
                // Only restore a page in saved state if it's a page that can actually be navigated to.
                if (restoredPage != null && adapter.itemCount >= restoredPage) {
                    binding.viewPager.setCurrentItem(restoredPage, false)
                }

                adapter.attachPlayerView(appPlayerView, binding.viewPager.currentItem)
            }
            .launchIn(lifecycleScope)

        // Only show the player when video is ready to play. This makes for a nice transition
        // from the video preview image to video content.
        viewModel.showPlayer()
            .onEach { showPlayer ->
                if (showPlayer) {
                    adapter.showPlayerFor(binding.viewPager.currentItem)
                }
            }
            .launchIn(lifecycleScope)
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
        // Keep Player resource alive across config changes
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
