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

class MainActivity : Activity() {

    private lateinit var priceText: TextView
    private lateinit var statusText: TextView
    private lateinit var historyText: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val history =
        ArrayList<Double>()

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

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.gravity =
            Gravity.CENTER_HORIZONTAL

        root.setPadding(
            30,
            30,
            30,
            30
        )

        root.setBackgroundColor(
            Color.rgb(15, 18, 25)
        )

        val title =
            TextView(this)

        title.text =
            "GOLD BOT"

        title.textSize =
            30f

        title.setTextColor(
            Color.YELLOW
        )

        title.gravity =
            Gravity.CENTER

        root.addView(title)

        val subtitle =
            TextView(this)

        subtitle.text =
            "XAU/USD"

        subtitle.textSize =
            18f

        subtitle.setTextColor(
            Color.LTGRAY
        )

        subtitle.gravity =
            Gravity.CENTER

        root.addView(subtitle)

        priceText =
            TextView(this)

        priceText.text =
            "XAU/USD\n--"

        priceText.textSize =
            30f

        priceText.setTextColor(
            Color.YELLOW
        )

        priceText.gravity =
            Gravity.CENTER

        priceText.setPadding(
            20,
            35,
            20,
            35
        )

        root.addView(priceText)

        statusText =
            TextView(this)

        statusText.text =
            "جاري الاتصال..."

        statusText.textSize =
            18f

        statusText.setTextColor(
            Color.WHITE
        )

        statusText.gravity =
            Gravity.CENTER

        statusText.setPadding(
            20,
            20,
            20,
            20
        )

        root.addView(statusText)

        val refresh =
            Button(this)

        refresh.text =
            "تحديث السعر الآن"

        refresh.setOnClickListener {

            getGoldPrice()
        }

        root.addView(refresh)

        val historyTitle =
            TextView(this)

        historyTitle.text =
            "آخر الأسعار"

        historyTitle.textSize =
            22f

        historyTitle.setTextColor(
            Color.YELLOW
        )

        historyTitle.gravity =
            Gravity.CENTER

        historyTitle.setPadding(
            10,
            30,
            10,
            10
        )

        root.addView(historyTitle)

        historyText =
            TextView(this)

        historyText.text =
            "لا توجد بيانات بعد"

        historyText.textSize =
            16f

        historyText.setTextColor(
            Color.WHITE
        )

        historyText.gravity =
            Gravity.CENTER

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

        history.add(price)

        if (history.size > 20) {

            history.removeAt(0)
        }

        priceText.text =
            String.format(
                Locale.US,
                "XAU/USD\n%.2f $",
                price
            )

        statusText.text =
            "متصل ✓\n" +
            "تحديث تلقائي كل 15 ثانية"

        updateHistory()
    }

    private fun updateHistory() {

        val text =
            StringBuilder()

        for (
            i in history.indices.reversed()
        ) {

            text.append(
                String.format(
                    Locale.US,
                    "%.2f $",
                    history[i]
                )
            )

            text.append("\n")
        }

        historyText.text =
            text.toString()
    }
}
