package com.example.passpoint.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

/**
 * 하단 탭 4개 화면 헤더 우측에 쓰는 알림 아이콘.
 * 백엔드에 알림 기능이 없어서 탭하면 안내만 띄운다.
 */
@Composable
fun NotificationBell(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Text(
        text = "🔔",
        fontSize = 18.sp,
        modifier = modifier.clickable {
            Toast.makeText(context, "알림 기능은 준비 중이에요.", Toast.LENGTH_SHORT).show()
        }
    )
}
