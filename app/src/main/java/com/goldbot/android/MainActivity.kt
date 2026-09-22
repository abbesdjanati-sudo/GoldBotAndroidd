package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread
class MainActivity : Activity() {
  
    private val priceHandler = Handler(Looper.getMainLooper())

private val priceRunnable = object : Runnable {
    override fun run() {
        getRealGoldPrice()
        priceHandler.postDelayed(this, 10000)
    }
}

    private lateinit var status: TextView
    private lateinit var signal: TextView
    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(25, 25, 25, 25)

        val title = TextView(this)
        title.text = "🥇 GOLD BOT"
        title.textSize = 30f
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.BLACK)
        root.addView(title)

        val market = TextView(this)
        market.text = """
            XAUUSD

            EMA 20 / EMA 50
            RSI 14
            ATR 14

            Risk: 1%
            SL: 1.5 ATR
            TP: 2.5 ATR

            الوضع: Demo / Paper Trading
        """.trimIndent()
        market.textSize = 18f
        market.setPadding(0, 30, 0, 30)
        root.addView(market)

        signal = TextView(this)
        signal.text = "الإشارة: WAIT"
        signal.textSize = 22f
        signal.gravity = Gravity.CENTER
        root.addView(signal)

        status = TextView(this)
        status.text = "الحالة: متوقف"
        status.textSize = 20f
        status.gravity = Gravity.CENTER
        status.setPadding(0, 25, 0, 25)
        root.addView(status)

        val start = Button(this)
        start.text = "▶ تشغيل الروبوت"
        root.addView(start)

        val stop = Button(this)
        stop.text = "■ إيقاف الروبوت"
        root.addView(stop)

        val analyze = Button(this)
        analyze.text = "🔎 تحليل السوق"
        root.addView(analyze)

        log = TextView(this)
        log.text = "\n📜 سجل الروبوت:\nلا توجد عمليات."
        log.textSize = 16f
        log.setPadding(0, 25, 0, 25)
        root.addView(log)

        val warning = TextView(this)
        warning.text = """
            
            ⚠️ تنبيه

            هذه النسخة تجريبية.
            لا ترسل صفقات حقيقية إلى الوسيط.
        """.trimIndent()
        warning.textSize = 15f
        root.addView(warning)

        start.setOnClickListener {
            status.text = "الحالة: يعمل ✓"
            signal.text = "الإشارة: WAIT"
            addLog("تم تشغيل الروبوت")
        }

        stop.setOnClickListener {
            status.text = "الحالة: متوقف"
            addLog("تم إيقاف الروبوت")
        }

        analyze.setOnClickListener {
            signal.text = "الإشارة: BUY"
            addLog("تم تحليل XAUUSD")
        }

        scroll.addView(root)

        setContentView(scroll)
    scroll.addView(root)

setContentView(scroll)

priceHandler.post(priceRunnable)
}
    

    private fun addLog(message: String) {
        log.text = "📜 سجل الروبوت:\n• $message"
    }
}
