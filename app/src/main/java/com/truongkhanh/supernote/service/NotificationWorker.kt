package com.truongkhanh.supernote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.utils.NOTIFICATION_DESCRIPTION_DATA_TAG
import com.truongkhanh.supernote.utils.NOTIFICATION_ID_DATA_TAG
import com.truongkhanh.supernote.utils.NOTIFICATION_TITLE_DATA_TAG

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        inputData.let { data ->
            val title = data.getString(NOTIFICATION_TITLE_DATA_TAG)
            val description = data.getString(NOTIFICATION_DESCRIPTION_DATA_TAG)
            val id = data.getInt(NOTIFICATION_ID_DATA_TAG, DEFAULT_VALUE)
            if (id != DEFAULT_VALUE) {
                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                createNotificationChannel(notificationManager)
                val notification = getNotification(title, description)
                notificationManager.notify(id, notification)
                return Result.success()
            }
        }
        return Result.failure()
    }

    private fun getNotification(title: String?, description: String?): Notification {
        val context = applicationContext
        return NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_uit)
            .setContentTitle(title)
            .setContentText(description)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .build()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        getNotificationChannel()?.let { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationChannel(): NotificationChannel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                ALARM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description =
                applicationContext.getString(R.string.lbl_notification_description)
            channel
        } else {
            null
        }
    }
}

private const val ALARM_CHANNEL_ID = "com.truongkhanh.supernote.AlarmChannelID"
private const val ALARM_CHANNEL_NAME = "Alarm Notification"
private const val DEFAULT_VALUE = 0