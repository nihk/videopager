package com.example.exo_viewpager_fun.data

import com.example.exo_viewpager_fun.data.VideoDataRepository
import com.example.exo_viewpager_fun.models.VideoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class FakeVideoDataRepository(private val flow: Flow<List<VideoData>?>) : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow.filterNotNull()
    }
}
