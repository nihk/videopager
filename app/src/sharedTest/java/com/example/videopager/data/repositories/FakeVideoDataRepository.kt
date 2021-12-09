package com.example.videopager.data.repositories

import com.example.videopager.models.VideoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class FakeVideoDataRepository(private val flow: Flow<List<VideoData>?>) : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow.filterNotNull()
    }
}
