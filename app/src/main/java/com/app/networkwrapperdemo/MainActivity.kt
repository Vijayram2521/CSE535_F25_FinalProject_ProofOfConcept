package com.app.networkwrapperdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.app.networkwrapperdemo.NetworkRequestDemo
import com.app.networkwrapperdemo.Priority
import com.app.networkwrapperdemo.NetworkSchedulerServiceDemo


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the service
        startService(Intent(this, NetworkSchedulerServiceDemo::class.java))

        // Create layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Real Time Button
        val realTimeButton = Button(this).apply {
            text = "Send REAL_TIME"
            setOnClickListener {
                val request = NetworkRequestDemo("Real time message", Priority.REAL_TIME)
                addRequest(request)
            }
        }

        // Near Time Button
        val nearTimeButton = Button(this).apply {
            text = "Send NEAR_TIME"
            setOnClickListener {
                val request = NetworkRequestDemo("Near time message", Priority.NEAR_TIME, maxWaitWindow = 600000) // 10 minutes
                addRequest(request)
            }
        }

        // Whenever Button
        val wheneverButton = Button(this).apply {
            text = "Send WHENEVER"
            setOnClickListener {
                val request = NetworkRequestDemo("Whenever message", Priority.WHENEVER)
                addRequest(request)
            }
        }

        // Add buttons to layout
        layout.addView(realTimeButton)
        layout.addView(nearTimeButton)
        layout.addView(wheneverButton)

        // Set layout as content view
        setContentView(layout)
    }

    private fun addRequest(request: NetworkRequestDemo) {
        // Add request to the appropriate queue
        when (request.priority) {
            Priority.REAL_TIME -> {
                synchronized(NetworkSchedulerServiceDemo.realTimeQueue) {
                    NetworkSchedulerServiceDemo.realTimeQueue.add(request)
                }
            }
            Priority.NEAR_TIME -> {
                synchronized(NetworkSchedulerServiceDemo.nearTimeQueue) {
                    NetworkSchedulerServiceDemo.nearTimeQueue.add(request)
                }
            }
            Priority.WHENEVER -> {
                synchronized(NetworkSchedulerServiceDemo.wheneverQueue) {
                    NetworkSchedulerServiceDemo.wheneverQueue.add(request)
                }
            }
        }
        Log.d("MainActivity", "Added request: ${request.data}")
    }

}
