package com.inkkal.example


import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule

/**
 * Created by Abderrahim El imame on 10/12/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@GlideModule
class AppGlideModule : AppGlideModule() {
    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setLogLevel(Log.ERROR)
        // set disk cache size & external vs. internal
        val cacheSize100MegaBytes = 104857600
        builder.setDiskCache(
            DiskLruCacheFactory(
                App.appContext.cacheDir.absolutePath,
                cacheSize100MegaBytes.toLong()
            )
        ) //change location of cache even user clear cache of  the app

    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {}

    class NoopDiskCacheFactory : DiskCache.Factory {
        override fun build(): DiskCache? {
            return DiskCacheAdapter()
        }
    }
}