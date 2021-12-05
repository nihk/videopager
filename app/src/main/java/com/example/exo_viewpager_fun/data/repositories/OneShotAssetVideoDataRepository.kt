package com.example.exo_viewpager_fun.data.repositories

import com.example.exo_viewpager_fun.models.VideoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Here for demo purposes I've used assets local to the app. This could be any other implementation,
// however. e.g. remotely fetched videos via Retrofit, Firebase, etc.
class OneShotAssetVideoDataRepository : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flowOf(AssetVideoData.all)
    }
}
