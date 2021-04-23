package com.inkkal.neararandroid.models

import android.animation.ValueAnimator


internal data class ARNearAnimationModel(
    val valueAnimator: ValueAnimator? = null,
    var animatedSize: Int = 0
)
