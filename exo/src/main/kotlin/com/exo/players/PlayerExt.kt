package com.exo.players

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

internal val Player.currentMediaItems: List<MediaItem> get() {
    return List(mediaItemCount, ::getMediaItemAt)
}

internal fun Player.loopVideos() {
    repeatMode = Player.REPEAT_MODE_ONE
}
