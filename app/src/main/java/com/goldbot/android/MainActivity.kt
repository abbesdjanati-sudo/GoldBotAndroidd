package com.goldbot.android

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {

    // ============================================================
    // UI
    // ============================================================

    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var analysisText: TextView
    private lateinit var tradeText: TextView
    private lateinit var accountText: TextView
    private lateinit var logText: TextView

    private lateinit var startButton: Button
    private lateinit var paperButton: Button

    // ============================================================
    // Price data
    // ============================================================

    private val prices = ArrayList<Double>()

    private var currentPrice = 0.0

    // ============================================================
    // Bot state
    // ============================================================

    private var botRunning = false
    private var paperTrading = false

    private var currentSignal = "WAIT"

    // ============================================================
    // Paper account
    // ============================================================

    private var balance = 10000.0
    private var startingBalance = 10000.0

    private var position = ""
    private var entryPrice = 0.0
    private var stopLoss = 0.0
    private var takeProfit = 0.0
    private var positionSize = 0.0

    private var riskPercent = 1.0

    // ============================================================
    // Telegram
    // ============================================================

    private var telegramToken = ""
    private var telegramChatId = ""

    // ============================================================
    // MT5
    // ============================================================

    private var mt5Server = ""
    private var mt5Login = ""
    private var mt5Password = ""

    // ============================================================
    // Handler
    // ============================================================

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {

        override fun run() {

            if (botRunning) {
                fetchGoldPrice()
            }

            handler.postDelayed(this, 15000)
        }
    }

    // ============================================================
    // Colors
    // ============================================================

    private val bgColor = Color.rgb(15, 18, 25)
    private val cardColor = Color.rgb(27, 31, 42)

    private val goldColor = Color.rgb(255, 215, 0)
    private val greenColor = Color.rgb(0, 220, 120)
    private val redColor = Color.rgb(255, 80, 80)
    private val orangeColor = Color.rgb(255, 170, 0)

    // ============================================================
    // Activity
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor

        buildInterface()
        loadSettings()

        handler.postDelayed(updateRunnable, 1000)
    }

    override fun onDestroy() {

        handler.removeCallbacks(updateRunnable)

        super.onDestroy()
    }

    // ============================================================
    // Build interface
    // ============================================================

    private fun buildInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 40)
        root.setBackgroundColor(bgColor)

        scroll.addView(root)

        // --------------------------------------------------------
        // Title
        // --------------------------------------------------------

        val title = TextView(this)

        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(goldColor)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val subtitle = TextView(this)

        subtitle.text = "XAU/USD AI Trading Assistant"
        subtitle.textSize = 14f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        root.addView(subtitle)

        root.addView(space(15))

        // --------------------------------------------------------
        // Status
        // --------------------------------------------------------

        statusText = TextView(this)

        statusText.text = "البوت متوقف"
        statusText.textSize = 19f
        statusText.setTextColor(redColor)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(15, 15, 15, 15)
        statusText.setBackgroundColor(cardColor)

        root.addView(statusText)

        root.addView(space(10))

        // --------------------------------------------------------
        // Price
        // --------------------------------------------------------

        priceText = TextView(this)

        priceText.text = "XAU/USD\n--"
        priceText.textSize = 28f
        priceText.setTextColor(goldColor)
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(15, 25, 15, 25)
        priceText.setBackgroundColor(cardColor)

        root.addView(priceText)

        root.addView(space(10))

        // --------------------------------------------------------
        // Start
        // --------------------------------------------------------

        startButton = Button(this)

        startButton.text = "تشغيل البوت"

        startButton.setOnClickListener {
            toggleBot()
        }

        root.addView(startButton)

        // --------------------------------------------------------
        // Manual refresh
        // --------------------------------------------------------

        val refreshButton = Button(this)

        refreshButton.text = "تحديث السعر"

        refreshButton.setOnClickListener {
            fetchGoldPrice()
        }

        root.addView(refreshButton)

        // --------------------------------------------------------
        // Paper trading
        // --------------------------------------------------------

        paperButton = Button(this)

        paperButton.text = "Paper Trading: OFF"

        paperButton.setOnClickListener {

            paperTrading = !paperTrading

            if (paperTrading) {

                paperButton.text = "Paper Trading: ON"

                addLog("تم تشغيل التداول التجريبي")

            } else {

                paperButton.text = "Paper Trading: OFF"

                addLog("تم إيقاف التداول التجريبي")
            }
        }

        root.addView(paperButton)

        root.addView(space(12))

        // --------------------------------------------------------
        // Signal
        // --------------------------------------------------------

        root.addView(sectionTitle("إشارة السوق"))

        signalText = TextView(this)

        signalText.text = "WAIT"
        signalText.textSize = 30f
        signalText.setTextColor(orangeColor)
        signalText.gravity = Gravity.CENTER
        signalText.setPadding(15, 25, 15, 25)
        signalText.setBackgroundColor(cardColor)

        root.addView(signalText)

        root.addView(space(12))

        // --------------------------------------------------------
        // Analysis
        // --------------------------------------------------------

        root.addView(sectionTitle("التحليل الفني"))

        analysisText = TextView(this)

        analysisText.text =
            "EMA 9: --\n" +
            "EMA 21: --\n" +
            "RSI 14: --\n" +
            "MACD: --\n" +
            "الاتجاه: --\n" +
            "الأسعار: 0"

        analysisText.textSize = 17f
        analysisText.setTextColor(Color.WHITE)
        analysisText.setPadding(20, 20, 20, 20)
        analysisText.setBackgroundColor(cardColor)

        root.addView(analysisText)

        root.addView(space(12))

        // --------------------------------------------------------
        // Trade
        // --------------------------------------------------------

        root.addView(sectionTitle("إدارة الصفقة"))

        tradeText = TextView(this)

        tradeText.text =
            "Entry: --\n" +
            "Stop Loss: --\n" +
            "Take Profit: --\n" +
            "Position Size: --\n" +
            "Risk: 1%"

        tradeText.textSize = 17f
        tradeText.setTextColor(Color.WHITE)
        tradeText.setPadding(20, 20, 20, 20)
        tradeText.setBackgroundColor(cardColor)

        root.addView(tradeText)

        root.addView(space(12))

        // --------------------------------------------------------
        // Account
        // --------------------------------------------------------

        root.addView(sectionTitle("الحساب التجريبي"))

        accountText = TextView(this)

        accountText.text =
            "الرصيد: 10000.00 $\n" +
            "الصفقة: لا توجد\n" +
            "الربح/الخسارة: 0.00 $"

        accountText.textSize = 17f
        accountText.setTextColor(Color.LTGRAY)
        accountText.setPadding(20, 20, 20, 20)
        accountText.setBackgroundColor(cardColor)

        root.addView(accountText)

        root.addView(space(8))

        // --------------------------------------------------------
        // Close trade
        // --------------------------------------------------------

        val closeTradeButton = Button(this)

        closeTradeButton.text = "إغلاق الصفقة التجريبية"

        closeTradeButton.setOnClickListener {
            closePaperTrade()
        }

        root.addView(closeTradeButton)

        // --------------------------------------------------------
        // Telegram
        // --------------------------------------------------------

        root.addView(space(10))

        root.addView(sectionTitle("الاتصالات"))

        val telegramButton = Button(this)

        telegramButton.text = "إعدادات Telegram"

        telegramButton.setOnClickListener {
            showTelegramDialog()
        }

        root.addView(telegramButton)

        // --------------------------------------------------------
        // MT5
        // --------------------------------------------------------

        val mt5Button = Button(this)

        mt5Button.text = "إعدادات MT5"

        mt5Button.setOnClickListener {
            showMt5Dialog()
        }

        root.addView(mt5Button)

        // --------------------------------------------------------
        // Reset
        // --------------------------------------------------------

        val resetButton = Button(this)

        resetButton.text = "إعادة الحساب التجريبي"

        resetButton.setOnClickListener {
            resetPaperAccount()
        }

        root.addView(resetButton)

        // --------------------------------------------------------
        // Logs
        // --------------------------------------------------------

        root.addView(space(10))

        root.addView(sectionTitle("السجل"))

        logText = TextView(this)

        logText.text = "GoldBot جاهز..."
        logText.textSize = 14f
        logText.setTextColor(Color.LTGRAY)
        logText.setPadding(15, 15, 15, 15)
        logText.setBackgroundColor(cardColor)

        root.addView(logText)

        setContentView(scroll)
    }

    // ============================================================
    // UI helpers
    // ============================================================

    private fun sectionTitle(text: String): TextView {

        val view = TextView(this)

        view.text = text
        view.textSize = 20f
        view.setTextColor(goldColor)
        view.setPadding(5, 8, 5, 8)

        return view
    }

    private fun space(height: Int): View {

        val view = Space(this)

        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        )

        return view
    }

    // ============================================================
    // Bot control
    // ============================================================

    private fun toggleBot() {

        botRunning = !botRunning

        if (botRunning) {

            statusText.text = "البوت يعمل"
            statusText.setTextColor(greenColor)

            startButton.text = "إيقاف البوت"

            addLog("تم تشغيل GoldBot")

            fetchGoldPrice()

        } else {

            statusText.text = "البوت متوقف"
            statusText.setTextColor(redColor)

            startButton.text = "تشغيل البوت"

            addLog("تم إيقاف GoldBot")
        }
    }

    // ============================================================
    // Fetch XAU price
    // ============================================================

    private fun fetchGoldPrice() {

        Thread {

            var connection: HttpURLConnection? = null

            try {

                val url = URL(
                    "https://api.gold-api.com/price/XAU"
                )

                connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode

                if (responseCode != 200) {

                    runOnUiThread {

                        addLog(
                            "خطأ مصدر السعر: HTTP $responseCode"
                        )
                    }

                    return@Thread
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                val json = JSONObject(response)

                val price = json.getDouble("price")

                runOnUiThread {
                    onNewPrice(price)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    addLog(
                        "خطأ السعر: " +
                                (e.message ?: "Unknown")
                    )
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }

    // ============================================================
    // New price
    // ============================================================

    private fun onNewPrice(price: Double) {

        if (price <= 0.0) {
            return
        }

        currentPrice = price

        prices.add(price)

        if (prices.size > 300) {
            prices.removeAt(0)
        }

        priceText.text = String.format(
            Locale.US,
            "XAU/USD\n%.2f $",
            price
        )

        calculateAnalysis()
        updatePaperTrade()
        updateAccount()
    }

    // ============================================================
    // Technical analysis
    // ============================================================

    private fun calculateAnalysis() {

        if (prices.size < 5) {

            analysisText.text =
                "EMA 9: --\n" +
                "EMA 21: --\n" +
                "RSI 14: --\n" +
                "MACD: --\n" +
                "الاتجاه: جمع البيانات...\n" +
                "الأسعار: ${prices.size}"

            signalText.text = "WAIT"
            signalText.setTextColor(orangeColor)

            currentSignal = "WAIT"

            return
        }

        val ema9 = calculateEMA(
            prices,
            9
        )

        val ema21 = calculateEMA(
            prices,
            21
        )

        val rsi = calculateRSI(
            prices,
            14
        )

        val macd = calculateMACD(
            prices
        )

        val newSignal = generateSignal(
            ema9,
            ema21,
            rsi,
            macd
        )

        currentSignal = newSignal

        val trend =
            if (ema9 > ema21) {
                "صاعد"
            } else if (ema9 < ema21) {
                "هابط"
            } else {
                "محايد"
            }

        analysisText.text = String.format(
            Locale.US,
            "EMA 9: %.2f\n" +
                    "EMA 21: %.2f\n" +
                    "RSI 14: %.2f\n" +
                    "MACD: %.4f\n" +
                    "الاتجاه: %s\n" +
                    "الأسعار: %d",
            ema9,
            ema21,
            rsi,
            macd,
            trend,
            prices.size
        )

        signalText.text = newSignal

        when (newSignal) {

            "BUY" -> {

                signalText.setTextColor(greenColor)

                calculateTradeLevels("BUY")
            }

            "SELL" -> {

                signalText.setTextColor(redColor)

                calculateTradeLevels("SELL")
            }

            else -> {

                signalText.setTextColor(orangeColor)

                tradeText.text =
                    "Entry: --\n" +
                    "Stop Loss: --\n" +
                    "Take Profit: --\n" +
                    "Position Size: --\n" +
                    "Risk: ${riskPercent}%"
            }
        }
    }

    // ============================================================
    // EMA
    // ============================================================

    private fun calculateEMA(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.isEmpty()) {
            return 0.0
        }

        val usablePeriod = min(
            period,
            data.size
        )

        var ema = 0.0

        for (i in 0 until usablePeriod) {
            ema += data[i]
        }

        ema /= usablePeriod.toDouble()

        val multiplier =
            2.0 / (period.toDouble() + 1.0)

        for (i in usablePeriod until data.size) {

            ema =
                ((data[i] - ema) * multiplier) + ema
        }

        return ema
    }

    // ============================================================
    // RSI
    // ============================================================

    private fun calculateRSI(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.size <= period) {
            return 50.0
        }

        var gains = 0.0
        var losses = 0.0

        val start =
            max(1, data.size - period)

        for (i in start until data.size) {

            val change =
                data[i] - data[i - 1]

            if (change > 0.0) {

                gains += change

            } else {

                losses += abs(change)
            }
        }

        if (losses == 0.0) {
            return 100.0
        }

        val averageGain =
            gains / period.toDouble()

        val averageLoss =
            losses / period.toDouble()

        if (averageLoss == 0.0) {
            return 100.0
        }

        val rs =
            averageGain / averageLoss

        return 100.0 -
                (100.0 / (1.0 + rs))
    }

    // ============================================================
    // MACD
    // ============================================================

    private fun calculateMACD(
        data: List<Double>
    ): Double {

        if (data.size < 5) {
            return 0.0
        }

        val fast = calculateEMA(
            data,
            12
        )

        val slow = calculateEMA(
            data,
            26
        )

        return fast - slow
    }

    // ============================================================
    // Signal engine
    // ============================================================

    private fun generateSignal(
        ema9: Double,
        ema21: Double,
        rsi: Double,
        macd: Double
    ): String {

        var buyScore = 0
        var sellScore = 0

        if (ema9 > ema21) {
            buyScore++
        }

        if (ema9 < ema21) {
            sellScore++
        }

        if (rsi > 50.0 && rsi < 70.0) {
            buyScore++
        }

        if (rsi < 50.0 && rsi > 30.0) {
            sellScore++
        }

        if (macd > 0.0) {
            buyScore++
        }

        if (macd < 0.0) {
            sellScore++
        }

        return when {

            buyScore >= 3 -> "BUY"

            sellScore >= 3 -> "SELL"

            else -> "WAIT"
        }
    }

    // ============================================================
    // Trade levels
    // ============================================================

    private fun calculateTradeLevels(
        direction: String
    ) {

        if (currentPrice <= 0.0) {
            return
        }

        val distance =
            max(
                currentPrice * 0.002,
                1.0
            )

       
