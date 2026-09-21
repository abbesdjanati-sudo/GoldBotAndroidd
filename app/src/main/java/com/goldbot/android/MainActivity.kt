package com.goldbot.android

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {

    private val bg = Color.rgb(15, 18, 25)
    private val card = Color.rgb(25, 29, 38)
    private val gold = Color.rgb(255, 193, 7)
    private val white = Color.WHITE
    private val gray = Color.LTGRAY
    private val green = Color.rgb(50, 200, 100)
    private val red = Color.rgb(240, 70, 70)

    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var indicatorText: TextView
    private lateinit var accountText: TextView
    private lateinit var logText: TextView

    private val prices = ArrayList<Double>()

    private var currentPrice = 0.0
    private var botRunning = false

    private var balance = 10000.0
    private var riskPercent = 1.0

    private var position = ""
    private var entryPrice = 0.0
    private var stopLoss = 0.0
    private var takeProfit = 0.0

    private var telegramToken = ""
    private var telegramChatId = ""

    private val handler = Handler(Looper.getMainLooper())

    private val updateTask = object : Runnable {
        override fun run() {
            fetchGoldPrice()

            if (botRunning) {
                handler.postDelayed(this, 15000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        loadSettings()
        buildInterface()
        fetchGoldPrice()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun buildInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 30, 24, 30)
        root.setBackgroundColor(bg)

        scroll.addView(root)

        val title = makeText(
            "GOLD BOT",
            30f,
            gold,
            true
        )
        title.gravity = Gravity.CENTER
        root.addView(title)

        addSpace(root, 12)

        statusText = makeText(
            "● البوت متوقف",
            18f,
            red,
            true
        )
        statusText.gravity = Gravity.CENTER
        root.addView(statusText)

        addSpace(root, 12)

        val priceCard = createCard()

        val priceTitle = makeText(
            "XAU/USD",
            18f,
            gray,
            true
        )

        priceText = makeText(
            "جاري جلب السعر...",
            28f,
            gold,
            true
        )
        priceText.gravity = Gravity.CENTER

        priceCard.addView(priceTitle)
        priceCard.addView(priceText)

        root.addView(priceCard)

        addSpace(root, 12)

        val signalCard = createCard()

        val signalTitle = makeText(
            "إشارة التداول",
            18f,
            white,
            true
        )

        signalText = makeText(
            "WAIT",
            30f,
            gold,
            true
        )
        signalText.gravity = Gravity.CENTER

        signalCard.addView(signalTitle)
        signalCard.addView(signalText)

        root.addView(signalCard)

        addSpace(root, 12)

        val indicatorCard = createCard()

        indicatorText = makeText(
            "EMA 9: --\nEMA 21: --\nRSI 14: --\nMACD: --\nSL: --\nTP: --",
            17f,
            white,
            false
        )

        indicatorCard.addView(indicatorText)
        root.addView(indicatorCard)

        addSpace(root, 12)

        val startButton = Button(this)
        startButton.text = "تشغيل / إيقاف البوت"
        startButton.setOnClickListener {
            toggleBot()
        }

        root.addView(startButton)

        val analyzeButton = Button(this)
        analyzeButton.text = "تحليل السوق الآن"
        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        root.addView(analyzeButton)

        val paperButton = Button(this)
        paperButton.text = "فتح صفقة تجريبية"
        paperButton.setOnClickListener {
            managePaperTrade()
        }

        root.addView(paperButton)

        val settingsButton = Button(this)
        settingsButton.text = "الإعدادات"
        settingsButton.setOnClickListener {
            showSettings()
        }

        root.addView(settingsButton)

        val telegramButton = Button(this)
        telegramButton.text = "إرسال الإشارة إلى Telegram"
        telegramButton.setOnClickListener {
            sendTelegram()
        }

        root.addView(telegramButton)

        addSpace(root, 12)

        val accountCard = createCard()

        accountText = makeText(
            "",
            17f,
            white,
            false
        )

        accountCard.addView(accountText)
        root.addView(accountCard)

        addSpace(root, 12)

        val logTitle = makeText(
            "السجل",
            18f,
            gold,
            true
        )

        root.addView(logTitle)

        logText = makeText(
            "GoldBot بدأ العمل.\n",
            14f,
            gray,
            false
        )

        root.addView(logText)

        updateAccountUI()

        setContentView(scroll)
    }

    private fun fetchGoldPrice() {

        thread {

            try {

                val url = URL("https://api.gold-api.com/price/XAU")

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val code = connection.responseCode

                if (code != 200) {
                    throw Exception("HTTP $code")
                }

                val text =
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                val json = JSONObject(text)

                val price = json.getDouble("price")

                runOnUiThread {
                    updatePrice(price)
                }

                connection.disconnect()

            } catch (e: Exception) {

                runOnUiThread {
                    statusText.text = "● خطأ في الاتصال"
                    statusText.setTextColor(red)

                    addLog("فشل جلب السعر: ${e.message}")
                }
            }
        }
    }

    private fun updatePrice(price: Double) {

        currentPrice = price

        prices.add(price)

        if (prices.size > 200) {
            prices.removeAt(0)
        }

        priceText.text =
            String.format(Locale.US, "%.2f $", price)

        statusText.text =
            if (botRunning) "● البوت يعمل"
            else "● البوت متوقف"

        statusText.setTextColor(
            if (botRunning) green else red
        )

        calculateIndicators()

        if (botRunning) {
            managePaperTrade()
        }
    }

    private fun calculateIndicators() {

        if (prices.size < 10) {

            indicatorText.text =
                "الأسعار المتاحة: ${prices.size}\n" +
                "نحتاج بيانات أكثر للتحليل."

            signalText.text = "WAIT"
            signalText.setTextColor(gold)

            return
        }

        val ema9 = calculateEMA(prices, 9)
        val ema21 =
            if (prices.size >= 21)
                calculateEMA(prices, 21)
            else
                ema9

        val rsi =
            if (prices.size >= 15)
                calculateRSI(prices, 14)
            else
                50.0

        val macd = calculateMACD(prices)

        val signal = generateSignal(
            ema9,
            ema21,
            rsi,
            macd
        )

        val sl = calculateSL(signal)
        val tp = calculateTP(signal)

        indicatorText.text =
            "EMA 9: ${format(ema9)}\n" +
            "EMA 21: ${format(ema21)}\n" +
            "RSI 14: ${format(rsi)}\n" +
            "MACD: ${format(macd)}\n" +
            "SL: ${if (sl > 0) format(sl) else "--"}\n" +
            "TP: ${if (tp > 0) format(tp) else "--"}"

        showSignal(signal)
    }

    private fun calculateEMA(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.isEmpty()) return 0.0

        val actualPeriod =
            min(period, data.size)

        var ema = data.take(actualPeriod).average()

        val multiplier =
            2.0 / (actualPeriod + 1)

        for (i in actualPeriod until data.size) {

            ema =
                (data[i] - ema) *
                multiplier + ema
        }

        return ema
    }

    private fun calculateRSI(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.size <= period) return 50.0

        var gains = 0.0
        var losses = 0.0

        val start = data.size - period

        for (i in start until data.size) {

            if (i == 0) continue

            val change =
                data[i] - data[i - 1]

            if (change > 0) {
                gains += change
            } else {
                losses += abs(change)
            }
        }

        if (losses == 0.0) return 100.0

        val rs = gains / losses

        return 100.0 - (100.0 / (1.0
