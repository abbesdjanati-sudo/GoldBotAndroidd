package com.goldbot.android

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputType
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

class MainActivity : Activity() {

    private lateinit var priceText: TextView
    private lateinit var changeText: TextView
    private lateinit var updateText: TextView
    private lateinit var trendText: TextView
    private lateinit var signalText: TextView
    private lateinit var indicatorText: TextView
    private lateinit var activityText: TextView
    private lateinit var statusText: TextView
    private lateinit var accountText: TextView
    private lateinit var positionText: TextView
    private lateinit var startButton: Button

    private var botRunning = false
    private var currentPrice = 0.0
    private var previousPrice = 0.0

    private val prices = mutableListOf<Double>()

    private var balance = 1000.0
    private var riskPercent = 1.0
    private var stopLossDistance = 5.0
    private var takeProfitDistance = 10.0

    private var paperDirection = ""
    private var paperEntry = 0.0
    private var paperSL = 0.0
    private var paperTP = 0.0
    private var paperSize = 1.0

    private var lastSignal = "WAIT"

    private val prefs by lazy {
        getSharedPreferences("goldbot_settings", MODE_PRIVATE)
    }

    private val bg = Color.rgb(12, 15, 22)
    private val card = Color.rgb(24, 29, 39)
    private val white = Color.WHITE
    private val gray = Color.rgb(170, 178, 190)
    private val green = Color.rgb(35, 190, 105)
    private val red = Color.rgb(225, 75, 75)
    private val gold = Color.rgb(230, 180, 70)
    private val blue = Color.rgb(70, 140, 230)

    private val handler = Handler(mainLooper)

