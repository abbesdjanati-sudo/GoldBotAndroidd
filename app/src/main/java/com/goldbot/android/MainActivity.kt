package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
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

            handler.postDelayed(
                this,
                15000
            )
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        buildInterface()

        getGoldPrice()

        handler.postDelayed(
            timer,
            15000
        )
    }

    override fun onDestroy() {

        handler.removeCallbacks(timer)

        super.onDestroy()
    }

    private fun buildInterface() {

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.setPadding(
            25,
            25,
            25,
            40
        )

        root.setBackgroundColor(
            Color.rgb(15, 18, 25)
        )

        val title = TextView(this)

        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(Color.YELLOW)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val subtitle = TextView(this)

        subtitle.text = "XAU/USD"
        subtitle.textSize = 18f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        root.addView(subtitle)

        priceText = TextView(this)

        priceText.text =
            "XAU/USD\n--"

        priceText.textSize = 30f
        priceText.setTextColor(Color.YELLOW)
        priceText.gravity = Gravity.CENTER

        priceText.setPadding(
            20,
            30,
            20,
            30
        )

        root.addView(priceText)

        statusText = TextView(this)

        statusText.text =
            "جاري الاتصال..."

        statusText.textSize = 18f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER

        statusText.setPadding(
            20,
            15,
            20,
            15
        )

        root.addView(statusText)

        val refreshButton =
            Button(this)

        refreshButton.text =
            "تحديث السعر الآن"

        refreshButton.setOnClickListener {
            getGoldPrice()
        }

        root.addView(refreshButton)

        val signalTitle =
            TextView(this)

        signalTitle.text =
            "إشارة السوق"

        signalTitle.textSize = 22f
        signalTitle.setTextColor(Color.YELLOW)
        signalTitle.gravity = Gravity.CENTER

        signalTitle.setPadding(
            10,
            25,
            10,
            10
        )

        root.addView(signalTitle)

        signalText =
            TextView(this)

        signalText.text =
            "WAIT"

        signalText.textSize = 30f
        signalText.setTextColor(Color.rgb(255, 170, 0))
        signalText.gravity = Gravity.CENTER

        signalText.setPadding(
            20,
            20,
            20,
            20
        )

        root.addView(signalText)

        val analysisTitle =
            TextView(this)

        analysisTitle.text =
            "التحليل الفني"

        analysisTitle.textSize = 22f
        analysisTitle.setTextColor(Color.YELLOW)
        analysisTitle.gravity = Gravity.CENTER

        analysisTitle.setPadding(
            10,
            25,
            10,
            10
        )

        root.addView(analysisTitle)

        analysisText =
            TextView(this)

        analysisText.text =
            "EMA 9: --\n" +
            "EMA 21: --\n" +
            "RSI 14: --\n" +
            "MACD: --\n" +
            "Trend: --"

        analysisText.textSize = 17f
        analysisText.setTextColor(Color.WHITE)
        analysisText.gravity = Gravity.CENTER

        analysisText.setPadding(
            20,
            20,
            20,
            20
        )

        root.addView(analysisText)

        val historyTitle =
            TextView(this)

        historyTitle.text =
            "آخر الأسعار"

        historyTitle.textSize = 22f
        historyTitle.setTextColor(Color.YELLOW)
        historyTitle.gravity = Gravity.CENTER

        historyTitle.setPadding(
            10,
            25,
            10,
            10
        )

        root.addView(historyTitle)

        historyText =
            TextView(this)

        historyText.text =
            "لا توجد بيانات بعد"

        historyText.textSize = 16f
        historyText.setTextColor(Color.WHITE)
        historyText.gravity = Gravity.CENTER

        root.addView(historyText)

        setContentView(root)
    }

    private fun getGoldPrice() {

        runOnUiThread {

            statusText.text =
                "جاري جلب السعر..."
        }

        Thread {

            var connection:
                HttpURLConnection? = null

            try {

                val url =
                    URL(
                        "https://api.gold-api.com/price/XAU"
                    )

                connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    10000

                connection.readTimeout =
                    10000

                val code =
                    connection.responseCode

                if (code != 200) {

                    runOnUiThread {

                        statusText.text =
                            "خطأ HTTP: $code"
                    }

                    return@Thread
                }

                val response =
                    connection
                        .inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val json =
                    JSONObject(response)

                val price =
                    json.getDouble("price")

                runOnUiThread {

                    updatePrice(price)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    statusText.text =
                        "فشل الاتصال\n" +
                        (
                            e.message
                                ?: "خطأ غير معروف"
                            )
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }

    private fun updatePrice(
        price: Double
    ) {

        if (price <= 0.0) {
            return
        }

        prices.add(price)

        if (prices.size > 100) {

            prices.removeAt(0)
        }

        priceText.text =
            String.format(
                Locale.US,
                "XAU/USD\n%.2f $",
                price
            )

        statusText.text =
            "متصل ✓\n" +
            "تحديث كل 15 ثانية"

        updateHistory()

        calculateIndicators()
    }

    private fun updateHistory() {

        if (prices.isEmpty()) {
            return
        }

        val result =
            StringBuilder()

        for (
            i in prices.indices.reversed()
        ) {

            result.append(
                String.format(
                    Locale.US,
                    "%.2f $",
                    prices[i]
                )
            )

            result.append("\n")

            if (prices.size - i >= 20) {
                break
            }
        }

        historyText.text =
            result.toString()
    }

    private fun calculateIndicators() {

        if (prices.size < 26) {

            signalText.text =
                "WAIT"

            signalText.setTextColor(
                Color.rgb(255, 170, 0)
            )

            analysisText.text =
                "EMA 9: --\n" +
                "EMA 21: --\n" +
                "RSI 14: --\n" +
                "MACD: --\n" +
                "Trend: جمع البيانات...\n" +
                "عدد الأسعار: ${prices.size}"

            return
        }

        val ema9 =
            calculateEma(
                prices,
                9
            )

        val ema21 =
            calculateEma(
                prices,
                21
            )

        val rsi =
            calculateRsi(
                prices,
                14
            )

        val macd =
            calculateMacd(prices)

        val trend =
            if (ema9 > ema21) {
                "صاعد"
            } else if (ema9 < ema21) {
                "هابط"
            } else {
                "محايد"
            }

        val signal =
            calculateSignal(
                ema9,
                ema21,
                rsi,
                macd
            )

        analysisText.text =
            String.format(
                Locale.US,
                "EMA 9: %.2f\n" +
                "EMA 21: %.2f\n" +
                "RSI 14: %.2f\n" +
                "MACD: %.4f\n" +
                "Trend: %s\n" +
                "عدد الأسعار: %d",
                ema9,
                ema21,
                rsi,
                macd,
                trend,
                prices.size
            )

        signalText.text =
            signal

        if (signal == "BUY") {

            signalText.setTextColor(
                Color.rgb(0, 220, 120)
            )

        } else if (signal == "SELL") {

            signalText.setTextColor(
                Color.rgb(255, 80, 80)
            )

        } else {

            signalText.setTextColor(
                Color.rgb(255, 170, 0)
            )
        }
    }

    private fun calculateEma(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.isEmpty()) {
            return 0.0
        }

        val count =
            min(
                period,
                data.size
            )

        var ema = 0.0

        for (i in 0 until count) {
            ema += data[i]
        }

        ema /= count.toDouble()

        val multiplier =
            2.0 /
            (period + 1.0)

        for (
            i in count until data.size
        ) {

            ema =
                (
                    (data[i] - ema) *
                    multiplier
                ) + ema
        }

        return ema
    }

    private fun calculateRsi(
        data: List<Double>,
        period: Int
    ): Double {

        if (data.size <= period) {
            return 50.0
        }

        var gains = 0.0
        var losses = 0.0

        val start =
            data.size - period

        for (
            i in start until data.size
        ) {

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
            gains / period

        val averageLoss =
            losses / period

        val rs =
            averageGain / averageLoss

        return 100.0 -
            (
                100.0 /
                (1.0 + rs)
            )
    }

    private fun calculateMacd(
        data: List<Double>
    ): Double {

        if (data.size < 26) {
            return 0.0
        }

        val ema12 =
            calculateEma(
                data,
                12
            )

        val ema26 =
            calculateEma(
                data,
                26
            )

        return ema12 - ema26
    }

    private fun calculateSignal(
        ema9: Double,
        ema21: Double,
        rsi: Double,
        macd: Double
    ): String {

        var buy = 0
        var sell = 0

        if (ema9 > ema21) {

            buy++

        } else if (ema9 < ema21) {

            sell++
        }

        if (
            rsi > 50.0 &&
            rsi < 70.0
        ) {

            buy++

        } else if (
            rsi < 50.0 &&
            rsi > 30.0
        ) {

            sell++
        }

        if (macd > 0.0) {

            buy++

        } else if (macd < 0.0) {

            sell++
        }

        return when {

            buy >= 3 -> "BUY"

            sell >= 3 -> "SELL"

            else -> "WAIT"
        }
    }
}
