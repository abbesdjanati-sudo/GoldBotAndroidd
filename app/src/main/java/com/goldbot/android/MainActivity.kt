package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {

    // =========================
    // UI
    // =========================

    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var indicatorsText: TextView
    private lateinit var tradeText: TextView
    private lateinit var balanceText: TextView
    private lateinit var logText: TextView

    private lateinit var startButton: Button
    private lateinit var paperButton: Button
    private lateinit var telegramButton: Button
    private lateinit var mt5Button: Button

    // =========================
    // Bot
    // =========================

    private var botRunning = false
    private var paperTrading = false

    private var currentPrice = 0.0

    private val prices = ArrayList<Double>()

    private var lastSignal = "WAIT"

    private var stopLoss = 0.0
    private var takeProfit = 0.0

    // =========================
    // Paper account
    // =========================

    private var balance = 10000.0

    private var position = ""
    private var entryPrice = 0.0
    private var positionSize = 0.0

    private var riskPercent = 1.0

    // =========================
    // Telegram
    // =========================

    private var telegramToken = ""
    private var telegramChatId = ""

    // =========================
    // MT5 settings
    // =========================

    private var mt5Server = ""
    private var mt5Login = ""
    private var mt5Password = ""

    // =========================
    // Handler
    // =========================

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {

            if (botRunning) {
                fetchGoldPrice()
            }

            handler.postDelayed(this, 15000)
        }
    }

    // =========================
    // Colors
    // =========================

    private val bg = Color.rgb(15, 18, 25)
    private val card = Color.rgb(25, 29, 39)
    private val gold = Color.rgb(255, 215, 0)
    private val white = Color.WHITE
    private val gray = Color.LTGRAY
    private val green = Color.rgb(0, 220, 120)
    private val red = Color.rgb(255, 80, 80)
    private val orange = Color.rgb(255, 170, 0)

    // =========================
    // Activity
    // =========================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildInterface()
        loadSettings()

        handler.postDelayed(updateRunnable, 1000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    // =========================
    // Interface
    // =========================

    private fun buildInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 40)
        root.setBackgroundColor(bg)

        scroll.addView(root)

        val title = TextView(this)
        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(gold)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 10, 0, 5)

        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "AI XAU/USD Trading Assistant"
        subtitle.textSize = 14f
        subtitle.setTextColor(gray)
        subtitle.gravity = Gravity.CENTER

        root.addView(subtitle)

        root.addView(space(15))

        // Status
        statusText = TextView(this)
        statusText.text = "البوت متوقف"
        statusText.textSize = 19f
        statusText.setTextColor(red)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(15, 15, 15, 15)
        statusText.setBackgroundColor(card)

        root.addView(statusText)

        root.addView(space(12))

        // Price
        priceText = TextView(this)
        priceText.text = "XAU/USD\n--"
        priceText.textSize = 28f
        priceText.setTextColor(gold)
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(15, 25, 15, 25)
        priceText.setBackgroundColor(card)

        root.addView(priceText)

        root.addView(space(12))

        // Main buttons
        startButton = Button(this)
        startButton.text = "تشغيل البوت"

        startButton.setOnClickListener {
            toggleBot()
        }

        root.addView(startButton)

        paperButton = Button(this)
        paperButton.text = "Paper Trading: OFF"

        paperButton.setOnClickListener {
            paperTrading = !paperTrading

            paperButton.text =
                if (paperTrading) {
                    "Paper Trading: ON"
                } else {
                    "Paper Trading: OFF"
                }

            addLog(
                if (paperTrading)
                    "تم تشغيل التداول التجريبي"
                else
                    "تم إيقاف التداول التجريبي"
            )
        }

        root.addView(paperButton)

        root.addView(space(10))

        // Signal
        val signalTitle = sectionTitle("إشارة السوق")
        root.addView(signalTitle)

        signalText = TextView(this)
        signalText.text = "WAIT"
        signalText.textSize = 30f
        signalText.setTextColor(orange)
        signalText.gravity = Gravity.CENTER
        signalText.setPadding(15, 25, 15, 25)
        signalText.setBackgroundColor(card)

        root.addView(signalText)

        root.addView(space(12))

        // Indicators
        root.addView(sectionTitle("تحليل السوق"))

        indicatorsText = TextView(this)
        indicatorsText.text =
            "EMA 9: --\n" +
            "EMA 21: --\n" +
            "RSI: --\n" +
            "MACD: --\n" +
            "الاتجاه: --\n" +
            "عدد الأسعار: 0"

        indicatorsText.textSize = 17f
        indicatorsText.setTextColor(white)
        indicatorsText.setPadding(20, 20, 20, 20)
        indicatorsText.setBackgroundColor(card)

        root.addView(indicatorsText)

        root.addView(space(12))

        // SL / TP
        root.addView(sectionTitle("إدارة الصفقة"))

        tradeText = TextView(this)
        tradeText.text =
            "Entry: --\n" +
            "Stop Loss: --\n" +
            "Take Profit: --\n" +
            "Lot/Size: --\n" +
            "Risk: 1%"

        tradeText.textSize = 17f
        tradeText.setTextColor(white)
        tradeText.setPadding(20, 20, 20, 20)
        tradeText.setBackgroundColor(card)

        root.addView(tradeText)

        root.addView(space(12))

        // Account
        root.addView(sectionTitle("الحساب التجريبي"))

        balanceText = TextView(this)
        balanceText.text =
            "الرصيد: 10000.00 $\n" +
            "الصفقة: لا توجد"

        balanceText.textSize = 17f
        balanceText.setTextColor(gray)
        balanceText.setPadding(20, 20, 20, 20)
        balanceText.setBackgroundColor(card)

        root.addView(balanceText)

        root.addView(space(10
