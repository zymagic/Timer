package com.zy.timer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import kotlin.math.hypot

class FloatingWindow(context: Context) : FrameLayout(context) {

    private val touchSlop: Int
    private var lastX: Float = -1f
    private var lastY: Float = -1f
    private var isDragging = false
    private lateinit var params: WindowManager.LayoutParams
    private val wm: WindowManager

    init {
        val config = ViewConfiguration.get(context)
        touchSlop = config.scaledTouchSlop
        wm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.rawX
                lastY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging && hypot(ev.rawX - lastX, ev.rawY - lastY) > touchSlop) {
                    isDragging = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return isDragging
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - lastX
                val deltaY = event.rawY - lastY
                if (isDragging) {
                    moveBy(deltaX.toInt(), deltaY.toInt())
                    lastX = event.rawX
                    lastY = event.rawY
                } else if (hypot(deltaX, deltaY) > touchSlop) {
                    isDragging = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return isDragging || super.onTouchEvent(event)
    }

    private fun moveBy(dx: Int, dy: Int) {
        params.x += dx
        params.y += dy
        safe {
            wm.updateViewLayout(this, params)
        }
    }

    fun show() {
        if (!this::params.isInitialized) {
            measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            params = WindowManager.LayoutParams(
                when (measuredWidth) {
                    0 -> WindowManager.LayoutParams.WRAP_CONTENT
                    else -> measuredWidth
                },
                when (measuredHeight) {
                    0 -> WindowManager.LayoutParams.WRAP_CONTENT
                    else -> measuredHeight
                },
                if (android.os.Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.START or Gravity.TOP
            params.y = (context.resources.displayMetrics.density * 56f).toInt()
        }
        safe {
            wm.addView(this, params)
        }
    }

    fun hide() {
        safe {
            wm.removeView(this)
        }
    }
}