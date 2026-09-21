package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var priceText: TextView
    private lateinit var changeText: TextView
    private lateinit var updateText: TextView
    private lateinit var trendText: TextView
    private lateinit var signalText: TextView
    private lateinit var activityText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button

    private var botRunning = false
    private var currentPrice = 0.0
    private var previousPrice = 0.0

    private val prices = mutableListOf<Double>()

    private val bg = Color.rgb(12, 15, 22)
    private val card = Color.rgb(24, 29, 39)
    private val white = Color.WHITE
    private val gray = Color.rgb(170, 178, 190)
    private val green = Color.rgb(35, 190, 105)
    private val red = Color.rgb(225, 75, 75)
    private val gold = Color.rgb(230, 180, 70)

    private val handler = android.os.Handler(mainLooper)

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

        buildInterface()

        fetchGoldPrice()

        handler.postDelayed(updateTask, 15000)
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

        val title = TextView(this)
        title.text = "◉  GoldBot"
        title.textSize = 28f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(gold)

        root.addView(title)

        addSpace(root, 20)

        // الحالة
        val statusCard = createCard()

        statusCard.addView(
            makeText("● حالة الروبوت", 15f, gray)
        )

        statusText = makeText(
            "● متوقف",
            24f,
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

        addSpace(root, 14)

        // الذهب
        val marketCard = createCard()

        marketCard.addView(
            makeText("🥇 الذهب", 15f, gray)
        )

        marketCard.addView(
            makeText("XAU/USD", 24f, white)
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
            17f,
            gray
        )

        marketCard.addView(changeText)

        updateText = makeText(
            "آخر تحديث: --",
            14f,
            gray
        )

        marketCard.addView(updateText)

        marketCard.addView(
            makeText(
                "● بيانات الإنترنت",
                14f,
                green
            )
        )

        root.addView(marketCard)

        addSpace(root, 14)

        // التحليل
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
            "⏳ نحتاج عدة قراءات",
            18f,
            gold
        )

        analysisCard.addView(signalText)

        analysisCard.addView(
            makeText(
                "M1     M5     M15     H1     H4",
                15f,
                gray
            )
        )

        root.addView(analysisCard)

        addSpace(root, 14)

        // الحساب
        val accountCard = createCard()

        accountCard.addView(
            makeText("💰 الحساب", 15f, gray)
        )

        accountCard.addView(
            makeText(
                "الرصيد: --",
                22f,
                white
            )
        )

        accountCard.addView(
            makeText(
                "الربح اليومي: --",
                17f,
                gray
            )
        )

        root.addView(accountCard)

        addSpace(root, 14)

        // التحكم
        val actionsCard = createCard()

        actionsCard.addView(
            makeText("⚡ التحكم السريع", 15f, gray)
        )

        val refreshButton = Button(this)
        refreshButton.text = "🔄 تحديث سعر الذهب"
        refreshButton.setTextColor(white)
        refreshButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        refreshButton.setOnClickListener {
            fetchGoldPrice()
        }

        actionsCard.addView(
            refreshButton,
            buttonParams()
        )

        val analyzeButton = Button(this)
        analyzeButton.text = "🔍 تحليل XAU/USD"
        analyzeButton.setTextColor(white)
        analyzeButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        actionsCard.addView(
            analyzeButton,
            buttonParams()
        )

        val mt5Button = Button(this)
        mt5Button.text = "⚙️ إعدادات MT5"
        mt5Button.setTextColor(white)
        mt5Button.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        mt5Button.setOnClickListener {
            showMt5Settings()
        }

        actionsCard.addView(
            mt5Button,
            buttonParams()
        )

        val telegramButton = Button(this)
        telegramButton.text = "🔔 Telegram"
        telegramButton.setTextColor(white)
        telegramButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        telegramButton.setOnClickListener {
            toast("Telegram سيتم ربطه لاحقًا")
        }

        actionsCard.addView(
            telegramButton,
            buttonParams()
        )

        root.addView(actionsCard)

        addSpace(root, 14)

        // سجل
        val logCard = createCard()

        logCard.addView(
            makeText("📜 سجل الروبوت", 15f, gray)
        )

        activityText = makeText(
            "جاري الاتصال...",
            15f,
            gray
        )

        logCard.addView(activityText)

        root.addView(logCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot • Market Monitor",
            12f,
            gray
        )

        footer.gravity = Gravity.CENTER

        root.addView(footer)

        scroll.addView(root)

        setContentView(scroll)
    }

    // ==============================
    // السعر الحقيقي
    // ==============================

    private fun fetchGoldPrice() {

        priceText.text = "جاري التحديث..."

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

        if (prices.size > 100) {
            prices.removeAt(0)
        }

        priceText.text =
            String.format(
                Locale.US,
                "$%,.2f",
                price
            )

        updateText.text =
            "آخر تحديث: الآن"

        if (previousPrice > 0) {

            val difference =
                currentPrice - previousPrice

            val percent =
                difference /
                        previousPrice *
                        100.0

            val sign =
                if (difference >= 0) "+"
                else ""

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

        activityText.text =
            "✓ تم تحديث XAU/USD\n" +
            "السعر: " +
            String.format(
                Locale.US,
                "$%,.2f",
                currentPrice
            ) +
            "\nالقراءات: " +
            prices.size

        calculateTrend()
    }

    // ==============================
    // الاتجاه
    // ==============================

    private fun calculateTrend() {

        if (prices.size < 3) {

            trendText.text =
                "الاتجاه: نحتاج قراءات أكثر"

            trendText.setTextColor(gold)

            signalText.text =
                "⏳ في انتظار البيانات"

            signalText.setTextColor(gold)

            return
        }

        val first =
            prices[prices.size - 3]

        val last =
            prices[prices.size - 1]

        val difference =
            last - first

        if (difference > 0) {

            trendText.text =
                "🟢 الاتجاه: صاعد"

            trendText.setTextColor(green)

            signalText.text =
                "📈 حركة صاعدة"

            signalText.setTextColor(green)

        } else if (difference < 0) {

            trendText.text =
                "🔴 الاتجاه: هابط"

            trendText.setTextColor(red)

            signalText.text =
                "📉 حركة هابطة"

            signalText.setTextColor(red)

        } else {

            trendText.text =
                "➖ الاتجاه: محايد"

            trendText.setTextColor(gold)

            signalText.text =
                "🟡 لا يوجد اتجاه واضح"

            signalText.setTextColor(gold)
        }
    }

    // ==============================
    // التحليل
    // ==============================

    private fun analyzeMarket() {

    if (prices.size < 3) {
        signalText.text = "⏳ نحتاج 3 قراءات على الأقل"
        signalText.setTextColor(gold)
        fetchGoldPrice()
        return
    }

    calculateTrend()

    activityText.text =
        activityText.text.toString() +
        "\n✓ تم تحليل حركة السعر"
}

        if (prices.size < 3) {

            signalText.text =
                "⏳ نحتاج 3 قراءات على الأقل"

            signalText.setTextColor(gold)

            fetchGoldPrice()

            return
        }

        calculateTrend()

        activityText.text =
    activityText.text.toString() +
    "  "\n✓ تم تحليل حركة السعر"
    }

    // ==============================
    // تشغيل الروبوت
    // ==============================

    private fun toggleBot() {

        botRunning = !botRunning

        if (botRunning) {

            statusText.text =
                "● يعمل الآن"

            statusText.setTextColor(green)

            startButton.text =
                "■ إيقاف الروبوت"

            startButton.setBackgroundColor(red)

            activityText.text =
                "✓ GoldBot يعمل\n" +
                "⏳ مراقبة XAU/USD..."

        } else {

            statusText.text =
                "● متوقف"

            statusText.setTextColor(red)

            startButton.text =
                "▶ تشغيل الروبوت"

            startButton.setBackgroundColor(green)

            activityText.text =
                "الروبوت متوقف"
        }
    }

    // ==============================
    // MT5
    // ==============================

    private fun showMt5Settings() {

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

        layout.addView(
            makeText(
                "⚙️ إعدادات MT5",
                25f,
                white
            )
        )

        addSpace(layout, 15)

        val server = EditText(this)
        server.hint = "MT5 Server"
        server.setTextColor(white)
        server.setHintTextColor(gray)

        layout.addView(server)

        val login = EditText(this)
        login.hint = "MT5 Login"
        login.inputType = 2
        login.setTextColor(white)
        login.setHintTextColor(gray)

        layout.addView(login)

        val password = EditText(this)
        password.hint = "MT5 Password"
        password.inputType = 129
        password.setTextColor(white)
        password.setHintTextColor(gray)

        layout.addView(password)

        addSpace(layout, 15)

        val test = Button(this)
        test.text = "🔌 اختبار الاتصال"
        test.setTextColor(white)
        test.setBackgroundColor(green)

        test.setOnClickListener {

            toast(
                "MT5 الحقيقي يحتاج Desktop أو VPS"
            )
        }

        layout.addView(test)

        val close = Button(this)
        close.text = "رجوع"
        close.setTextColor(white)

        layout.addView(close)

        val dialog =
            android.app.Dialog(this)

        dialog.setContentView(layout)

        dialog.show()

        close.setOnClickListener {
            dialog.dismiss()
        }
    }

    // ==============================
    // أدوات
    // ==============================

    private fun createCard(): LinearLayout {

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

        val space = Space(this)

        layout.addView(
            space,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        )
    }

    private fun showError(message: String) {

        priceText.text =
            "السعر غير متاح"

        priceText.setTextColor(red)

        activityText.text =
            "❌ $message"
    }

    private fun toast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
