package com.zy.timer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NTPManager.instance.init(this)

        setContentView(R.layout.activity_main)

        start.setOnClickListener {
            if (checkWindowPermission(true)) {
                TimerService.start(this)
            }
        }

        stop.setOnClickListener {
            TimerService.stop(this)
        }

        offset.setText(NTPManager.instance.manualOffset.toString())
        add.setOnClickListener {
            var cur = Integer.parseInt(offset.text.toString())
            cur += 100
            offset.setText(cur.toString())
        }
        minus.setOnClickListener {
            var cur = Integer.parseInt(offset.text.toString())
            cur -= 100
            offset.setText(cur.toString())
        }

        tag.setText(NTPManager.instance.tag)

        save.setOnClickListener {
            NTPManager.instance.update(Integer.parseInt(offset.text.toString()).toLong(), tag.text.toString())
        }
    }

    private fun checkWindowPermission(jump: Boolean) : Boolean {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            val op = Settings.canDrawOverlays(applicationContext)
            if (!op && jump) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivityForResult(intent, 1)
            }
            return op
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && checkWindowPermission(false)) {
            TimerService.start(this)
        }
    }
}
