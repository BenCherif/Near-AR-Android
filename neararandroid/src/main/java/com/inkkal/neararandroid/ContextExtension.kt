package com.inkkal.neararandroid

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
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


fun Bitmap.getRoundedCornerBitmap(context: Context, roundPixelSize: Int): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)
    val roundPx = roundPixelSize.toFloat()

    val borderSizePx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        3f,
        context.resources.displayMetrics
    ).toInt()
    paint.isAntiAlias = true
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)

    // draw border
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderSizePx.toFloat()
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    return output
}