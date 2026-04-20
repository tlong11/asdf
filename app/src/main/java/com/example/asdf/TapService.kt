package com.example.asdf

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
        createFloatingButton()
    }

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

        // 这里暂时用简单视图，避免依赖布局文件
        floatingView = android.widget.FrameLayout(this).apply {
            setBackgroundColor(0xCC000000.toInt())
            setPadding(20, 10, 20, 10)
            
            val textView = TextView(context).apply {
                text = "停止"
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
            }
            addView(textView)
            
            setOnClickListener {
                isServiceActive = !isServiceActive
                textView.text = if (isServiceActive) "点击中" else "已停止"
                if (isServiceActive) startAutoTap()
            }
        }
        
        windowManager.addView(floatingView, params)
    }

    private fun startAutoTap() {
        Thread {
            while (isServiceActive) {
                performTap(540f, 960f)
                Thread.sleep(1000)
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
