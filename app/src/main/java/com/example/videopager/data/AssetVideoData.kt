package com.example.videopager.data

import com.player.models.VideoData

object AssetVideoData {
    private const val ASSET_PATH = "file:///android_asset"

    val waves = VideoData(
        id = "1",
        mediaUri = "$ASSET_PATH/waves.mp4",
        previewImageUri = "$ASSET_PATH/preview_waves.png",
    )
    val christmas = VideoData(
        id = "2",
        mediaUri = "$ASSET_PATH/christmas.mp4",
        previewImageUri = "$ASSET_PATH/preview_christmas.png",
    )
    val yellow = VideoData(
        id = "3",
        mediaUri = "$ASSET_PATH/yellow.mp4",
        previewImageUri = "$ASSET_PATH/preview_yellow.png",
    )

    val all = listOf(waves, christmas, yellow)
}
