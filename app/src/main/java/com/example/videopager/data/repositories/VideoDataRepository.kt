package com.example.videopager.data.repositories

import com.example.videopager.models.VideoData
import kotlinx.coroutines.flow.Flow

interface VideoDataRepository {
    fun videoData(): Flow<List<VideoData>>
}
