package com.example.exo_viewpager_fun

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

interface VideoDataRepository {
    fun videoData(): Flow<List<VideoData>>
}

// Remote videos that worked at the time of this commit, but might not by the time you try them!
// Note that the video previews are local assets. In a real app, they should be served by your backend.
class RemoteVideoDataRepository : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        val videoData = listOf(
            AssetVideoData.waves.copy(mediaUri = "https://assets.mixkit.co/videos/preview/mixkit-waves-in-the-water-1164-large.mp4"),
            AssetVideoData.christmas.copy(mediaUri = "https://assets.mixkit.co/videos/preview/mixkit-the-spheres-of-a-christmas-tree-2720-large.mp4"),
            AssetVideoData.yellow.copy(mediaUri = "https://assets.mixkit.co/videos/preview/mixkit-tree-with-yellow-flowers-1173-large.mp4")
        )
        return flowOf(videoData)
    }
}

// Here for demo purposes I've used assets local to the app. This could be any other implementation,
// however. e.g. remotely fetched videos via Retrofit, Firebase, etc.
class OneShotAssetVideoDataRepository : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flowOf(AssetVideoData.all)
    }
}

// Simulate a stream of data, not unlike pagination. Note the support for emissions that shuffle
// up the order compared to previous emissions.
class StreamingAssetVideoDataRepository : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow {
            emit(listOf(AssetVideoData.waves))
            delay(2_000L)
            emit(listOf(AssetVideoData.waves, AssetVideoData.christmas))
            delay(2_000L)
            emit(listOf(AssetVideoData.yellow, AssetVideoData.waves, AssetVideoData.christmas))
        }
    }
}

private object AssetVideoData {
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
