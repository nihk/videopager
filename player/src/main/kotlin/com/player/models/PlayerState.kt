package com.player.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    val currentMediaItemId: String?,
    val currentMediaItemIndex: Int? = null,
    val seekPositionMillis: Long,
    val isPlaying: Boolean
) : Parcelable {
    companion object {
        val INITIAL = PlayerState(
            currentMediaItemId = null,
            currentMediaItemIndex = null,
            seekPositionMillis = 0L,
            isPlaying = true
        )
    }
}
