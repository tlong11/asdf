package com.yourpackage.taphelper // 替换成你的包名

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.View
import android.widget.Button
import android.widget.TextView

class TapService : AccessibilityService() {

    private lateinit var floatingView: View
    private var isServiceActive = false
    private lateinit var windowManager: WindowManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton() // 创建一个悬浮窗按钮，方便开关点击
    }

    // 创建一个悬浮控制按钮
    private fun createFloatingButton() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        val toggleBtn = floatingView.findViewById<Button>(R.id.btn_toggle)
        val statusText = floatingView.findViewById<TextView>(R.id.tv_status)

        toggleBtn.setOnClickListener {
            isServiceActive = !isServiceActive
            if (isServiceActive) {
                statusText.text = "点击中"
                startAutoTap()
            } else {
                statusText.text = "已停止"
            }
        }

        windowManager.addView(floatingView, params)
    }

    // 模拟点击屏幕中心 (你可以修改坐标)
    private fun startAutoTap() {
        // 这里为了演示，每隔1秒点击一次屏幕中心 (540, 960)
        // 正式项目中你应该使用 Handler 或 Timer，这里简化逻辑
        Thread {
            while (isServiceActive) {
                // 模拟点击坐标 (x, y)
                performTap(540f, 960f)
                Thread.sleep(1000) // 间隔1秒
            }
        }.start()
    }

    private fun performTap(x: Float, y: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            path.moveTo(x, y)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
            dispatchGesture(gestureBuilder.build(), null, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onInterrupt() {}
}
