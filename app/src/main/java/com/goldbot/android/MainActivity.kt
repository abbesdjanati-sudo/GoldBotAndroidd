package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Gold Bot"
        textView.textSize = 28f

        setContentView(textView)
    }
}
