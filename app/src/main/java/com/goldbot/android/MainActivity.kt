package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var priceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var activityText: TextView
    private lateinit var trendText: TextView
    private lateinit var changeText: TextView
    private lateinit var updateText: TextView
    private lateinit var startButton: Button

    private var botRunning = false
    private var currentPrice = 0.0
    private var previousPrice = 0.0

    private val priceHistory = mutableListOf<Double>()
    private val timeHistory = mutableListOf<Long>()

    private val bg = Color.rgb(12, 15, 22)
    private val card = Color.rgb(24, 29, 39)
    private val white = Color.WHITE
    private val gray = Color.rgb(170, 178, 190)
    private val green = Color.rgb(35, 190, 105)
    private val red = Color.rgb(225, 75, 75)
    private val gold = Color.rgb(230, 180, 70)

    private val handler = android.os.Handler(mainLooper)

    private val updateRunnable = object : Runnable {
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

        handler.postDelayed(
            updateRunnable,
            15000
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    // =====================================================
    // INTERFACE
    // =====================================================

    private fun buildInterface() {

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(22, 20, 22, 30)

        // HEADER
        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        val logo = TextView(this)
        logo.text = "◉"
        logo.textSize = 34f
        logo.setTextColor(gold)

        val title = TextView(this)
        title.text = "  GoldBot"
        title.textSize = 28f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(white)

        header.addView(logo)
        header.addView(title)

        root.addView(header)

        addSpace(root, 18)

        // BOT STATUS
        val statusCard = createCard()

        statusCard.addView(
            makeText("●  حالة الروبوت", 15f, gray)
        )

        statusText = makeText(
            "●  متوقف",
            25f,
            red
        )

        statusText.setTypeface(
            null,
            Typeface.BOLD
        )

        statusCard.addView(statusText)

        startButton = Button(this)
        startButton.text = "▶  تشغيل الروبوت"
        startButton.textSize = 16f
        startButton.setTextColor(white)
        startButton.setBackgroundColor(green)

        startButton.setOnClickListener {
            toggleBot()
        }

        statusCard.addView(
            startButton,
            matchParams(14)
        )

        root.addView(statusCard)

        addSpace(root, 14)

        // MARKET
        val marketCard = createCard()

        marketCard.addView(
            makeText("🥇  الذهب", 15f, gray)
        )

        val pair = makeText(
            "XAU/USD",
            24f,
            white
        )

        pair.setTypeface(
            null,
            Typeface.BOLD
        )

        marketCard.addView(pair)

        priceText = makeText(
            "جاري جلب السعر...",
            30f,
            gold
        )

        priceText.setTypeface(
            null,
            Typeface.BOLD
        )

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
                "●  بيانات الإنترنت",
                14f,
                green
            )
        )

        root.addView(marketCard)

        addSpace(root, 14)

        // ANALYSIS
        val analysisCard = createCard()

        analysisCard.addView(
            makeText(
                "📊  تحليل السوق",
                15f,
                gray
            )
        )

        trendText = makeText(
            "الاتجاه: في انتظار البيانات",
            23f,
            gold
        )

        trendText.setTypeface(
            null,
            Typeface.BOLD
        )

        trendText.setPadding(
            0,
            18,
            0,
            10
        )

        analysisCard.addView(trendText)

        signalText = makeText(
            "⏳  نحتاج عدة قراءات للتحليل",
            18f,
            gold
        )

        signalText.setTypeface(
            null,
            Typeface.BOLD
        )

        analysisCard.addView(signalText)

        analysisCard.addView(
            makeText(
                "M1     M5     M15     H1     H4",
                15f,
                gray
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        root.addView(analysisCard)

        addSpace(root, 14)

        // ACCOUNT
        val accountCard = createCard()

        accountCard.addView(
            makeText(
                "💰  الحساب",
                15f,
                gray
            )
        )

        balanceText = makeText(
            "الرصيد: --",
            22f,
            white
        )

        balanceText.setTypeface(
            null,
            Typeface.BOLD
        )

        accountCard.addView(balanceText)

        accountCard.addView(
            makeText(
                "الربح اليومي: --",
                17f,
                gray
            )
        )

        root.addView(accountCard)

        addSpace(root, 14)

        // ACTIONS
        val actionsCard = createCard()

        actionsCard.addView(
            makeText(
                "⚡  التحكم السريع",
                15f,
                gray
            )
        )

        val refreshButton = Button(this)

        refreshButton.text =
            "🔄  تحديث سعر الذهب"

        refreshButton.setTextColor(white)

        refreshButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        refreshButton.setOnClickListener {
            fetchGoldPrice()
        }

        actionsCard.addView(
            refreshButton,
            matchParams(8)
        )

        val analyzeButton = Button(this)

        analyzeButton.text =
            "🔍  تحليل XAU/USD"

        analyzeButton.setTextColor(white)

        analyzeButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        actionsCard.addView(
            analyzeButton,
            matchParams(8)
        )

        val mt5Button = Button(this)

        mt5Button.text =
            "⚙️  إعدادات MT5"

        mt5Button.setTextColor(white)

        mt5Button.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        mt5Button.setOnClickListener {
            showMt5Settings()
        }

        actionsCard.addView(
            mt5Button,
            matchParams(8)
        )

        val telegramButton = Button(this)

        telegramButton.text =
            "🔔  Telegram"

        telegramButton.setTextColor(white)

        telegramButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        telegramButton.setOnClickListener {
            toast(
                "Telegram سيتم ربطه لاحقًا"
            )
        }

        actionsCard.addView(
            telegramButton,
            matchParams(8)
        )

        root.addView(actionsCard)

        addSpace(root, 14)

        // SETTINGS
        val settingsCard = createCard()

        settingsCard.addView(
            makeText(
                "⚙️  إعدادات التداول",
                15f,
                gray
            )
        )

        settingsCard.addView(
            makeText(
                "Stop Loss        --",
                16f,
                white
            )
        )

        settingsCard.addView(
            makeText(
                "Take Profit      --",
                16f,
                white
            )
        )

        settingsCard.addView(
            makeText(
                "Risk              1%",
                16f,
                white
            )
        )

        root.addView(settingsCard)

        addSpace(root, 14)

        // LOG
        val activityCard = createCard()

        activityCard.addView(
            makeText(
                "📜  سجل الروبوت",
                15f,
                gray
            )
        )

        activityText = makeText(
            "جاري الاتصال...",
            15f,
            gray
        )

        activityCard.addView(
            activityText
        )

        root.addView(activityCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot  •  Market Monitor",
            12f,
            gray
        )

        footer.gravity = Gravity.CENTER

        root.addView(footer)

        scroll.addView(root)

        setContentView(scroll)
    }

    // =====================================================
    // GOLD PRICE
    // =====================================================

    private fun fetchGoldPrice() {

        priceText.text =
            "جاري التحديث..."

        activityText.text =
            "⏳ جاري الاتصال بمصدر الذهب..."

        thread {

            var connection:
                    HttpURLConnection? = null

            try {

                val url = URL(
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

                val responseCode =
                    connection.responseCode

                if (responseCode != 200) {

                    runOnUiThread {

                        showPriceError(
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

                if (price <= 0) {

                    runOnUiThread {

                        showPriceError(
                            "السعر غير موجود في الاستجابة"
                        )
                    }

                    return@thread
                }

                runOnUiThread {

                    updatePrice(price)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    showPriceError(
                        "تعذر الاتصال بالإنترنت"
                    )
                }

            } finally {

                connection?.disconnect()
            }
        }
    }

    private fun updatePrice(
        price: Double
    ) {

        previousPrice = currentPrice
        currentPrice = price

        priceHistory.add(price)
        timeHistory.add(
            System.currentTimeMillis()
        )

        if (priceHistory.size > 100) {

            priceHistory.removeAt(0)
            timeHistory.removeAt(0)
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
                currentPrice -
                        previousPrice

            val percentage =
                (difference /
                        previousPrice) * 100.0

            val sign =
                if (difference >= 0)
                    "+"
                else
                    ""

            changeText.text =
                String.format(
                    Locale.US,
                    "التغير: %s%.2f (%.3f%%)",
                    sign,
                    difference,
                    percentage
                )

            changeText.setTextColor(
                if (difference >= 0)
                    green
                else
                    red
            )
        }

        activityText.text =
            "✓ تم تحديث سعر الذهب\n" +
            "السعر: " +
            String.format(
                Locale.US,
                "$%,.2f",
                currentPrice
            ) +
            "\nالقراءات المحفوظة: " +
            priceHistory.size

        calculateTrend()
    }

    // =====================================================
    // TREND
    // =====================================================

    private fun calculateTrend() {

        if (priceHistory.size < 3) {

            trendText.text =
                "الاتجاه: نحتاج قراءات أكثر"

            trendText.setTextColor(gold)

            signalText.text =
                "⏳  في انتظار بيانات إضافية"

            signalText.setTextColor(gold)

            return
        }

        val recentCount =
            minOf(
                5,
                priceHistory.size
            )

        val recent =
            priceHistory.takeLast(
                recentCount
            )

        val first =
            recent.first()

        val last =
            recent.last()

        val difference =
            last - first

        val percentage =
            (difference / first) * 100.0

        if (abs(percentage) < 0.01) {

            trendText.text =
                "➖ الاتجاه: محايد"

            trendText.setTextColor(gold)

            signalText.text =
                "🟡 WAIT — لا يوجد اتجاه واضح"

            signalText.setTextColor(gold)

        } else if (difference > 0) {

            trendText.text =
                "🟢 الاتجاه: صاعد"

            trendText.setTextColor(green)

            signalText.text =
                "📈 حركة صاعدة — ليست إشارة تداول"

            signalText.setTextColor(green)

        } else {

            trendText.text =
                "🔴 الاتجاه: هابط"

            trendText.setTextColor(red)

            signalText.text =
                "📉 حركة هابطة — ليست إشارة تداول"

            signalText.setTextColor(red)
        }
    }

    // =====================================================
    // ANALYSIS
    // =====================================================

    private fun analyzeMarket() {

        if (priceHistory.size < 3) {

            signalText.text =
                "⏳ نحتاج 3 قراءات على الأقل"

            signalText.setTextColor(gold)

            fetchGoldPrice()

            return
        }

        signalText.text =
            "🔄 تحليل حركة السعر..."

        signalText.setTextColor(gold)

        animateView(signalText)

        HandlerDelay(1000) {

            calculateTrend()

            activityText.text +=
                "\n✓ اكتمل تحليل الاتجاه"
        }
    }

    // =====================================================
    // BOT
    // =====================================================

    private fun toggleBot() {

        botRunning = !botRunning

        if (botRunning) {

            statusText.text =
                "●  يعمل الآن"

            statusText.setTextColor(green)

            startButton.text =
                "■  إيقاف الروبوت"

            startButton.setBackgroundColor(red)

            activityText.text =
                "✓ تم تشغيل GoldBot\n" +
                "⏳ مراقبة XAU/USD..."

            signalText.text =
                "🔄 مراقبة السوق..."

            signalText.setTextColor(gold)

            animateView(signalText)

        } else {

            statusText.text =
                "●  متوقف"

            statusText.setTextColor(red)

            startButton.text =
                "▶  تشغيل الروبوت"

            startButton.setBackgroundColor(green)

            activityText.text =
                "الروبوت متوقف\n" +
                "اضغط تشغيل للبدء"

            signalText.text =
                "⏳ في انتظار التحليل"

            signalText.setTextColor(gold)
        }
    }

    // =====================================================
    // MT5
    // =====================================================

    private fun showMt5Settings() {

        val layout =
            LinearLayout(this)

        layout.orientation =
            LinearLayout.VERTICAL

        layout.setPadding(
            35,
            20,
            35,
            20
        )

        layout.setBackgroundColor(bg)

        val title =
            makeText(
                "⚙️ إعدادات MT5",
                25f,
                white
            )

        title.setTypeface(
            null,
            Typeface.BOLD
        )

        layout.addView(title)

        addSpace(layout, 15)

        val server =
            EditText(this)

        server.hint =
            "MT5 Server"

        server.setTextColor(white)
        server.setHintTextColor(gray)

        layout.addView(server)

        val login =
            EditText(this)

        login.hint =
            "MT5 Login"

        login.inputType = 2

        login.setTextColor(white)
        login.setHintTextColor(gray)

        layout.addView(login)

        val password =
            EditText(this)

        password.hint =
            "MT5 Password"

        password.inputType = 129

        password.setTextColor(white)
        password.setHintTextColor(gray)

        layout.addView(password
