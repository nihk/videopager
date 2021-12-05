package com.example.exo_viewpager_fun.data.repositories

import com.example.exo_viewpager_fun.models.VideoData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

class RedditVideoDataRepository : VideoDataRepository {
    private val api = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .baseUrl("https://old.reddit.com/")
        .build()
        .create(RedditService::class.java)

    override fun videoData(): Flow<List<VideoData>> = flow {
        val response = api.tikTokCringe()
        val videoData = response
            .data
            .posts
            .map { post ->
                VideoData(
                    mediaUri = post.data.secureMedia?.video?.hlsUrl.orEmpty(),
                    previewImageUri = post.data.preview.images.first().source.url
                )
            }
            .filter { videoData -> videoData.mediaUri.isNotEmpty() }

        emit(videoData)
    }

    private interface RedditService {
        @GET("/r/tiktokcringe/top.json?raw_json=1")
        suspend fun tikTokCringe(): RedditResponse
    }

    @JsonClass(generateAdapter = true)
    internal data class RedditResponse(
        val data: Data1
    ) {
        @JsonClass(generateAdapter = true)
        data class Data1(
            @Json(name = "children")
            val posts: List<Post>
        ) {
            @JsonClass(generateAdapter = true)
            data class Post(
                val data: Data2
            ) {
                @JsonClass(generateAdapter = true)
                data class Data2(
                    @Json(name = "secure_media")
                    val secureMedia: SecureMedia?,
                    val preview: Preview
                ) {
                    @JsonClass(generateAdapter = true)
                    data class SecureMedia(
                        @Json(name = "reddit_video")
                        val video: Video
                    ) {
                        @JsonClass(generateAdapter = true)
                        data class Video(
                            val width: Int,
                            val height: Int,
                            val duration: Int,
                            @Json(name = "hls_url")
                            val hlsUrl: String,
                            @Json(name = "dash_url")
                            val dashUrl: String
                        )
                    }

                    @JsonClass(generateAdapter = true)
                    data class Preview(
                        val images: List<Image>
                    ) {
                        @JsonClass(generateAdapter = true)
                        data class Image(
                            val source: Source
                        ) {
                            @JsonClass(generateAdapter = true)
                            data class Source(
                                val url: String,
                                val width: Int,
                                val height: Int
                            )
                        }
                    }
                }
            }
        }
    }
}
