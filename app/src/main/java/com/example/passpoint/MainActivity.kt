package com.example.passpoint

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.passpoint.core.network.RetrofitClient
import com.example.passpoint.feature.fcm.FcmTokenRegistrar
import com.example.passpoint.feature.fcm.service.MyFirebaseMessagingService
import com.example.passpoint.ui.navigation.AppNavHost
import com.example.passpoint.ui.theme.PassPointTheme
import com.kakao.sdk.common.KakaoSdk

class MainActivity : ComponentActivity() {

    // 알림 권한 요청 런처. 결과와 무관하게 토큰 조회/등록은 진행한다(권한은 표시에만 영향).
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        createNotificationChannel()
        requestNotificationPermissionIfNeeded()

        // 현재 FCM 토큰을 Logcat에 찍고(태그 "FCM"), 로그인 상태면 서버에 등록한다.
        FcmTokenRegistrar.fetchAndRegister(this)

        setContent {
            PassPointTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost()
                }
            }
        }
    }

    // 백그라운드 수신 notification 메시지가 쓸 기본 채널을 미리 만들어둔다(존재 시 무시됨).
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MyFirebaseMessagingService.CHANNEL_ID,
                "기본 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
