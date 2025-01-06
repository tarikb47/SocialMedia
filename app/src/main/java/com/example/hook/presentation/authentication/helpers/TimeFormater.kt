package com.example.hook.presentation.authentication.helpers

import java.util.concurrent.TimeUnit

class TimeFormater {
     fun getRelativeTime(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp

        return when {
            elapsedTime < TimeUnit.MINUTES.toMillis(1) -> "just now"
            elapsedTime < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            elapsedTime < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            elapsedTime < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(elapsedTime)
                "$days day${if (days > 1) "s" else ""} ago"
            }
            else -> "a while ago"
        }
    }
}
