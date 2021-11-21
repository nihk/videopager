package com.example.exo_viewpager_fun.players

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

val ExoPlayer.currentMediaItems: List<MediaItem> get() {
    val mediaItems = mutableListOf<MediaItem>()

    for (i in 0 until mediaItemCount) {
        mediaItems += getMediaItemAt(i)
    }

    return mediaItems
}

fun ExoPlayer.loopVideos() {
    repeatMode = Player.REPEAT_MODE_ONE
}
