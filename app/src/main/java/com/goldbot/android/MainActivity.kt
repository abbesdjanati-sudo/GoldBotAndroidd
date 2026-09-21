package com.goldbot.android

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
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
    private lateinit var trendText: TextView
    private lateinit var signalText: TextView
    private lateinit var indicatorText: TextView
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var accountText: TextView
    private lateinit var positionText: TextView
    private lateinit var startButton: Button

    private var currentPrice = 0.0
    private var previousPrice = 0.0
    private var botRunning = false

    private val prices = mutableListOf<Double>()

    private var balance = 1000.0
    private var riskPercent = 1.0
    private var slDistance = 5.0
    private var tpDistance = 10.0

    private var position = ""
    private var entry = 0.0
    private var stopLoss = 0.0
    private var takeProfit = 0.0
    private var positionSize = 1.0

    private val bg = Color.rgb(12, 15, 22)
    private val card = Color.rgb(24, 29, 39)
    private val white = Color.WHITE
    private val gray = Color.rgb(170, 178, 190)
    private val green = Color.rgb(35, 190, 105)
    private val red = Color.rgb(225, 75, 75)
    private val gold = Color.rgb(230, 180, 70)

    private val handler = Handler(mainLooper)

    private val refreshTask = object : Runnable {
        override fun run() {
            fetchGoldPrice()
            handler.postDelayed(this, 15000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.statusBarColor = bg
    window.navigationBarColor = bg

    buildInterface()
} 

        

    override fun onDestroy() {
        handler.removeCallbacks(refreshTask)
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

        addSpace(root, 15)

        val statusCard = createCard()

        statusCard.addView(
            makeText("● حالة الروبوت", 15f, gray)
        )

        statusText = makeText(
            "● متوقف",
            23f,
            red
        )

        statusText.setTypeface(null, Typeface.BOLD)
        statusCard.addView(statusText)

        startButton = Button(this)
        startButton.text = "▶ تشغيل الروبوت"
        startButton.setTextColor(white)
        startButton.setBackgroundColor(green)

        startButton.setOnClickListener {
            toggleBot()
        }

        statusCard.addView(
            startButton,
            buttonParams()
        )

        root.addView(statusCard)

        addSpace(root, 12)

        val marketCard = createCard()

        marketCard.addView(
            makeText("🥇 XAU/USD", 20f, white)
        )

        priceText = makeText(
            "جاري جلب السعر...",
            30f,
            gold
        )

        priceText.setTypeface(null, Typeface.BOLD)
        marketCard.addView(priceText)

        changeText = makeText(
            "التغير: --",
            16f,
            gray
        )

        marketCard.addView(changeText)

        marketCard.addView(
            makeText(
                "● السعر عبر الإنترنت",
                13f,
                green
            )
        )

        root.addView(marketCard)

        addSpace(root, 12)

        val analysisCard = createCard()

        analysisCard.addView(
            makeText(
                "📊 تحليل السوق",
                18f,
                gray
            )
        )

        trendText = makeText(
            "الاتجاه: انتظار البيانات",
            21f,
            gold
        )

        trendText.setTypeface(null, Typeface.BOLD)
        analysisCard.addView(trendText)

        signalText = makeText(
            "🟡 WAIT",
            22f,
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

        addSpace(root, 12)

        val accountCard = createCard()

        accountCard.addView(
            makeText(
                "💰 الحساب التجريبي",
                18f,
                gray
            )
        )

        accountText = makeText(
            "",
            17f,
            white
        )

        accountCard.addView(accountText)

        positionText = makeText(
            "لا توجد صفقة",
            16f,
            gray
        )

        accountCard.addView(positionText)

        root.addView(accountCard)

        addSpace(root, 12)

        val actionCard = createCard()

        actionCard.addView(
            makeText(
                "⚡ التحكم",
                18f,
                gray
            )
        )

        val refresh = Button(this)
        refresh.text = "🔄 تحديث السعر"
        refresh.setTextColor(white)
        refresh.setOnClickListener {
            fetchGoldPrice()
        }

        actionCard.addView(
            refresh,
            buttonParams()
        )

        val analyze = Button(this)
        analyze.text = "🔍 تحليل السوق"
        analyze.setTextColor(white)
        analyze.setOnClickListener {
            analyzeMarket()
        }

        actionCard.addView(
            analyze,
            buttonParams()
        )

        val settings = Button(this)
        settings.text = "⚙️ الإعدادات"
        settings.setTextColor(white)
        settings.setOnClickListener {
            showSettings()
        }

        actionCard.addView(
            settings,
            buttonParams()
        )

        val telegram = Button(this)
        telegram.text = "🔔 اختبار Telegram"
        telegram.setTextColor(white)
        telegram.setOnClickListener {
            sendTelegram("🤖 GoldBot\nاختبار Telegram")
        }

        actionCard.addView(
            telegram,
            buttonParams()
        )

        root.addView(actionCard)

        addSpace(root, 12)

        val logCard = createCard()

        logCard.addView(
            makeText(
                "📜 السجل",
                18f,
                gray
            )
        )

        logText = makeText(
            "GoldBot جاهز...",
            14f,
            gray
        )

        logCard.addView(logText)
        root.addView(logCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot • Paper Trading",
            12f,
            gray
        )

        footer.gravity = Gravity.CENTER
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)

        updateAccount()
    }

    private fun fetchGoldPrice() {

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

                val responseCode =
                    connection.responseCode

                if (responseCode != 200) {

                    runOnUiThread {
                        showError(
                            "HTTP $responseCode"
                        )
                    }

                    return@thread
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val json =
                    JSONObject(response)

                val price =
                    json.optDouble(
                        "price",
                        0.0
                    )

                if (price <= 0.0) {

                    runOnUiThread {
                        showError(
                            "السعر غير موجود"
                        )
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

        if (previousPrice > 0.0) {

            val difference =
                currentPrice - previousPrice

            val percent =
                difference /
                    previousPrice *
                    100.0

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
                if (difference >= 0)
                    green
                else
                    red
            )
        }

        logText.text =
            String.format(
                Locale.US,
                "✓ تم تحديث السعر\nالسعر: $%.2f\nالقراءات: %d",
                currentPrice,
                prices.size
            )

        calculateIndicators()
        managePaperTrade()
    }

    private fun calculateIndicators() {

        if (prices.size < 5) {

            trendText.text =
                "الاتجاه: نحتاج بيانات أكثر"

            trendText.setTextColor(gold)

            signalText.text =
                "🟡 WAIT"

            signalText.setTextColor(gold)

            return
        }

        val ema20 =
            calculateEMA(
                prices,
                20
            )

        val ema50 =
            calculateEMA(
                prices,
                50
            )

        val rsi =
            calculateRSI(
                prices,
                14
            )

        val macdData =
            calculateMACD(prices)

        val macd =
            macdData.first

        val macdSignal =
            macdData.second

        indicatorText.text =
            String.format(
                Locale.US,
                "EMA20: %.2f\nEMA50: %.2f\nRSI: %.2f\nMACD: %.4f\nSignal: %.4f",
                ema20,
                ema50,
                rsi,
                macd,
                macdSignal
            )

        if (
            currentPrice > ema20 &&
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

        val signal =
            generateSignal(
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

        if (data.isEmpty()) {
            return 0.0
        }

        var ema = data[0]

        val multiplier =
            2.0 /
                (period + 1)

        for (i in 1 until data.size) {

            ema =
                (
                    (data[i] - ema) *
                        multiplier
                    ) + ema
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

        val count =
            minOf(
                period,
                data.size - 1
            )

        var gains = 0.0
        var losses = 0.0

        val start =
            data.size - count

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

        val rs =
            (gains / count) /
                (losses / count)

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
            calculateEMA(
                data,
                12
            )

        val ema26 =
            calculateEMA(
                data,
                26
            )

        val macd =
            ema12 - ema26

        val values =
            mutableListOf<Double>()

        for (i in 2 until data.size) {

            val part =
                data.subList(
                    0,
                    i + 1
                )

            values.add(
                calculateEMA(
                    part,
                    12
                ) -
                    calculateEMA(
                        part,
                        26
                    )
            )
        }

        val signal =
            calculateEMA(
                values,
                9
            )

        return Pair(
            macd,
            signal
        )
    }

    private fun generateSignal(
        ema20: Double,
        ema50: Double,
        rsi: Double,
        macd: Double,
        macdSignal: Double
    ): String {

        if (
            currentPrice > ema20 &&
            ema20 > ema50 &&
            rsi >= 50.0 &&
            rsi <= 70.0 &&
            macd > macdSignal
        ) {
            return "BUY"
        }

        if (
            currentPrice < ema20 &&
            ema20 < ema50 &&
            rsi >= 30.0 &&
            rsi <= 50.0 &&
            macd < macdSignal
        ) {
            return "SELL"
        }

        return "WAIT"
    }

    private fun showSignal(
        signal: String
    ) {

        when (signal) {

            "BUY" -> {

                signalText.text =
                    "🟢 BUY"

                signalText.setTextColor(
                    green
                )
            }

            "SELL" -> {

                signalText.text =
                    "🔴 SELL"

                signalText.setTextColor(
                    red
                )
            }

            else -> {

                signalText.text =
                    "🟡 WAIT"

                signalText.setTextColor(
                    gold
                )
            }
        }
    }

    private fun toggleBot() {

        botRunning =
            !botRunning

        if (botRunning) {

            statusText.text =
                "● يعمل الآن"

            statusText.setTextColor(
                green
            )

            startButton.text =
                "■ إيقاف الروبوت"

            startButton.setBackgroundColor(
                red
            )

            logText.text =
                "✓ GoldBot يعمل\n" +
                "🧪 Paper Trading\n" +
                "⏳ مراقبة XAU/USD"

        } else {

            statusText.text =
                "● متوقف"

            statusText.setTextColor(
                red
            )

            startButton.text =
                "▶ تشغيل الروبوت"

            startButton.setBackgroundColor(
                green
            )

            logText.text =
                "GoldBot متوقف"
        }
    }

    private fun managePaperTrade() {

        if (!botRunning) {
            return
        }

        if (currentPrice <= 0.0) {
            return
        }

        if (position.isEmpty()) {

            if (prices.size < 5) {
                return
            }

            val ema20 =
                calculateEMA(
                    prices,
                    20
                )

            val ema50 =
                calculateEMA(
                    prices,
                    50
                )

            val rsi =
                calculateRSI(
                    prices,
                    14
                )

            val macd =
                calculateMACD(
                    prices
                )

            val signal =
                generateSignal(
                    ema20,
                    ema50,
                    rsi,
                    macd.first,
                    macd.second
                )

            if (signal == "BUY") {
                openPaperTrade("BUY")
            }

            if (signal == "SELL") {
                openPaperTrade("SELL")
            }

            return
        }

        val profit =
            if (position == "BUY") {
                (currentPrice - entry) *
                    positionSize
            } else {
                (entry - currentPrice) *
                    positionSize
            }

        var close = false

        if (position == "BUY") {

            if (
                currentPrice <= stopLoss ||
                currentPrice >= takeProfit
            ) {
                close = true
            }

        } else {

            if (
                currentPrice >= stopLoss ||
                currentPrice <= takeProfit
            ) {
                close = true
            }
        }

        if (close) {

            balance += profit

            logText.text =
                String.format(
                    Locale.US,
                    "✓ إغلاق صفقة\nP/L: $%.2f",
                    profit
                )

            position = ""
            entry = 0.0
            stopLoss = 0.0
            takeProfit = 0.0

            updateAccount()
        }
    }

    private fun openPaperTrade(
        direction: String
    ) {

        position = direction
        entry = currentPrice

        if (direction == "BUY") {

            stopLoss =
                entry - slDistance

            takeProfit =
                entry + tpDistance

        } else {

            stopLoss =
                entry + slDistance

            takeProfit =
                entry - tpDistance
        }

        val riskMoney =
            balance *
                riskPercent /
                100.0

        positionSize =
            if (slDistance > 0) {
                riskMoney /
                    slDistance
            } else {
                1.0
            }

        positionText.text =
            String.format(
                Locale.US,
                "%s\nالدخول: %.2f\nSL: %.2f\nTP: %.2f",
                direction,
                entry,
                stopLoss,
                takeProfit
            )

        positionText.setTextColor(
            if (direction == "BUY")
                green
            else
                red
        )
    }

    private fun analyzeMarket() {

        if (prices.size < 5) {

            logText.text =
                "⏳ نحتاج 5 قراءات على الأقل"

            fetchGoldPrice()

            return
        }

        calculateIndicators()

        logText.text =
            "✓ تم تحليل السوق\n" +
            "السعر الحالي: " +
            String.format(
                Locale.US,
                "%.2f",
                currentPrice
            )
    }

    private fun showSettings() {

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            30,
            20,
            30,
            20
        )

        layout.setBackgroundColor(bg)

        val balanceInput =
            createInput(
                "الرصيد",
                balance.toString()
            )

        val riskInput =
            createInput(
                "المخاطرة %",
                riskPercent.toString()
            )

        val slInput =
            createInput(
                "Stop Loss",
                slDistance.toString()
            )

        val tpInput =
            createInput(
                "Take Profit",
                tpDistance.toString()
            )

        val tokenInput =
            createInput(
                "Telegram Bot Token",
                ""
            )

        val chatInput =
            createInput(
                "Telegram Chat ID",
                ""
            )

        layout.addView(
            makeText(
                "⚙️ إعدادات GoldBot",
                23f,
                white
            )
        )

        layout.addView(balanceInput)
        layout.addView(riskInput)
        layout.addView(slInput)
        layout.addView(tpInput)

        addSpace(layout, 10)

        layout.addView(
            makeText(
                "🔔 Telegram",
                18f,
                gold
            )
        )

        layout.addView(tokenInput)
        layout.addView(chatInput)

        addSpace(layout, 10)

        val save =
            Button(this)

        save.text =
            "💾 حفظ"

        save.setOnClickListener {

            balance =
                balanceInput.text
                    .toString()
                    .toDoubleOrNull()
                    ?: 1000.0

            riskPercent =
                riskInput.text
                    .toString()
                    .toDoubleOrNull()
                    ?: 1.0

            slDistance =
                slInput.text
                    .toString()
                    .toDoubleOrNull()
                    ?: 5.0

            tpDistance =
                tpInput.text
                    .toString()
                    .toDoubleOrNull()
                    ?: 10.0

            getPreferences(
                MODE_PRIVATE
            )
                .edit()
                .putString(
                    "telegram_token",
                    tokenInput.text.toString()
                )
                .putString(
                    "telegram_chat",
                    chatInput.text.toString()
                )
                .apply()

            updateAccount()

            toast(
                "تم حفظ الإعدادات"
            )
        }

        layout.addView(save)

        AlertDialog.Builder(this)
            .setView(layout)
            .setNegativeButton(
                "إغلاق",
                null
            )
            .show()
    }

    private fun createInput(
        hint: String,
        value: String
    ): EditText {

        val input =
            EditText(this)

        input.hint = hint
        input.setText(value)

        input.setTextColor(white)
        input.setHintTextColor(gray)

        input.inputType =
            InputType.TYPE_CLASS_TEXT

        return input
    }

    private fun updateAccount() {

        if (!::accountText.isInitialized) {
            return
        }

        val riskMoney =
            balance *
                riskPercent /
                100.0

        accountText.text =
            String.format(
                Locale.US,
                "الرصيد: $%.2f\nالمخاطرة: %.2f%%\nمبلغ المخاطرة: $%.2f",
                balance,
                riskPercent,
                riskMoney
            )
    }

    private fun sendTelegram(
        message: String
    ) {

        val prefs =
            getPreferences(
                MODE_PRIVATE
            )

        val token =
            prefs.getString(
                "telegram_token",
                ""
            ) ?: ""

        val chat =
            prefs.getString(
                "telegram_chat",
                ""
            ) ?: ""

        if (
            token.isBlank() ||
            chat.isBlank()
        ) {
            toast(
                "أدخل Telegram Token و Chat ID أولاً"
            )
            return
        }

        thread {

            var connection:
                HttpURLConnection? = null

            try {

                val url =
                    URL(
                        "https://api.telegram.org/bot" +
                            token +
                            "/sendMessage"
                    )

                connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "POST"

                connection.doOutput = true

                connection.connectTimeout =
                    10000

                connection.readTimeout =
                    10000

                val data =
                    "chat_id=" +
                        URLEncoder.encode(
                            chat,
                            "UTF-8"
                        ) +
                        "&text=" +
                        URLEncoder.encode(
                            message,
                            "UTF-8"
                        )

                connection.outputStream.use {
                    it.write(
                        data.toByteArray()
                    )
                }

                val code =
                    connection.responseCode

                runOnUiThread {

                    if (code == 200) {
                        toast(
                            "تم إرسال Telegram"
                        )
                    } else {
                        toast(
                            "Telegram HTTP $code"
                        )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {
                    toast(
                        "فشل اتصال Telegram"
                    )
                }

            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun createCard():
        LinearLayout {

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            20,
            18,
            20,
            18
        )

        layout.setBackgroundColor(card)

        return layout
    }

    private fun makeText(
        text: String,
        size: Float,
        color: Int
    ): TextView {

        val view =
            TextView(this)

        view.text = text
        view.textSize = size
        view.setTextColor(color)

        view.setPadding(
            0,
            5,
            0,
            5
        )

        return view
    }

    private fun buttonParams():
        LinearLayout.LayoutParams {

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.topMargin = 8

        return params
    }

    private fun addSpace(
        layout: LinearLayout,
        height: Int
    ) {

        val space =
            Space(this)

        layout.addView(
            space,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        )
    }

    private fun showError(
        message: String
    ) {

        priceText.text =
            "السعر غير متاح"

        priceText.setTextColor(red)

        logText.text =
            "❌ $message"
    }

    private fun toast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
