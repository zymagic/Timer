package com.zy.timer

import android.view.animation.Animation
import android.view.animation.Transformation

class TimerAnimation(private val cbk: (Long) -> Unit) : Animation() {

    private var offset: Long = 0

    init {
        NTPManager.instance.updateTime {
            offset = it - System.currentTimeMillis()
        }
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val now = System.currentTimeMillis()
        cbk.invoke(now + offset + NTPManager.instance.manualOffset)
        t?.transformationType = Transformation.TYPE_IDENTITY
    }
}