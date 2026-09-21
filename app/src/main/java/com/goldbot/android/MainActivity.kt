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
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.rgb(15, 18, 25))
        root.setPadding(30, 30, 30, 30)

        val title = TextView(this)

        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(Color.YELLOW)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val status = TextView(this)

        status.text = "Bot is ready"
        status.textSize = 20f
        status.setTextColor(Color.WHITE)
        status.gravity = Gravity.CENTER
        status.setPadding(20, 30, 20, 30)

        root.addView(status)

        setContentView(root)
    }
}
