package com.zy.timer

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.Executors

class NTPManager private constructor() {

    companion object {
        val instance = NTPManager()

        private val hosts = arrayOf(
            "dns1.synet.edu.cn",
            "news.neu.edu.cn",
            "dns.sjtu.edu.cn",
            "dns2.synet.edu.cn",
            "ntp.glnet.edu.cn",
            "ntp-sz.chl.la",
            "ntp.gwadar.cn",
            "cn.pool.ntp.org"
        )

        private const val TIMEOUT = 30000 // 30s

        private val EXECUTOR = Executors.newSingleThreadExecutor()
    }

    private lateinit var sp: SharedPreferences

    var manualOffset: Long = 0

    var tag: String = "UTC"

    fun init(context: Context) {
        sp = context.getSharedPreferences("timer", Context.MODE_PRIVATE)
        manualOffset = sp.getLong("offset", 0)
        tag = sp.getString("tag", "UTC") ?: "UTC"
    }

    fun update(offset: Long, tag: String) {
        this.manualOffset = offset
        this.tag = tag
        sp.edit().putLong("offset", manualOffset).putString("tag", tag).apply()
    }

    fun updateTime(listener: (Long) -> Unit) {
        EXECUTOR.execute {
            for (host in hosts) {
                val time = getNtpTimeFromServer(host)
                android.util.Log.e("NTP", "update from $host :: $time")
                if (time != -1L) {
                    listener.invoke(time)
                    break
                }
            }
        }
    }

    private fun getNtpTimeFromServer(host: String) : Long {
        val client = SNTPClient()
        val success = client.requestTime(host, TIMEOUT)
        if (success) {
            return client.ntpTime
        }
        return -1L
    }
}