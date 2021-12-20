package com.videopager.data

import com.player.models.VideoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class FakeVideoDataRepository(private val flow: Flow<List<VideoData>?>) : VideoDataRepository {
    override fun videoData(): Flow<List<VideoData>> {
        return flow.filterNotNull()
    }
}
