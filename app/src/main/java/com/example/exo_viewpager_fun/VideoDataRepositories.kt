package com.example.exo_viewpager_fun

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface VideoDataRepository {
    fun videoData(): Flow<List<VideoData>>
}

class AssetVideoDataRepository : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flowOf(videoData)
    }

    companion object {
        private const val ASSET_PATH = "file:///android_asset"

        private val videoData = listOf(
            VideoData(
                mediaUri = "$ASSET_PATH/waves.mp4",
                previewImageUri = "$ASSET_PATH/preview_waves.png",
            ),
            VideoData(
                mediaUri = "$ASSET_PATH/christmas.mp4",
                previewImageUri = "$ASSET_PATH/preview_christmas.png",
            ),
            VideoData(
                mediaUri = "$ASSET_PATH/yellow.mp4",
                previewImageUri = "$ASSET_PATH/preview_yellow.png",
            ),
        )
    }
}
