package com.example.exo_viewpager_fun

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameUriFetcher
import com.example.exo_viewpager_fun.databinding.MainActivityBinding
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, AssetVideoDataRepository(), this)
    }
    private val imageLoader by lazy {
        val context = this
        ImageLoader.Builder(context)
            .componentRegistry {
                add(VideoFrameUriFetcher(context))
                add(VideoFrameDecoder(context)) // Fallback
            }
            .build()
    }
    // Use one PlayerView instance that gets attached to the ViewHolder of the active ViewPager page
    private val playerView: PlayerView by lazy {
        layoutInflater.inflate(R.layout.player_view, null) as PlayerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = PagerAdapter(playerView, imageLoader)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // onPageScrollStateChanged would also work, but onPageScrolled in contrast fires
            // an initial callback here for the current page when items are submitted to the
            // ViewPager adapter. This is convenient for setting up the current page.
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val pageSettled = positionOffsetPixels == 0
                if (pageSettled) {
                    viewModel.playMediaAt(position)
                    adapter.onPageSettled(position)
                }
            }
        })

        viewModel.videoData
            .filter { videoData -> videoData.isNotEmpty() }
            .onEach { videoData ->
                adapter.submitList(videoData)

                val restoredPage = savedInstanceState?.consume<Int>(KEY_PAGE)
                if (restoredPage != null) {
                    binding.viewPager.setCurrentItem(restoredPage, false)
                }
            }
            .launchIn(lifecycleScope)

        // Only show the PlayerView when video is ready to play. This makes for a nice transition
        // from the video preview image to video content.
        viewModel.showPlayer()
            .onEach { showPlayer -> playerView.isVisible = showPlayer }
            .launchIn(lifecycleScope)
    }

    private fun <T> Bundle.consume(key: String): T? {
        val value = get(key) as? T
        remove(key)
        return value
    }

    override fun onStart() {
        super.onStart()
        playerView.player = viewModel.getPlayer()
    }

    override fun onStop() {
        super.onStop()
        // Player and PlayerView hold circular ref's to each other, so avoid leaking Activity here
        // by nulling it out.
        playerView.player = null
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
