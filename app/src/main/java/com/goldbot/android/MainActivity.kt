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
    private lateinit var price: TextView
    private lateinit var status: TextView
    private lateinit var signalView: TextView
    private lateinit var analysis: TextView
    private lateinit var trade: TextView
    private lateinit var account: TextView
    private lateinit var log: TextView
    private lateinit var start: Button
    private lateinit var paper: Button

    private val h = Handler(Looper.getMainLooper())
    private val prices = ArrayList<Double>()
    private var running = false
    private var paperOn = false
    private var current = 0.0
    private var signal = "WAIT"
    private var balance = 10000.0
    private var risk = 1.0
    private var position = ""
    private var entry = 0.0
    private var sl = 0.0
    private var tp = 0.0
    private var size = 0.0
    private var tgToken = ""
    private var tgChat = ""
    private var mt5Server = ""
    private var mt5Login = ""
    private var mt5Password = ""

    private val bg = Color.rgb(15,18,25)
    private val card = Color.rgb(27,31,42)
    private val gold = Color.rgb(255,215,0)
    private val green = Color.rgb(0,220,120)
    private val red = Color.rgb(255,80,80)
    private val orange = Color.rgb(255,170,0)

    private val timer = object : Runnable {
        override fun run() {
            if (running) getPrice()
            h.postDelayed(this, 15000L)
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        load()
        ui()
        h.postDelayed(timer, 1000L)
    }

    override fun onDestroy() {
        h.removeCallbacks(timer)
        super.onDestroy()
    }

    private fun ui() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24,24,24,40)
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

        val sub = TextView(this).apply {
            text = "XAU/USD Trading Assistant"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(sub)
        root.addView(space(10))

        status = text("البوت متوقف",19f,red)
        status.gravity = Gravity.CENTER
        status.setPadding(15,15,15,15)
        status.setBackgroundColor(card)
        root.addView(status)

        val source = text("المصدر: غير متصل",14f,Color.LTGRAY)
        source.gravity = Gravity.CENTER
        root.addView(source)

        price = text("XAU/USD\n--",28f,gold)
        price.gravity = Gravity.CENTER
        price.setPadding(15,25,15,25)
        price.setBackgroundColor(card)
        root.addView(price)

        start = Button(this).apply {
            text = "تشغيل البوت"
            setOnClickListener { toggle() }
        }
        root.addView(start)

        val refresh = Button(this).apply {
            text = "تحديث السعر الآن"
            setOnClickListener { getPrice() }
        }
        root.addView(refresh)

        paper = Button(this).apply {
            text = "Paper Trading: OFF"
            setOnClickListener {
                paperOn = !paperOn
                text = if (paperOn) "Paper Trading: ON"
                else "Paper Trading: OFF"
                addLog(
                    if (paperOn) "تم تشغيل التداول التجريبي"
                    else "تم إيقاف التداول التجريبي"
                )
            }
        }
        root.addView(paper)

        root.addView(section("إشارة السوق"))

        signalView = text("WAIT",30f,orange)
        signalView.gravity = Gravity.CENTER
        signalView.setPadding(15,25,15,25)
        signalView.setBackgroundColor(card)
        root.addView(signalView)

        root.addView(section("التحليل الفني"))

        analysis = text(
            "EMA 9: --\n" +
            "EMA 21: --\n" +
            "RSI 14: --\n" +
            "MACD: --\n" +
            "Trend: --\n" +
            "Prices: 0",
            16f,
            Color.WHITE
        )
        analysis.setPadding(20,20,20,20)
        analysis.setBackgroundColor(card)
        root.addView(analysis)

        root.addView(section("إدارة الصفقة"))

        trade = text(
            "Entry: --\n" +
            "Stop Loss: --\n" +
            "Take Profit: --\n" +
            "Position Size: --\n" +
            "Risk: 1%",
            16f,
            Color.WHITE
        )
        trade.setPadding(20,20,20,20)
        trade.setBackgroundColor(card)
        root.addView(trade)

        root.addView(section("الحساب التجريبي"))

        account = text(
            "الرصيد: 10000.00 $\n" +
            "الصفقة: لا توجد\n" +
            "P/L العائم: 0.00 $",
            16f,
            Color.LTGRAY
        )
        account.setPadding(20,20,20,20)
        account.setBackgroundColor(card)
        root.addView(account)

        button(root,"إغلاق الصفقة التجريبية") {
            closeTrade()
        }

        button(root,"إعدادات المخاطرة") {
            riskDialog()
        }

        root.addView(section("الاتصالات"))

        button(root,"إعدادات Telegram") {
            telegramDialog()
        }

        button(root,"إعدادات MT5") {
            mt5Dialog()
        }

        button(root,"إعادة الحساب التجريبي") {
            reset()
        }

        root.addView(section("السجل"))

        log = text(
            "GoldBot جاهز...",
            14f,
            Color.LTGRAY
        )
        log.setPadding(15,15,15,15)
        log.setBackgroundColor(card)
        root.addView(log)

        setContentView(scroll)
    }

    private fun text(
        s: String,
        size: Float,
        color: Int
    ): TextView =
        TextView(this).apply {
            text = s
            textSize = size
            setTextColor(color)
        }

    private fun section(s: String): TextView =
        TextView(this).apply {
            text = s
            textSize = 20f
            setTextColor(gold)
            setPadding(5,14,5,8)
        }

    private fun space(n: Int): View =
        Space(this).apply {
            layoutParams =
                LinearLayout.LayoutParams(-1,n)
        }

    private fun button(
        root: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        root.addView(
           
