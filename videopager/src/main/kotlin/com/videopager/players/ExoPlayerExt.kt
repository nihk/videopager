package com.videopager.players

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

internal val ExoPlayer.currentMediaItems: List<MediaItem> get() {
    return List(mediaItemCount, ::getMediaItemAt)
}

internal fun ExoPlayer.loopVideos() {
    repeatMode = Player.REPEAT_MODE_ONE
}
