package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val bg = Color.rgb(15, 18, 25)
    private val gold = Color.rgb(255, 193, 7)
    private val white = Color.WHITE
    private val gray = Color.LTGRAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        showHome()
    }

    private fun showHome() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 40, 30, 30)
        root.setBackgroundColor(bg)

        val title = TextView(this)
        title.text = "Gold Bot"
        title.textSize = 30f
        title.setTextColor(gold)
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val status = TextView(this)
        status.text = "\n● البوت متوقف"
        status.textSize = 20f
        status.setTextColor(white)
        status.gravity = Gravity.CENTER

        root.addView(
            status,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val price = TextView(this)
        price.text = "\nXAU/USD\nبانتظار السعر..."
        price.textSize = 24f
        price.setTextColor(gold)
        price.gravity = Gravity.CENTER

        root.addView(
            price,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val info = TextView(this)
        info.text =
           
