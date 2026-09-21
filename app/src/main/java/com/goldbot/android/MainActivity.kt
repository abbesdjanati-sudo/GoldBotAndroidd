package com.goldbot.android

import android.app.Activity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(30, 30, 30, 30)
        root.setBackgroundColor(Color.rgb(15, 18, 25))

        val title = TextView(this)

        title.text = "GOLD BOT"
        title.textSize = 30f
        title.setTextColor(Color.YELLOW)
        title.gravity = Gravity.CENTER

        root.addView(title)

        priceText = TextView(this)

        priceText.text = "XAU/USD\n--"
        priceText.textSize = 28f
        priceText.setTextColor(Color.YELLOW)
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(20, 30, 20, 30)

        root.addView(priceText)

        statusText = TextView(this)

        statusText.text = "جاري الاتصال..."
        statusText.textSize = 18f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(20, 20, 20, 20)

        root.addView(statusText)

        val button = Button(this)

        button.text = "تحديث السعر"

        button.setOnClickListener {
            getGoldPrice()
        }

        root.addView(button)

        setContentView(root)

        getGoldPrice()
    }

    private fun getGoldPrice() {

        statusText.text = "جاري جلب سعر الذهب..."

        Thread {

            var connection: HttpURLConnection? = null

            try {

                val url =
                    URL("https://api.gold-api.com/price/XAU")

                connection =
                    url.openConnection()
                            as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val code =
                    connection.responseCode

                if (code != 200) {

                    runOnUiThread {

                        statusText.text =
                            "خطأ في الاتصال: HTTP $code"
                    }

                    return@Thread
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
                    json.getDouble("price")

                runOnUiThread {

                    priceText.text =
                        String.format(
                            Locale.US,
                            "XAU/USD\n%.2f $",
                            price
                        )

                    statusText.text =
                        "متصل ✓\nالسعر الحقيقي للذهب"
                }

            } catch (e: Exception) {

                runOnUiThread {

                    statusText.text =
                        "فشل الاتصال\n" +
                        (e.message ?: "خطأ غير معروف")
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }
}
