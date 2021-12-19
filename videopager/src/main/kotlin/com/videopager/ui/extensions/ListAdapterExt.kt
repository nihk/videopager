package com.videopager.ui.extensions

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// ListAdapter submission is asynchronous. This function can be run in a coroutine to combine
// submitting a list and waiting for the diff result to be set on the adapter.
internal suspend fun <T, VH : RecyclerView.ViewHolder> ListAdapter<T, VH>.awaitList(list: List<T>?) = suspendCoroutine<Unit> { cont ->
    submitList(list) {
        cont.resume(Unit)
    }
}
