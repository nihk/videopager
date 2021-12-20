package com.example.videopager.data

import com.player.models.VideoData
import com.videopager.data.VideoDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
