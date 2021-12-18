package com.videopager.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import coil.ImageLoader
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult

class TestImageLoader : ImageLoader {
    private val disposable = object : Disposable {
        override val isDisposed get() = true
        override fun dispose() = Unit
        override suspend fun await() = Unit
    }

    override val defaults = DefaultRequestOptions()

    override val memoryCache get() = error("Unused")

    override val bitmapPool = BitmapPool(0)

    override fun enqueue(request: ImageRequest): Disposable {
        request.target?.onStart(ColorDrawable(randomColor()))
        request.target?.onSuccess(ColorDrawable(randomColor()))
        return disposable
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        return SuccessResult(
            drawable = ColorDrawable(randomColor()),
            request = request,
            metadata = ImageResult.Metadata(
                memoryCacheKey = MemoryCache.Key(""),
                isSampled = false,
                dataSource = DataSource.MEMORY_CACHE,
                isPlaceholderMemoryCacheKeyPresent = false
            )
        )
    }

    override fun newBuilder() = error("unused")

    override fun shutdown() = Unit

    @ColorInt
    private fun randomColor(): Int {
        fun random256() = (0..256).random()
        return Color.argb(255, random256(), random256(), random256())
    }
}
