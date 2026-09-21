package com.goldbot.android

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {

    private lateinit var priceView: TextView
    private lateinit var statusView: TextView
    private lateinit var signalView: TextView
    private lateinit var analysisView: TextView
    private lateinit var tradeView: TextView
    private lateinit var accountView: TextView
    private lateinit var logView: TextView
    private lateinit var startButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val prices = ArrayList<Double>()

    private var running = false
    private var paperTrading = false

    private var currentPrice = 0.0
    private var signal = "WAIT"

    private var balance = 10000.0
    private var risk = 1.0

    private var position = ""
    private var entry = 0.0
    private var stopLoss = 0.0
    private var takeProfit = 0.0
    private var positionSize = 0.0

    private var telegramToken = ""
    private var telegramChat = ""

    private var mt5Server = ""
    private var mt5Login = ""
    private var mt5Password = ""

    private val bg = Color.rgb(15, 18, 25)
    private val card = Color.rgb(27, 31, 42)
    private val gold = Color.rgb(255, 215, 0)
    private val green = Color.rgb(0, 220, 120)
    private val red = Color.rgb(255, 80, 80)
    private val orange = Color.rgb(255, 170, 0)

    private val timer = object : Runnable {
        override fun run() {
            if (running) {
                getPrice()
            }

            handler.postDelayed(this, 15000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        loadSettings()
        buildInterface()

        handler.postDelayed(timer, 1000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timer)
        super.onDestroy()
    }

    private fun buildInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 40)
            setBackgroundColor(bg)
        }

        scroll.addView(root)

        val title = TextView(this).apply {
            text = "GOLD BOT"
            textSize = 30f
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "XAU/USD Trading Assistant"
            textSize = 14f
            setTextColor(Color
