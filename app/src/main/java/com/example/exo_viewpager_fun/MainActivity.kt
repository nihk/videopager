package com.example.exo_viewpager_fun

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.exo_viewpager_fun.databinding.MainActivityBinding
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, this)
    }
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerView = PlayerView(this).apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
        val adapter = PagerAdapter(playerView, videoUris)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val pageHasSettled = positionOffsetPixels == 0
                if (pageHasSettled) {
                    adapter.onPageSettled(position)
                    viewModel.playMediaAt(position)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        playerView.player = viewModel.getPlayer()
    }

    override fun onStop() {
        super.onStop()
        playerView.player = null
        // Keep Player resource alive across config changes
        if (!isChangingConfigurations) {
            viewModel.tearDown()
        }
    }
}
