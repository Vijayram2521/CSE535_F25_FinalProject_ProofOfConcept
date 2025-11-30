package com.app.networkwrapperdemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*
import kotlin.concurrent.thread
import android.content.Context
import android.content.IntentFilter
import android.os.BatteryManager

class NetworkSchedulerServiceDemo : Service() {
    companion object {
        val realTimeQueue = mutableListOf<NetworkRequestDemo>()
        val nearTimeQueue = mutableListOf<NetworkRequestDemo>()
        val wheneverQueue = mutableListOf<NetworkRequestDemo>()
    }
     var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startScheduler()
        }
        return START_STICKY
    }

     fun startScheduler() {
        thread {
            while (isRunning) {
                // Sleep for a short time to avoid busy waiting
                Thread.sleep(1000)

                // Check and process queues
                processQueues()
            }
        }
    }

     fun processQueues() {
        // Real time: process immediately
        synchronized(realTimeQueue) {
            while (realTimeQueue.isNotEmpty()) {
                val request = realTimeQueue.removeAt(0)
                Log.d("NetworkScheduler", "Sending REAL_TIME: ${request.data}")
            }
        }

        // Near time: process if conditions are met
        synchronized(nearTimeQueue) {
            val now = System.currentTimeMillis()
            val iterator = nearTimeQueue.iterator()
            while (iterator.hasNext()) {
                val request = iterator.next()
                val batteryLevel = getBatteryLevel(this)
                val isWifi = isWifiConnected()
                val isStrongSignal = isStrongMobileSignal()

                if (batteryLevel >= 50 && (isWifi || isStrongSignal)) {
                    if (now - request.timestamp <= request.maxWaitWindow) {
                        Log.d("NetworkScheduler", "Sending NEAR_TIME: ${request.data}")
                        iterator.remove()
                    }
                } else if (batteryLevel >= 80 && isWifi) {
                    Log.d("NetworkScheduler", "Sending NEAR_TIME: ${request.data}")
                    iterator.remove()
                }
            }
        }

        // Whenever: process if conditions are met
        synchronized(wheneverQueue) {
            val now = System.currentTimeMillis()
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val batteryLevel = getBatteryLevel(this)
            val isCharging = isDeviceCharging()
            val isWifi = isWifiConnected()
            val isMobileData = isMobileDataConnected()

            val iterator = wheneverQueue.iterator()
            while (iterator.hasNext()) {
                val request = iterator.next()
                if ((isCharging || batteryLevel >= 80) && isWifi) {
                    Log.d("NetworkScheduler", "Sending WHENEVER: ${request.data}")
                    iterator.remove()
                } else if ((batteryLevel >= 60 || isCharging) && isMobileData && hour >= 22) {
                    Log.d("NetworkScheduler", "Sending WHENEVER: ${request.data}")
                    iterator.remove()
                }
            }
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale).toInt()
    }

    private fun isWifiConnected(): Boolean {
        // Implement Wi-Fi check
        return true
    }

    private fun isStrongMobileSignal(): Boolean {
        // Implement mobile signal strength check
        return true
    }

    private fun isDeviceCharging(): Boolean {
        // Implement charging check
        return false
    }

    private fun isMobileDataConnected(): Boolean {
        // Implement mobile data check
        return true
    }
}
