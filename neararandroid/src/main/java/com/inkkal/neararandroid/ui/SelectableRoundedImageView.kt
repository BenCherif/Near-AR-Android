package com.inkkal.neararandroid.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.inkkal.neararandroid.R
import timber.log.Timber

/**
 * Created by Abderrahim El imame on 4/23/21.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
class SelectableRoundedImageView : AppCompatImageView {
    private var resourceId = 0

    // Set default scale type to FIT_CENTER, which is default scale type of
    // original ImageView.
    private var scaleType = ScaleType.FIT_CENTER
    var cornerRadius = 0.0f
        private set
    private var rightTopCornerRadius = 0.0f
    private var leftBottomCornerRadius = 0.0f
    private var rightBottomCornerRadius = 0.0f
    var borderWidth = 0.0f
    var borderColors: ColorStateList? = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        private set
    private var isOval = false
    private var drawableResource: Drawable? = null
    private var radiusList = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

    constructor(context: Context?) : super(context!!) {}

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.SelectableRoundedImageView, defStyle, 0
        )
        val index = a.getInt(R.styleable.SelectableRoundedImageView_android_scaleType, -1)
        if (index >= 0) {
            scaleType = sScaleTypeArray[index]
        }
        cornerRadius = a.getDimensionPixelSize(
            R.styleable.SelectableRoundedImageView_sriv_left_top_corner_radius, 0
        ).toFloat()
        rightTopCornerRadius = a.getDimensionPixelSize(
            R.styleable.SelectableRoundedImageView_sriv_right_top_corner_radius, 0
        ).toFloat()
        leftBottomCornerRadius = a.getDimensionPixelSize(
            R.styleable.SelectableRoundedImageView_sriv_left_bottom_corner_radius, 0
        ).toFloat()
        rightBottomCornerRadius = a.getDimensionPixelSize(
            R.styleable.SelectableRoundedImageView_sriv_right_bottom_corner_radius, 0
        ).toFloat()
        require(!(cornerRadius < 0.0f || rightTopCornerRadius < 0.0f || leftBottomCornerRadius < 0.0f || rightBottomCornerRadius < 0.0f)) { "radius values cannot be negative." }
        radiusList = floatArrayOf(
            cornerRadius, cornerRadius,
            rightTopCornerRadius, rightTopCornerRadius,
            rightBottomCornerRadius, rightBottomCornerRadius,
            leftBottomCornerRadius, leftBottomCornerRadius
        )
        borderWidth = a.getDimensionPixelSize(
            R.styleable.SelectableRoundedImageView_sriv_border_width, 0
        ).toFloat()
        require(borderWidth >= 0) { "border width cannot be negative." }
        borderColors = a
            .getColorStateList(R.styleable.SelectableRoundedImageView_sriv_border_color)
        if (borderColors == null) {
            borderColors = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        }
        isOval = a.getBoolean(R.styleable.SelectableRoundedImageView_sriv_oval, false)
        a.recycle()
        updateDrawable()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun getScaleType(): ScaleType {
        return scaleType
    }

    override fun setScaleType(scaleType: ScaleType) {
        super.setScaleType(scaleType)
        this.scaleType = scaleType
        updateDrawable()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        resourceId = 0
        drawableResource = SelectableRoundedCornerDrawable.fromDrawable(drawable, resources)
        super.setImageDrawable(drawableResource)
        updateDrawable()
    }

    override fun setImageBitmap(bm: Bitmap) {
        resourceId = 0
        drawableResource = SelectableRoundedCornerDrawable.fromBitmap(bm, resources)
        super.setImageDrawable(drawableResource)
        updateDrawable()
    }

    override fun setImageResource(resId: Int) {
        if (resourceId != resId) {
            resourceId = resId
            drawableResource = resolveResource()
            super.setImageDrawable(drawableResource)
            updateDrawable()
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val rsrc = resources ?: return null
        var d: Drawable? = null
        if (resourceId != 0) {
            try {
                d = rsrc.getDrawable(resourceId)
            } catch (e: Resources.NotFoundException) {
                Timber.w("Unable to find resource: $resourceId$e")
                // Don't try again.
                resourceId = 0
            }
        }
        return SelectableRoundedCornerDrawable.fromDrawable(d, resources)
    }

    private fun updateDrawable() {
        if (drawableResource == null) {
            return
        }
        (drawableResource as SelectableRoundedCornerDrawable).scaleType = scaleType
        (drawableResource as SelectableRoundedCornerDrawable).setCornerRadii(radiusList)
        (drawableResource as SelectableRoundedCornerDrawable).borderWidth = borderWidth
        (drawableResource as SelectableRoundedCornerDrawable).setBorderColor(borderColors)
        (drawableResource as SelectableRoundedCornerDrawable).setOval(isOval)
    }

    /**
     * Set radii for each corner.
     *
     * @param leftTop     The desired radius for left-top corner in dip.
     * @param rightTop    The desired desired radius for right-top corner in dip.
     * @param leftBottom  The desired radius for left-bottom corner in dip.
     * @param rightBottom The desired radius for right-bottom corner in dip.
     */
    fun setCornerRadius(leftTop: Float, rightTop: Float, leftBottom: Float, rightBottom: Float) {
        val density = resources.displayMetrics.density
        val lt = leftTop * density
        val rt = rightTop * density
        val lb = leftBottom * density
        val rb = rightBottom * density
        radiusList = floatArrayOf(lt, lt, rt, rt, rb, rb, lb, lb)
        updateDrawable()
    }

    /**
     * Set border width.
     *
     * @param width The desired width in dip.
     */
    fun setBorderWidthDP(width: Float) {
        val scaledWidth = resources.displayMetrics.density * width
        if (borderWidth == scaledWidth) {
            return
        }
        borderWidth = scaledWidth
        updateDrawable()
        invalidate()
    }

    var borderColor: Int
        get() = borderColors!!.defaultColor
        set(color) {
            setBorderColor(ColorStateList.valueOf(color))
        }

    fun setBorderColor(colors: ColorStateList?) {
        if (borderColors == colors) {
            return
        }
        borderColors = colors
            ?: ColorStateList
                .valueOf(DEFAULT_BORDER_COLOR)
        updateDrawable()
        if (borderWidth > 0) {
            invalidate()
        }
    }

    fun isOval(): Boolean {
        return isOval
    }

    fun setOval(oval: Boolean) {
        isOval = oval
        updateDrawable()
        invalidate()
    }

    internal class SelectableRoundedCornerDrawable(private val mBitmap: Bitmap?, r: Resources) : Drawable() {
        private val mBounds = RectF()
        private val mBorderBounds = RectF()
        private val mBitmapRect = RectF()
        private var mBitmapWidth = 0
        private var mBitmapHeight = 0
        private val mBitmapPaint: Paint
        private val mBorderPaint: Paint
        private val mBitmapShader: BitmapShader
        private val mRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        private val mBorderRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        private var mOval = false
        private var mBorderWidth = 0f
        var borderColors = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
            private set

        // Set default scale type to FIT_CENTER, which is default scale type of
        // original ImageView.
        private var mScaleType = ScaleType.FIT_CENTER
        private val mPath = Path()
        private var mBoundsConfigured = false
        override fun isStateful(): Boolean {
            return borderColors.isStateful
        }

        override fun onStateChange(state: IntArray): Boolean {
            val newColor = borderColors.getColorForState(state, 0)
            return if (mBorderPaint.color != newColor) {
                mBorderPaint.color = newColor
                true
            } else {
                super.onStateChange(state)
            }
        }

        private fun configureBounds(canvas: Canvas) {
            // I have discovered a truly marvelous explanation of this,
            // which this comment space is too narrow to contain. :)
            // If you want to understand what's going on here,
            // See http://www.joooooooooonhokim.com/?p=289
            val clipBounds = canvas.clipBounds
            val canvasMatrix = canvas.matrix
            if (ScaleType.CENTER == mScaleType) {
                mBounds.set(clipBounds)
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                applyScaleToRadii(canvasMatrix)
                mBounds.set(clipBounds)
            } else if (ScaleType.FIT_XY == mScaleType) {
                val m = Matrix()
                m.setRectToRect(mBitmapRect, RectF(clipBounds), Matrix.ScaleToFit.FILL)
                mBitmapShader.setLocalMatrix(m)
                mBounds.set(clipBounds)
            } else if (ScaleType.FIT_START == mScaleType || ScaleType.FIT_END == mScaleType || ScaleType.FIT_CENTER == mScaleType || ScaleType.CENTER_INSIDE == mScaleType) {
                applyScaleToRadii(canvasMatrix)
                mBounds.set(mBitmapRect)
            } else if (ScaleType.MATRIX == mScaleType) {
                applyScaleToRadii(canvasMatrix)
                mBounds.set(mBitmapRect)
            }
        }

        private fun applyScaleToRadii(m: Matrix) {
            val values = FloatArray(9)
            m.getValues(values)
            for (i in mRadii.indices) {
                mRadii[i] = mRadii[i] / values[0]
            }
        }

        private fun adjustCanvasForBorder(canvas: Canvas) {
            val canvasMatrix = canvas.matrix
            val values = FloatArray(9)
            canvasMatrix.getValues(values)
            val scaleFactorX = values[0]
            val scaleFactorY = values[4]
            val translateX = values[2]
            val translateY = values[5]
            val newScaleX = (mBounds.width()
                    / (mBounds.width() + mBorderWidth + mBorderWidth))
            val newScaleY = (mBounds.height()
                    / (mBounds.height() + mBorderWidth + mBorderWidth))
            canvas.scale(newScaleX, newScaleY)
            if (ScaleType.FIT_START == mScaleType || ScaleType.FIT_END == mScaleType || ScaleType.FIT_XY == mScaleType || ScaleType.FIT_CENTER == mScaleType || ScaleType.CENTER_INSIDE == mScaleType || ScaleType.MATRIX == mScaleType) {
                canvas.translate(mBorderWidth, mBorderWidth)
            } else if (ScaleType.CENTER == mScaleType || ScaleType.CENTER_CROP == mScaleType) {
                // First, make translate values to 0
                canvas.translate(
                    -translateX / (newScaleX * scaleFactorX),
                    -translateY / (newScaleY * scaleFactorY)
                )
                // Then, set the final translate values.
                canvas.translate(-(mBounds.left - mBorderWidth), -(mBounds.top - mBorderWidth))
            }
        }

        private fun adjustBorderWidthAndBorderBounds(canvas: Canvas) {
            val canvasMatrix = canvas.matrix
            val values = FloatArray(9)
            canvasMatrix.getValues(values)
            val scaleFactor = values[0]
            val viewWidth = mBounds.width() * scaleFactor
            mBorderWidth = mBorderWidth * mBounds.width() / (viewWidth - 2 * mBorderWidth)
            mBorderPaint.strokeWidth = mBorderWidth
            mBorderBounds.set(mBounds)
            mBorderBounds.inset(-mBorderWidth / 2, -mBorderWidth / 2)
        }

        private fun setBorderRadii() {
            for (i in mRadii.indices) {
                if (mRadii[i] > 0) {
                    mBorderRadii[i] = mRadii[i]
                    mRadii[i] = mRadii[i] - mBorderWidth
                }
            }
        }

        override fun draw(canvas: Canvas) {
            canvas.save()
            if (!mBoundsConfigured) {
                configureBounds(canvas)
                if (mBorderWidth > 0) {
                    adjustBorderWidthAndBorderBounds(canvas)
                    setBorderRadii()
                }
                mBoundsConfigured = true
            }
            if (mOval) {
                if (mBorderWidth > 0) {
                    adjustCanvasForBorder(canvas)
                    mPath.addOval(mBounds, Path.Direction.CW)
                    canvas.drawPath(mPath, mBitmapPaint)
                    mPath.reset()
                    mPath.addOval(mBorderBounds, Path.Direction.CW)
                    canvas.drawPath(mPath, mBorderPaint)
                } else {
                    mPath.addOval(mBounds, Path.Direction.CW)
                    canvas.drawPath(mPath, mBitmapPaint)
                }
            } else {
                if (mBorderWidth > 0) {
                    adjustCanvasForBorder(canvas)
                    mPath.addRoundRect(mBounds, mRadii, Path.Direction.CW)
                    canvas.drawPath(mPath, mBitmapPaint)
                    mPath.reset()
                    mPath.addRoundRect(mBorderBounds, mBorderRadii, Path.Direction.CW)
                    canvas.drawPath(mPath, mBorderPaint)
                } else {
                    mPath.addRoundRect(mBounds, mRadii, Path.Direction.CW)
                    canvas.drawPath(mPath, mBitmapPaint)
                }
            }
            canvas.restore()
        }

        fun setCornerRadii(radii: FloatArray?) {
            if (radii == null) return
            if (radii.size != 8) {
                throw ArrayIndexOutOfBoundsException("radii[] needs 8 values")
            }
            for (i in radii.indices) {
                mRadii[i] = radii[i]
            }
        }

        override fun getOpacity(): Int {
            return if (mBitmap == null || mBitmap.hasAlpha() || mBitmapPaint.alpha < 255) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
        }

        override fun setAlpha(alpha: Int) {
            mBitmapPaint.alpha = alpha
            invalidateSelf()
        }

        override fun setColorFilter(cf: ColorFilter?) {
            mBitmapPaint.colorFilter = cf
            invalidateSelf()
        }

        override fun setDither(dither: Boolean) {
            mBitmapPaint.isDither = dither
            invalidateSelf()
        }

        override fun setFilterBitmap(filter: Boolean) {
            mBitmapPaint.isFilterBitmap = filter
            invalidateSelf()
        }

        override fun getIntrinsicWidth(): Int {
            return mBitmapWidth
        }

        override fun getIntrinsicHeight(): Int {
            return mBitmapHeight
        }

        var borderWidth: Float
            get() = mBorderWidth
            set(width) {
                mBorderWidth = width
                mBorderPaint.strokeWidth = width
            }
        var borderColor: Int
            get() = borderColors.defaultColor
            set(color) {
                setBorderColor(ColorStateList.valueOf(color))
            }

        /**
         * Controls border color of this ImageView.
         *
         * @param colors The desired border color. If it's null, no border will be
         * drawn.
         */
        fun setBorderColor(colors: ColorStateList?) {
            if (colors == null) {
                mBorderWidth = 0f
                borderColors = ColorStateList.valueOf(Color.TRANSPARENT)
                mBorderPaint.color = Color.TRANSPARENT
            } else {
                borderColors = colors
                mBorderPaint.color = borderColors.getColorForState(
                    state,
                    DEFAULT_BORDER_COLOR
                )
            }
        }

        fun isOval(): Boolean {
            return mOval
        }

        fun setOval(oval: Boolean) {
            mOval = oval
        }

        var scaleType: ScaleType?
            get() = mScaleType
            set(scaleType) {
                if (scaleType == null) {
                    return
                }
                mScaleType = scaleType
            }

        companion object {
            private const val TAG = "SelectableRoundedCornerDrawable"
            private const val DEFAULT_BORDER_COLOR = Color.BLACK
            fun fromBitmap(bitmap: Bitmap?, r: Resources): SelectableRoundedCornerDrawable? {
                return bitmap?.let { SelectableRoundedCornerDrawable(it, r) }
            }

            fun fromDrawable(drawable: Drawable?, r: Resources): Drawable? {
                if (drawable != null) {
                    if (drawable is SelectableRoundedCornerDrawable) {
                        return drawable
                    } else if (drawable is LayerDrawable) {
                        val ld = drawable
                        val num = ld.numberOfLayers
                        for (i in 0 until num) {
                            val d = ld.getDrawable(i)
                            ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d, r))
                        }
                        return ld
                    }
                    val bm = drawableToBitmap(drawable)
                    if (bm != null) {
                        return SelectableRoundedCornerDrawable(bm, r)
                    } else {
                        Timber.w("Failed to create bitmap from drawable!")
                    }
                }
                return drawable
            }

            fun drawableToBitmap(drawable: Drawable?): Bitmap? {
                if (drawable == null) {
                    return null
                }
                if (drawable is BitmapDrawable) {
                    return drawable.bitmap
                }
                var bitmap: Bitmap?
                val width = Math.max(drawable.intrinsicWidth, 2)
                val height = Math.max(drawable.intrinsicHeight, 2)
                try {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    bitmap = null
                }
                return bitmap
            }
        }

        init {
            mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            if (mBitmap != null) {
                mBitmapWidth = mBitmap.getScaledWidth(r.displayMetrics)
                mBitmapHeight = mBitmap.getScaledHeight(r.displayMetrics)
            } else {
                mBitmapHeight = -1
                mBitmapWidth = mBitmapHeight
            }
            mBitmapRect[0f, 0f, mBitmapWidth.toFloat()] = mBitmapHeight.toFloat()
            mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mBitmapPaint.style = Paint.Style.FILL
            mBitmapPaint.shader = mBitmapShader
            mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mBorderPaint.style = Paint.Style.STROKE
            mBorderPaint.color = borderColors.getColorForState(
                state,
                DEFAULT_BORDER_COLOR
            )
            mBorderPaint.strokeWidth = mBorderWidth
        }
    }

    companion object {
        const val TAG = "SelectableRoundedImageView"
        private val sScaleTypeArray = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
    }
}