    private val updateTask = object : Runnable {
        override fun run() {
            fetchGoldPrice()
            handler.postDelayed(this, 15000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        loadSettings()
        buildInterface()

        // تأخير الاتصال حتى يكتمل إنشاء الواجهة
        handler.postDelayed({
            fetchGoldPrice()
        }, 700)

        handler.postDelayed(updateTask, 15700)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateTask)
        super.onDestroy()
    }

    private fun buildInterface() {

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(22, 20, 22, 30)

        val title = makeText("◉ GoldBot", 28f, gold)
        title.setTypeface(null, Typeface.BOLD)
        root.addView(title)

        addSpace(root, 18)

        // ================= STATUS =================

        val statusCard = createCard()

        statusCard.addView(
            makeText("● حالة الروبوت", 15f, gray)
        )

        statusText = makeText("● متوقف", 24f, red)
        statusText.setTypeface(null, Typeface.BOLD)
        statusCard.addView(statusText)

        startButton = Button(this)
        startButton.text = "▶ تشغيل الروبوت"
        startButton.setTextColor(white)
        startButton.setBackgroundColor(green)

        startButton.setOnClickListener {
            toggleBot()
        }

        statusCard.addView(startButton, buttonParams())
        root.addView(statusCard)

        addSpace(root, 14)

        // ================= GOLD =================

        val marketCard = createCard()

        marketCard.addView(
            makeText("🥇 الذهب", 15f, gray)
        )

        marketCard.addView(
            makeText("XAU/USD", 24f, white)
        )

        priceText = makeText("جاري جلب السعر...", 30f, gold)
        priceText.setTypeface(null, Typeface.BOLD)
        marketCard.addView(priceText)

        changeText = makeText("التغير: --", 17f, gray)
        marketCard.addView(changeText)

        updateText = makeText("آخر تحديث: --", 14f, gray)
        marketCard.addView(updateText)

        marketCard.addView(
            makeText("● مصدر السعر عبر الإنترنت", 14f, green)
        )

        root.addView(marketCard)

        addSpace(root, 14)

        // ================= ANALYSIS =================

        val analysisCard = createCard()

        analysisCard.addView(
            makeText("📊 تحليل السوق", 15f, gray)
        )

        trendText = makeText(
            "الاتجاه: في انتظار البيانات",
            22f,
            gold
        )
        trendText.setTypeface(null, Typeface.BOLD)
        analysisCard.addView(trendText)

        signalText = makeText(
            "⏳ WAIT - نحتاج بيانات أكثر",
            20f,
            gold
        )
        signalText.setTypeface(null, Typeface.BOLD)
        analysisCard.addView(signalText)

        indicatorText = makeText(
            "EMA20: --\nEMA50: --\nRSI: --\nMACD: --",
            15f,
            gray
        )
        analysisCard.addView(indicatorText)

        root.addView(analysisCard)

        addSpace(root, 14)

        // ================= ACCOUNT =================

        val accountCard = createCard()

        accountCard.addView(
            makeText("💰 الحساب التجريبي", 15f, gray)
        )

        accountText = makeText(
            "الرصيد: $1,000.00\nالمخاطرة: 1%\nمبلغ المخاطرة: $10.00",
            18f,
            white
        )
        accountCard.addView(accountText)

        positionText = makeText(
            "لا توجد صفقة تجريبية",
            16f,
            gray
        )
        accountCard.addView(positionText)

        root.addView(accountCard)

        addSpace(root, 14)

        // ================= ACTIONS =================

        val actionsCard = createCard()

        actionsCard.addView(
            makeText("⚡ التحكم", 15f, gray)
        )

        val refreshButton = Button(this)
        refreshButton.text = "🔄 تحديث سعر الذهب"
        refreshButton.setTextColor(white)
        refreshButton.setBackgroundColor(Color.rgb(55, 65, 85))

        refreshButton.setOnClickListener {
            fetchGoldPrice()
        }

        actionsCard.addView(refreshButton, buttonParams())

        val analyzeButton = Button(this)
        analyzeButton.text = "🔍 تحليل XAU/USD"
        analyzeButton.setTextColor(white)
        analyzeButton.setBackgroundColor(Color.rgb(55, 65, 85))

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        actionsCard.addView(analyzeButton, buttonParams())

        val settingsButton = Button(this)
        settingsButton.text = "⚙️ الإعدادات"
        settingsButton.setTextColor(white)
        settingsButton.setBackgroundColor(Color.rgb(55, 65, 85))

        settingsButton.setOnClickListener {
            showSettings()
        }

        actionsCard.addView(settingsButton, buttonParams())

        val telegramButton = Button(this)
        telegramButton.text = "🔔 اختبار Telegram"
        telegramButton.setTextColor(white)
        telegramButton.setBackgroundColor(Color.rgb(55, 65, 85))

        telegramButton.setOnClickListener {
            sendTelegram(
                "🤖 GoldBot\nاختبار اتصال Telegram ناجح."
            )
        }

        actionsCard.addView(telegramButton, buttonParams())

        root.addView(actionsCard)

        addSpace(root, 14)

        // ================= LOG =================

        val logCard = createCard()

        logCard.addView(
            makeText("📜 سجل الروبوت", 15f, gray)
        )

        activityText = makeText(
            "جاري تجهيز GoldBot...",
            15f,
            gray
        )

        logCard.addView(activityText)
        root.addView(logCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot • XAU/USD • Paper Trading",
            12f,
            gray
        )

        footer.gravity = Gravity.CENTER
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)

        updateAccountUI()
    }

    // =========================================================
    // PRICE
    // =========================================================

