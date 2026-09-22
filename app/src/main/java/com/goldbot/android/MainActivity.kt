package com.example.goldbot

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var priceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var riskText: TextView
    private lateinit var slText: TextView
    private lateinit var tpText: TextView
    private lateinit var logText: TextView

    private var running = false
    private var balance = 3000.0
    private var riskPercent = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            createInterface()
        } catch (e: Exception) {
            val error = TextView(this)
            error.text = "Gold Bot\n\nخطأ في تشغيل الواجهة:\n${e.message}"
            error.textSize = 18f
            error.setPadding(30, 50, 30, 30)
            setContentView(error)
        }
    }

    private fun createInterface() {

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        scroll.addView(root)

        val title = TextView(this).apply {
            text = "🥇 GOLD BOT"
            textSize = 30f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        root.addView(title, params())

        val subtitle = TextView(this).apply {
            text = "XAUUSD • Trading Assistant"
            textSize = 17f
            gravity = Gravity.CENTER
        }

        root.addView(subtitle, params())

        addSeparator(root)

        priceText = addInfo(root, "السعر", "غير متصل")
        balanceText = addInfo(root, "الرصيد", "$3,000.00")
        riskText = addInfo(root, "المخاطرة", "1%")

        addSeparator(root)

        addSection(root, "⚙️ استراتيجية التداول")

        addInfo(root, "EMA سريع", "20")
        addInfo(root, "EMA بطيء", "50")
        addInfo(root, "RSI", "14")
        addInfo(root, "ATR", "14")
        addInfo(root, "Stop Loss", "1.5 × ATR")
        addInfo(root, "Take Profit", "2.5 × ATR")

        addSeparator(root)

        addSection(root, "📊 الإشارة الحالية")

        signalText = TextView(this).apply {
            text = "انتظار البيانات..."
            textSize = 21f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(10, 25, 10, 25)
        }

        root.addView(signalText, params())

        slText = addInfo(root, "SL", "--")
        tpText = addInfo(root, "TP", "--")

        addSeparator(root)

        addSection(root, "🤖 التحكم")

        statusText = TextView(this).apply {
            text = "الحالة: متوقف"
            textSize = 19f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        root.addView(statusText, params())

        val startButton = Button(this).apply {
            text = "▶ تشغيل الروبوت"
            textSize = 18f
        }

        root.addView(startButton, params())

        val stopButton = Button(this).apply {
            text = "■ إيقاف الروبوت"
            textSize = 18f
        }

        root.addView(stopButton, params())

        val analyzeButton = Button(this).apply {
            text = "🔎 تحليل السوق"
            textSize = 18f
        }

        root.addView(analyzeButton, params())

        addSeparator(root)

        addSection(root, "💰 إدارة المخاطر")

        val riskInput = EditText(this).apply {
            hint = "نسبة المخاطرة %"
            setText("1.0")
            inputType = 2
        }

        root.addView(riskInput, params())

        val applyRiskButton = Button(this).apply {
            text = "تطبيق المخاطرة"
        }

        root.addView(applyRiskButton, params())

        addSeparator(root)

        addSection(root, "📜 سجل الروبوت")

        logText = TextView(this).apply {
            text = "لا توجد عمليات حتى الآن."
            textSize = 15f
            setPadding(10, 10, 10, 20)
        }

        root.addView(logText, params())

        addSeparator(root)

        val warning = TextView(this).apply {
            text = """
                ⚠️ وضع Demo / Paper Trading

                هذه النسخة لا ترسل أوامر حقيقية إلى وسيط.
                لا تستخدمها بأموال حقيقية قبل إضافة اتصال API
                رسمي وآمن للوسيط واختبار النظام.
            """.trimIndent()

            textSize = 14f
            setPadding(10, 20, 10, 20)
        }

        root.addView(warning, params())

        startButton.setOnClickListener {
            running = true
            statusText.text = "الحالة: يعمل ✓"
            addLog("تم تشغيل الروبوت")
            analyzeMarket()
        }

        stopButton.setOnClickListener {
            running = false
            statusText.text = "الحالة: متوقف"
            addLog("تم إيقاف الروبوت")
        }

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        applyRiskButton.setOnClickListener {

            val value = riskInput.text.toString().toDoubleOrNull()

            if (value != null && value > 0.0 && value <= 5.0) {

                riskPercent = value

                riskText.text = String.format(
                    Locale.US,
                    "%.2f%%",
                    riskPercent
                )

                addLog(
                    "تم تغيير المخاطرة إلى %.2f%%".format(
                        Locale.US,
                        riskPercent
                    )
                )

            } else {

                Toast.makeText(
                    this,
                    "أدخل قيمة بين 0.1% و 5%",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setContentView(scroll)
    }

    private fun analyzeMarket() {

        // بيانات تجريبية فقط.
        // لاحقًا نستبدلها ببيانات XAUUSD حقيقية من API.

        val price = 2650.00
        val ema20 = 2648.50
        val ema50 = 2645.00
        val rsi = 58.0
        val atr = 4.0

        priceText.text = String.format(
            Locale.US,
            "$%.2f",
            price
        )

        val signal = when {

            ema20 > ema50 && rsi >= 50.0 && rsi < 70.0 ->
                "🟢 BUY"

            ema20 < ema50 && rsi <= 50.0 && rsi > 30.0 ->
                "🔴 SELL"

            else ->
                "🟡 WAIT"
        }

        signalText.text = signal

        val sl: Double
        val tp: Double

        if (signal.contains("BUY")) {

            sl = price - (1.5 * atr)
            tp = price + (2.5 * atr)

        } else if (signal.contains("SELL")) {

            sl = price + (1.5 * atr)
            tp = price - (2.5 * atr)

        } else {

            sl = 0.0
            tp = 0.0
        }

        slText.text = if (sl > 0) {
            String.format(Locale.US, "$%.2f", sl)
        } else {
            "--"
        }

        tpText.text = if (tp > 0) {
            String.format(Locale.US, "$%.2f", tp)
        } else {
            "--"
        }

        addLog(
            "تحليل: $signal | EMA20 %.2f | EMA50 %.2f | RSI %.1f".format(
                Locale
