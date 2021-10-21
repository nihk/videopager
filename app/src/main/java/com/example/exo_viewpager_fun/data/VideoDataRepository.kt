package com.example.exo_viewpager_fun.data

import com.example.exo_viewpager_fun.models.VideoData
import kotlinx.coroutines.flow.Flow

interface VideoDataRepository {
    fun videoData(): Flow<List<VideoData>>
}
