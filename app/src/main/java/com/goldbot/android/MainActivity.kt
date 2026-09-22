package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

class MainActivity : Activity() {

    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var historyText: TextView
    private lateinit var analysisText: TextView
    private lateinit var signalText: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val prices = ArrayList<Double>()

    private val timer = object : Runnable {
        override fun run() {
            getGoldPrice()
            handler.postDelayed(this, 15000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildInterface()
        getGoldPrice()

        handler.postDelayed(timer, 15000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timer)
        super.onDestroy()
    }

    private fun buildInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setPadding(25, 25, 25, 40)
        root.setBackgroundColor(Color.rgb(15, 18, 25))

        scroll.addView(root)

        val title = TextView(this)

        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(Color.YELLOW)
        title.gravity = Gravity.CENTER
        title.setPadding(10, 10, 10, 5)

        root.addView(title)

        val subtitle = TextView(this)

        subtitle.text = "XAU/USD"
        subtitle.textSize = 18f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(10, 5, 10, 20)

        root.addView(subtitle)

        val priceTitle = TextView(this)

        priceTitle.text = "GOLD PRICE"
        priceTitle.textSize = 18f
        priceTitle.setTextColor(Color.LTGRAY)
       
