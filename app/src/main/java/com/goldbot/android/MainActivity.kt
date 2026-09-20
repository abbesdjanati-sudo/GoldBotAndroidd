
package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 30, 30, 30)
        root.gravity = Gravity.TOP

        val title = TextView(this)
        title.text = "GoldBot"
        title.textSize = 30f
        title.setTextColor(Color.BLACK)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 20, 0, 40)

        val status = TextView(this)
        status.text = "حالة الروبوت: متوقف"
        status.textSize = 20f
        status.setPadding(0, 20, 0, 20)

        val market = TextView(this)
        market.text = "الذهب XAU/USD"
        market.textSize = 20f
        market.setPadding(0, 20, 0, 20)

        val signal = TextView(this)
        signal.text = "الإشارة: في انتظار تحليل السوق"
        signal.textSize = 20f
        signal.setPadding(0, 20, 0, 20)

        val balance = TextView(this)
        balance.text = "الرصيد: --"
        balance.textSize = 20f
        balance.setPadding(0, 20, 0, 20)

        root.addView(title)
        root.addView(status)
        root.addView(market)
        root.addView(signal)
        root.addView(balance)

        setContentView(root)
    }
}
