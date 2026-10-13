package com.whoseturn.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object ImageLoader {

    private val memoryCache: LruCache<String, Bitmap>

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    fun load(
        url: String,
        imageView: ImageView,
        placeholderRes: Int? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        imageView.tag = url
        if (url.isEmpty()) {
            placeholderRes?.let { imageView.setImageResource(it) }
            return
        }

        val cached = memoryCache.get(url)
        if (cached != null) {
            imageView.setImageBitmap(cached)
            onSuccess?.invoke()
            return
        }

        placeholderRes?.let { imageView.setImageResource(it) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stream = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    memoryCache.put(url, bitmap)
                    withContext(Dispatchers.Main) {
                        if (imageView.tag == url) {
                            imageView.setImageBitmap(bitmap)
                            onSuccess?.invoke()
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network image error gracefully
            }
        }
    }
}
