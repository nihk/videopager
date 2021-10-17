package com.example.exo_viewpager_fun

import kotlinx.coroutines.flow.Flow

class FakeVideoDataRepository(private val flow: Flow<List<VideoData>>) : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow
    }
}
