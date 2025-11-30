package com.app.networkwrapperdemo

data class NetworkRequestDemo(
    val data: String,
    val priority: Priority,
    val timestamp: Long = System.currentTimeMillis(),
    var maxWaitWindow: Long = 0L // max time a network request can wait
)
