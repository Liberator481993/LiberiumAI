package com.liberium.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object DynamicIslandStatus {
    private const val CHANNEL_ID = "liberium_ai_activity"
    private const val NOTIFICATION_ID = 7401

    fun show(context: Context, title: String, message: String, active: Boolean) {
        createChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            7401,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openApp)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(active)
            .setOnlyAlertOnce(true)
            .setAutoCancel(!active)
            .setShowWhen(true)
            .setProgress(if (active) 0 else 0, 0, active)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun clear(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Активность Liberium AI",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Показывает, когда Liberium AI анализирует запрос или вложения"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
