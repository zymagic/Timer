package com.zy.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.view.LayoutInflater
import android.widget.TextView
import com.zy.timer.NTPManager.Companion.instance
import java.text.SimpleDateFormat
import java.util.*

class TimerService : Service() {

    private var holder: TimerViewHolder? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getIntExtra("code", -1) ?: -1) {
            1 -> {
                showWindow()
                return START_STICKY_COMPATIBILITY
            }
            0 -> {
                removeWindow()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showWindow() {
        if (holder == null) {
            holder = TimerViewHolder(this)
        }
        holder?.window?.show()
        holder?.run()
    }

    private fun removeWindow() {
        holder?.window?.hide()
        holder?.stop()
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra("code", 1)
            safe {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra("code", 0)
            safe {
                context.startService(intent)
            }
        }
    }

    class TimerViewHolder(context: Context) {
        val window: FloatingWindow = FloatingWindow(context)
        private val flag: TextView
        private val time: TextView
        private val dateFormat: SimpleDateFormat

        init {
            LayoutInflater.from(context).inflate(R.layout.timer_layout, window, true)
            flag = window.findViewById(R.id.flag)
            time = window.findViewById(R.id.time)
            dateFormat = SimpleDateFormat("kk:mm:ss:SS", Locale.US)
        }

        fun run() {
            flag.text = if (instance.manualOffset == 0L) instance.tag else {
                if (instance.manualOffset > 0) {
                    "${instance.tag}(+${instance.manualOffset})"
                } else {
                    "${instance.tag}(${instance.manualOffset})"
                }
            }
            if (time.animation != null) {
                time.startAnimation(time.animation)
            } else {
                val animation = TimerAnimation {
                    time.text = dateFormat.format(it)
                }
                animation.duration = 1000
                animation.repeatCount = -1
                time.startAnimation(animation)
            }
        }

        fun stop() {
            time.clearAnimation()
        }
    }
}