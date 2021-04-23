package com.inkkal.neararandroid.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.inkkal.neararandroid.R
import com.inkkal.neararandroid.convertToBitmap
import com.inkkal.neararandroid.getDrawable
import com.inkkal.neararandroid.helpers.ARNearUtils
import com.inkkal.neararandroid.helpers.ARNearUtils.adjustLowPassFilterAlphaValue
import com.inkkal.neararandroid.helpers.ARNearUtils.getShowUpAnimation
import com.inkkal.neararandroid.helpers.ARNearUtils.kilometers
import com.inkkal.neararandroid.models.ARNearAnimationModel
import com.inkkal.neararandroid.models.ARNearModel
import com.inkkal.neararandroid.models.CompassModel
import kotlin.math.min


internal class ARView : View {


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    private var userNamePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.ar_label_user_name)
        textSize = TEXT_USER_NAME_SIZE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private var textPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.ar_label_text)
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private var rectanglePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.ar_label_background)
        style = Paint.Style.FILL
    }

    private var animatedRectanglePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.ar_label_background)
        style = Paint.Style.STROKE
        strokeWidth =
            ANIMATED_RECTANGLE_STROKE_WIDTH
    }

    private var arLabels: List<ARNearModel>? = null
    private var animators = mutableMapOf<Int, ARNearAnimationModel>()
    private var images = mutableMapOf<Int, Bitmap>()
    private var lowPassFilterAlphaListener: ((Float) -> Unit)? = null

    companion object {
        private const val TEXT_HORIZONTAL_PADDING = 50f
        private const val TEXT_VERTICAL_PADDING = 45f
        private const val LABEL_CORNER_RADIUS = 20f
        private const val TEXT_SIZE = 40f
        private const val TEXT_USER_NAME_SIZE = 45f
        private const val ANIMATED_RECTANGLE_STROKE_WIDTH = 10f
        private const val ANIMATED_VALUE_MAX_SIZE = 120
        private const val MAX_ALPHA_VALUE = 255f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        arLabels
            ?.forEach {
                drawArLabel(canvas, it)
            }
    }

    private fun drawArLabel(canvas: Canvas, arNearModel: ARNearModel) {

        val labelText: String = if (arNearModel.distance >= 1000) {
            "${arNearModel.distance.kilometers()} Km"
        } else {
            "${arNearModel.distance} m"
        }
        val textWidthHalf = textPaint.measureText(labelText) / 2
        val textSize = textPaint.textSize
        val userNameWidthHalf = userNamePaint.measureText(arNearModel.username) / 2

        val left = arNearModel.positionX - userNameWidthHalf - TEXT_HORIZONTAL_PADDING
        val top = arNearModel.positionY - textSize - TEXT_VERTICAL_PADDING
        val right = arNearModel.positionX + userNameWidthHalf + TEXT_HORIZONTAL_PADDING
        val bottom = arNearModel.positionY + 10f//TEXT_VERTICAL_PADDING

        canvas.drawRoundRect(
            left, top, right, bottom,
            LABEL_CORNER_RADIUS,
            LABEL_CORNER_RADIUS, rectanglePaint.apply { alpha = arNearModel.alpha }
        )
        canvas.drawText(
            labelText,
            arNearModel.positionX,
            arNearModel.positionY,
            textPaint.apply { alpha = arNearModel.alpha }
        )
        canvas.drawText(
            arNearModel.username,
            arNearModel.positionX,
            arNearModel.positionY - TEXT_SIZE,
            userNamePaint.apply { alpha = arNearModel.alpha }
        )

        if (images[arNearModel.id] != null) {
            images[arNearModel.id]?.let { drawUserImage(canvas, it, arNearModel) }
        } else {
            val url = arNearModel.userImage
            if (url.isValidUrl()) {
                var uri = url.toUri()
                if (uri.scheme.isNullOrBlank()) {
                    uri = Uri.parse("http://$url")
                }
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .into(object : CustomTarget<Bitmap>() {
                        @Override
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            images[arNearModel.id] = resource

                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            val bitmap =
                                getDrawable(
                                    R.drawable.default_avatar,
                                    context,
                                )?.let { it1 ->
                                    val scale: Float = resources.displayMetrics.density
                                    val pixels = (36 * scale + 0.5f).toInt()
                                    convertToBitmap(
                                        it1,
                                        pixels,
                                        pixels
                                    )
                                }
                            bitmap?.let { drawUserImage(canvas, it, arNearModel) }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })
            } else {
                val bitmap =
                    getDrawable(
                        R.drawable.default_avatar,
                        context,
                    )?.let { it1 ->
                        val scale: Float = resources.displayMetrics.density
                        val pixels = (36 * scale + 0.5f).toInt()
                        convertToBitmap(
                            it1,
                            pixels,
                            pixels
                        )
                    }
                bitmap?.let { drawUserImage(canvas, it, arNearModel) }
            }

        }

        if (animators[arNearModel.id]?.valueAnimator?.isRunning == true) {
            applyAnimationValues(
                canvas, left, top, right, bottom,
                animators[arNearModel.id]?.animatedSize ?: 0
            )
        }
    }

    private fun String.isValidUrl(): Boolean {
        return Patterns.WEB_URL.matcher(this).matches()
    }

    private fun drawUserImage(canvas: Canvas, resource: Bitmap, arNearModel: ARNearModel) {
        /*  val scale: Float = resources.displayMetrics.density
          val pixels = (36 * scale + 0.5f).toInt()
          val bitmap: Bitmap =
              Bitmap.createScaledBitmap(resource, pixels, pixels, true)

          val paint = Paint()
          paint.isAntiAlias = true
          canvas.drawARGB(0, 0, 0, 0)
          paint.color = ContextCompat.getColor(context, R.color.ar_label_background)
          canvas.drawCircle(
              arNearModel.positionX, arNearModel.positionY - 136f,
              (bitmap.width / 2).toFloat(), paint
          )
          paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
          canvas.drawBitmap(bitmap, arNearModel.positionX - 50f, arNearModel.positionY - 186f, paint)*/
        val scale: Float = resources.displayMetrics.density
        val pixels = (36 * scale + 0.5f).toInt()
        val bitmap: Bitmap =
            Bitmap.createScaledBitmap(resource, pixels, pixels, true)

        val borderSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            context.resources.displayMetrics
        ).toInt()
        val cornerSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            context.resources.displayMetrics
        ).toInt()
        val positionX = arNearModel.positionX.toInt() - 50
        val positionY = arNearModel.positionY.toInt() - 189
        val paint = Paint()
        val rect = Rect(positionX, positionY, positionX + bitmap.width, positionY + bitmap.height)
        val rectF = RectF(rect)

        // prepare canvas for transfer
        paint.isAntiAlias = true
        paint.color = -0x1
        paint.style = Paint.Style.FILL
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawRoundRect(rectF, cornerSizePx.toFloat(), cornerSizePx.toFloat(), paint)


        // draw bitmap
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, arNearModel.positionX - 50f, arNearModel.positionY - 189f, paint)
        // draw border
        paint.color = ContextCompat.getColor(context, R.color.black)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderSizePx.toFloat()
        canvas.drawRoundRect(rectF, cornerSizePx.toFloat(), cornerSizePx.toFloat(), paint)


    }

    private fun applyAnimationValues(
        canvas: Canvas?,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        animatedRectangleSize: Int
    ) {
        animatedRectanglePaint.alpha =
            getAnimatedAlphaValue(animatedRectangleSize)
        canvas?.drawRoundRect(
            left - animatedRectangleSize, top - animatedRectangleSize,
            right + animatedRectangleSize, bottom + animatedRectangleSize,
            LABEL_CORNER_RADIUS,
            LABEL_CORNER_RADIUS, animatedRectanglePaint
        )
    }

    private fun getAnimatedAlphaValue(animatedRectangleSize: Int) =
        (MAX_ALPHA_VALUE - animatedRectangleSize * MAX_ALPHA_VALUE / ANIMATED_VALUE_MAX_SIZE).toInt()

    fun setCompassData(compassModel: CompassModel) {
        val labelsThatShouldBeShown =
            ARNearUtils.prepareLabelsProperties(compassModel, width, height)

        showAnimationIfNeeded(arLabels, labelsThatShouldBeShown)

        arLabels = labelsThatShouldBeShown

        adjustAlphaFilterValue()

        invalidate()
    }

    private fun adjustAlphaFilterValue() {
        arLabels
            ?.find { isInView(it.positionX) }
            ?.let {
                lowPassFilterAlphaListener?.invoke(
                    adjustLowPassFilterAlphaValue(it.positionX, width)
                )
            }
    }

    private fun showAnimationIfNeeded(
        labelsShownBefore: List<ARNearModel>?,
        labelsThatShouldBeShown: List<ARNearModel>
    ) {
        labelsShownBefore?.let { checkForShowingUpLabels(labelsThatShouldBeShown, it) }
            ?: labelsThatShouldBeShown.forEach {
                animators[it.id] = getShowUpAnimation()
            }
    }

    private fun checkForShowingUpLabels(
        labelsThatShouldBeShown: List<ARNearModel>,
        labelsShownBefore: List<ARNearModel>
    ) {
        labelsThatShouldBeShown
            .filterNot { newlabel -> labelsShownBefore.any { oldLabel -> newlabel.id == oldLabel.id } }
            .forEach { newLabels ->
                animators[newLabels.id] = getShowUpAnimation()
            }
    }

    fun setLowPassFilterAlphaListener(lowPassFilterAlphaListener: ((Float) -> Unit)?) {
        this.lowPassFilterAlphaListener = lowPassFilterAlphaListener
    }

    private fun isInView(positionX: Float) = positionX > 0 && positionX < width

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        setMeasuredDimension(
            measureDimension(desiredWidth, widthMeasureSpec),
            measureDimension(desiredHeight, heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }
}