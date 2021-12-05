package com.example.exo_viewpager_fun.data.repositories

import com.example.exo_viewpager_fun.models.VideoData

object AssetVideoData {
    private const val ASSET_PATH = "file:///android_asset"

    val waves = VideoData(
        mediaUri = "$ASSET_PATH/waves.mp4",
        previewImageUri = "$ASSET_PATH/preview_waves.png",
    )
    val christmas = VideoData(
        mediaUri = "$ASSET_PATH/christmas.mp4",
        previewImageUri = "$ASSET_PATH/preview_christmas.png",
    )
    val yellow = VideoData(
        mediaUri = "$ASSET_PATH/yellow.mp4",
        previewImageUri = "$ASSET_PATH/preview_yellow.png",
    )

    val all = listOf(waves, christmas, yellow)
}
