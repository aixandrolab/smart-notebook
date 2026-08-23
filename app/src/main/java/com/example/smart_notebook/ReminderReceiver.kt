// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.toColorInt

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("note_title") ?: "Note"
        val content = intent.getStringExtra("note_content") ?: "Time to check your note"
        val noteId = intent.getStringExtra("note_id") ?: ""

        createNotificationChannel(context)

        val openIntent = Intent(context, NoteDetailActivity::class.java).apply {
            putExtra("note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, DismissReceiver::class.java).apply {
            putExtra("notification_id", System.currentTimeMillis().toInt())
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "smart_notebook_channel")
            .setContentTitle("📝 Reminder: $title")
            .setContentText("You set a reminder for this note. Please take a moment to read it! 📖")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You set a reminder for this note: \"$title\"\n\n\"$content\"\n\nPlease take a moment to review it! 📖"))
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setColor("#FF9800".toColorInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 300, 500, 300, 1000))
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_view,
                    "Open Note",
                    pendingIntent
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Dismiss",
                    dismissPendingIntent
                ).build()
            )
            .setTimeoutAfter(3600000)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                notify(notificationId, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "smart_notebook_channel",
                "Smart Notebook Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders for your important notes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 1000)
                setShowBadge(true)
                lightColor = "#FF9800".toColorInt()
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}