    private fun fetchGoldPrice() {

        runOnUiThread {
            if (currentPrice <= 0) {
                priceText.text = "جاري التحديث..."
            }
        }

        thread {

            var connection: HttpURLConnection? = null

            try {

                val url = URL(
                    "https://api.gold-api.com/price/XAU"
                )

                connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.useCaches = false

                val code = connection.responseCode

                if (code != 200) {
                    runOnUiThread {
                        showError("HTTP $code")
                    }
                    return@thread
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val json = JSONObject(response)

                val price =
                    json.optDouble("price", 0.0)

                if (price <= 0) {
                    runOnUiThread {
                        showError("السعر غير موجود")
                    }
                    return@thread
                }

                runOnUiThread {
                    updatePrice(price)
                }

            } catch (e: Exception) {

                runOnUiThread {
                    showError(
                        "تعذر الاتصال بالإنترنت"
                    )
                }

            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun updatePrice(price: Double) {

        previousPrice = currentPrice
        currentPrice = price

        prices.add(price)

        if (prices.size > 300) {
            prices.removeAt(0)
        }

        priceText.text =
            String.format(
                Locale.US,
                "$%,.2f",
                price
            )

        priceText.setTextColor(gold)

        updateText.text = "آخر تحديث: الآن"

        if (previousPrice > 0) {

            val difference =
                currentPrice - previousPrice

            val percent =
                difference / previousPrice * 100.0

            val sign =
                if (difference >= 0) "+" else ""

            changeText.text =
                String.format(
                    Locale.US,
                    "التغير: %s%.2f (%.3f%%)",
                    sign,
                    difference,
                    percent
                )

            changeText.setTextColor(
                if (difference >= 0) green
                else red
            )
        }

        activityText.text =
            "✓ تم تحديث XAU/USD\n" +
            "السعر: " +
            String.format(
                Locale.US,
                "$%,.2f",
                currentPrice
            ) +
            "\nعدد القراءات: " +
            prices.size

        calculateIndicators()

        managePaperTrade()
    }

    // =========================================================
    // INDICATORS
    // =========================================================

    private fun calculateIndicators() {

        if (prices.size < 5) {

            trendText.text =
                "الاتجاه: نحتاج قراءات أكثر"

            trendText.setTextColor(gold)

            signalText.text =
                "⏳ WAIT - في انتظار البيانات"

            signalText.setTextColor(gold)

            indicatorText.text =
                "EMA20: --\nEMA50: --\nRSI: --\nMACD: --"

            return
        }

        val ema20 = calculateEMA(prices, 20)
        val ema50 = calculateEMA(prices, 50)
        val rsi = calculateRSI(prices, 14)

        val macdResult = calculateMACD(prices)

        val macd = macdResult.first
        val macdSignal = macdResult.second

        indicatorText.text =
            String.format(
                Locale.US,
                "EMA20: %.2f\nEMA50: %.2f\nRSI: %.2f\nMACD: %.4f\nMACD Signal: %.4f",
                ema20,
                ema50,
                rsi,
                macd,
                macdSignal
            )

        if (currentPrice > ema20 &&
            ema20 > ema50
        ) {

            trendText.text =
                "🟢 الاتجاه: صاعد"

            trendText.setTextColor(green)

        } else if (
            currentPrice < ema20 &&
            ema20 < ema50
        ) {

            trendText.text =
                "🔴 الاتجاه: هابط"

            trendText.setTextColor(red)

        } else {

            trendText.text =
                "🟡 الاتجاه: محايد"

            trendText.setTextColor(gold)
        }

        val signal = generateSignal(
            ema20,
            ema50,
            rsi,
            macd,
            macdSignal
        )

        showSignal(signal)
    }

    private fun calculateEMA(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.isEmpty()) return 0.0

        val actualPeriod =
            minOf(period, data.size)

        var ema = data[0]

        val multiplier =
            2.0 / (actualPeriod + 1)

        for (i in 1 until data.size) {

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

        if (data.size < 2) {
            return 50.0
        }

        val actualPeriod =
            minOf(period, data.size - 1)

        var gains = 0.0
        var losses = 0.0

        val start =
            data.size - actualPeriod

        for (i in start until data.size) {

            val change =
                data[i] - data[i - 1]

            if (change > 0) {
                gains += change
            } else {
                losses += abs(change)
            }
        }

        if (losses == 0.0) {
            return 100.0
        }

        val averageGain =
            gains / actualPeriod

        val averageLoss =
            losses / actualPeriod

        val rs =
            averageGain / averageLoss

        return 100.0 -
            (100.0 / (1.0 + rs))
    }

    private fun calculateMACD(
        data: List<Double>
    ): Pair<Double, Double> {

        if (data.size < 3) {
            return Pair(0.0, 0.0)
        }

        val ema12 =
            calculateEMA(data,
