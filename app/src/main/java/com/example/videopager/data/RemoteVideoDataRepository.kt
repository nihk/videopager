package com.example.videopager.data

import com.player.models.VideoData
import com.videopager.data.VideoDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Remote videos that worked at the time of this commit, but might not by the time you try them!
// Note that the video previews are local assets. In a real app, preview images should be served by your backend.
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
