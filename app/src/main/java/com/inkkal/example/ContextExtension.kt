package com.inkkal.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Created by Abderrahim El imame on 11/14/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
fun convertToBitmap(drawable: Drawable, widthPixels: Int, heightPixels: Int): Bitmap {
    val mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mutableBitmap)
    drawable.setBounds(0, 0, widthPixels, heightPixels)
    drawable.draw(canvas)
    return mutableBitmap
}

fun getColor(color: Int, context: Context): Int {
    return ContextCompat.getColor(context, color)
}

fun getDrawable(drawable: Int, context: Context): Drawable? {
    return ContextCompat.getDrawable(context, drawable)
}