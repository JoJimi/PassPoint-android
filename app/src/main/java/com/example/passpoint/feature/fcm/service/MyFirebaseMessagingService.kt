package com.example.passpoint.feature.fcm.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.passpoint.MainActivity
import com.example.passpoint.R
import com.example.passpoint.feature.fcm.FcmTokenRegistrar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM"
        const val CHANNEL_ID = "passpoint_default"
        private const val CHANNEL_NAME = "기본 알림"
    }

    /** 토큰이 새로 발급/갱신될 때 호출된다. 로그로 찍고 서버에 등록한다. */
    override fun onNewToken(token: String) {
        Log.d(TAG, "새 토큰 발급(onNewToken): $token")
        FcmTokenRegistrar.register(applicationContext, token)
    }

    /** 앱이 포그라운드일 때, 또는 data 메시지일 때 호출된다. */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "메시지 수신 - data=${message.data}, notification=${message.notification}")

        // notification 페이로드 우선, 없으면 data 페이로드에서 꺼낸다.
        val title = message.notification?.title ?: message.data["title"] ?: "PassPoint"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        createChannel()

        // 알림 탭 시 앱 열기
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        // Android 13+ 에서 알림 권한이 없으면 표시할 수 없다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "알림 권한이 없어 표시하지 못함 (POST_NOTIFICATIONS)")
            return
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
