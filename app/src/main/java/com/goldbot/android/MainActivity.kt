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

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var priceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var activityText: TextView
    private lateinit var startButton: Button

    private var botRunning = false
    private var currentPrice = 0.0

    private val bg = Color.rgb(12, 15, 22)
    private val card = Color.rgb(24, 29, 39)
    private val white = Color.WHITE
    private val gray = Color.rgb(170, 178, 190)
    private val green = Color.rgb(35, 190, 105)
    private val red = Color.rgb(225, 75, 75)
    private val gold = Color.rgb(230, 180, 70)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildInterface()

        // جلب السعر الحقيقي عند تشغيل التطبيق
        fetchGoldPrice()
    }

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

        statusText = makeText("●  متوقف", 25f, red)
        statusText.setTypeface(null, Typeface.BOLD)
        statusCard.addView(statusText)

        startButton = Button(this)
        startButton.text = "▶  تشغيل الروبوت"
        startButton.textSize = 16f
        startButton.setTextColor(white)
        startButton.setBackgroundColor(green)

        startButton.setOnClickListener {
            toggleBot()
        }

        statusCard.addView(startButton, matchParams(14))
        root.addView(statusCard)

        addSpace(root, 14)

        // MARKET
        val marketCard = createCard()

        marketCard.addView(
            makeText("🥇  الذهب", 15f, gray)
        )

        val pair = makeText("XAU/USD", 24f, white)
        pair.setTypeface(null, Typeface.BOLD)
        marketCard.addView(pair)

        priceText = makeText(
            "جاري جلب السعر...",
            30f,
            gold
        )

        priceText.setTypeface(null, Typeface.BOLD)
        marketCard.addView(priceText)

        marketCard.addView(
            makeText("●  بيانات الإنترنت", 14f, green)
        )

        root.addView(marketCard)

        addSpace(root, 14)

        // SIGNAL
        val signalCard = createCard()

        signalCard.addView(
            makeText("📊  تحليل السوق", 15f, gray)
        )

        signalText = makeText(
            "⏳  في انتظار التحليل",
            23f,
            gold
        )

        signalText.setTypeface(null, Typeface.BOLD)
        signalText.setPadding(0, 18, 0, 18)

        signalCard.addView(signalText)

        val timeframe = makeText(
            "M1     M5     M15     H1     H4",
            15f,
            gray
        )

        timeframe.gravity = Gravity.CENTER
        signalCard.addView(timeframe)

        root.addView(signalCard)

        addSpace(root, 14)

        // ACCOUNT
        val accountCard = createCard()

        accountCard.addView(
            makeText("💰  الحساب", 15f, gray)
        )

        balanceText = makeText(
            "الرصيد:  --",
            22f,
            white
        )

        balanceText.setTypeface(null, Typeface.BOLD)
        accountCard.addView(balanceText)

        accountCard.addView(
            makeText("الربح اليومي:  --", 17f, gray)
        )

        root.addView(accountCard)

        addSpace(root, 14)

        // ACTIONS
        val actionsCard = createCard()

        actionsCard.addView(
            makeText("⚡  التحكم السريع", 15f, gray)
        )

        val refreshButton = Button(this)
        refreshButton.text = "🔄  تحديث سعر الذهب"
        refreshButton.setTextColor(white)
        refreshButton.setBackgroundColor(Color.rgb(55, 65, 85))

        refreshButton.setOnClickListener {
            fetchGoldPrice()
        }

        actionsCard.addView(
            refreshButton,
            matchParams(8)
        )

        val analyzeButton = Button(this)
        analyzeButton.text = "🔍  تحليل XAU/USD"
        analyzeButton.setTextColor(white)
        analyzeButton.setBackgroundColor(Color.rgb(55, 65, 85))

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        actionsCard.addView(
            analyzeButton,
            matchParams(8)
        )

        val mt5Button = Button(this)
        mt5Button.text = "⚙️  إعدادات MT5"
        mt5Button.setTextColor(white)
        mt5Button.setBackgroundColor(Color.rgb(55, 65, 85))

        mt5Button.setOnClickListener {
            showMt5Settings()
        }

        actionsCard.addView(
            mt5Button,
            matchParams(8)
        )

        val telegramButton = Button(this)
        telegramButton.text = "🔔  Telegram"
        telegramButton.setTextColor(white)
        telegramButton.setBackgroundColor(Color.rgb(55, 65, 85))

        telegramButton.setOnClickListener {
            toast("Telegram سيتم ربطه لاحقًا")
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
            makeText("⚙️  إعدادات التداول", 15f, gray)
        )

        settingsCard.addView(
            makeText("Stop Loss        --", 16f, white)
        )

        settingsCard.addView(
            makeText("Take Profit      --", 16f, white)
        )

        settingsCard.addView(
            makeText("Risk              1%", 16f, white)
        )

        root.addView(settingsCard)

        addSpace(root, 14)

        // LOG
        val activityCard = createCard()

        activityCard.addView(
            makeText("📜  سجل الروبوت", 15f, gray)
        )

        activityText = makeText(
            "جاري الاتصال بمصدر بيانات الذهب...",
            15f,
            gray
        )

        activityCard.addView(activityText)

        root.addView(activityCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot  •  Live Market Data",
            12f,
            gray
        )

        footer.gravity = Gravity.CENTER
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)
    }

    // =====================================================
    // جلب سعر الذهب من الإنترنت
    // =====================================================

    private fun fetchGoldPrice() {

        priceText.text = "جاري التحديث..."
        priceText.setTextColor(gold)

        activityText.text =
            "⏳ جاري الاتصال بمصدر بيانات الذهب..."

        thread {

            try {

                /*
                 * نقطة اختبار عامة.
                 *
                 * ملاحظة:
                 * هذا الجزء يحتاج API حقيقي لسعر XAU/USD.
                 * الرابط التالي مجرد مكان اتصال وليس ضمانًا
                 * بأن الخدمة ستوفر السعر بدون مفتاح.
                 */

                val url = URL(
                    "https://api.gold-api.com/price/XAU"
                )

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode =
                    connection.responseCode

                if (responseCode == 200) {

                    val response =
                        connection.inputStream
                            .bufferedReader()
                            .use { it.readText() }

                    val json =
                        JSONObject(response)

                    val price =
                        json.optDouble(
                            "price",
                            0.0
                        )

                    runOnUiThread {

                        if (price > 0) {

                            currentPrice = price

                            priceText.text =
                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    price
                                )

                            priceText.setTextColor(gold)

                            activityText.text =
                                "✓ تم تحديث سعر الذهب\n" +
                                "XAU/USD\n" +
                                "السعر: " +
                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    price
                                )

                        } else {

                            showPriceError(
                                "مصدر البيانات لم يرجع سعرًا صحيحًا"
                            )
                        }
                    }

                } else {

                    runOnUiThread {

                        showPriceError(
                            "HTTP $responseCode"
                        )
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {

                runOnUiThread {

                    showPriceError(
                        "تعذر الاتصال بالإنترنت"
                    )
                }
            }
        }
    }

    private fun showPriceError(message: String) {

        priceText.text =
            "السعر غير متاح"

        priceText.setTextColor(red)

        activityText.text =
            "❌ $message\n" +
            "اضغط تحديث وحاول مرة أخرى."
    }

    // =====================================================
    // MT5 SETTINGS
    // =====================================================

    private fun showMt5Settings() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(35, 20, 35, 20)
        layout.setBackgroundColor(bg)

        val title = makeText(
            "⚙️ إعدادات MT5",
            25f,
            white
        )

        title.setTypeface(null, Typeface.BOLD)
        layout.addView(title)

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

        val testButton = Button(this)
        testButton.text = "🔌 اختبار الاتصال"
        testButton.setTextColor(white)
        testButton.setBackgroundColor(green)

        testButton.setOnClickListener {

            Toast.makeText(
                this,
                "الاتصال الحقيقي بـ MT5 يحتاج MT5 Desktop أو VPS",
                Toast.LENGTH_LONG
            ).show()
        }

        layout.addView(testButton)

        addSpace(layout, 8)

        val closeButton = Button(this)
        closeButton.text = "رجوع"
        closeButton.setTextColor(white)
        closeButton.setBackgroundColor(
            Color.rgb(55, 65, 85)
        )

        layout.addView(closeButton)

        val dialog = android.app.Dialog(this)

        dialog.setContentView(layout)

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    // =====================================================
    // BOT
    // =====================================================

    private fun toggleBot() {

        botRunning = !botRunning

        if (botRunning) {

            statusText.text = "●  يعمل الآن"
            statusText.setTextColor(green)

            startButton.text =
                "■  إيقاف الروبوت"

            startButton.setBackgroundColor(red)

            activityText.text =
                "✓ تم تشغيل GoldBot\n" +
                "⏳ جاري مراقبة XAU/USD..."

            signalText.text =
                "🔄  جاري تحليل السوق..."

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
                "⏳  في انتظار التحليل"

            signalText.setTextColor(gold)
        }
    }

    // =====================================================
    // ANALYSIS
    // =====================================================

    private fun analyzeMarket() {

        if (currentPrice <= 0) {

            signalText.text =
                "⚠️  لا يوجد سعر حقيقي بعد"

            signalText.setTextColor(red)

            fetchGoldPrice()

            return
        }

        signalText.text =
            "🔄  تحليل البيانات..."

        signalText.setTextColor(gold)

        animateView(signalText)

        HandlerDelay(1500) {

            /*
             * مؤقتًا لا نعطي BUY/SELL عشوائي.
             * سنضيف التحليل الحقيقي بعد إدخال بيانات
             * الشموع والمؤشرات.
             */

            signalText.text =
                "🟡  WAIT — بانتظار التحليل الحقيقي"

            signalText.setTextColor(gold)

            activityText.text =
                "✓ تم الحصول على سعر XAU/USD\n" +
                "السعر الحالي: " +
                String.format(
                    Locale.US,
                    "$%,.2f",
                    currentPrice
                ) +
                "\n⏳ محرك التحليل قيد التطوير"
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private fun createCard(): LinearLayout {

        val cardLayout =
            LinearLayout(this)

        cardLayout.orientation =
            LinearLayout.VERTICAL

        cardLayout.setPadding(
            20,
            18,
            20,
            18
        )

        cardLayout.setBackgroundColor(card)

        return cardLayout
    }

    private fun makeText(
        text: String,
        size: Float,
        color: Int
    ): TextView {

        val view = TextView(this)

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

    private fun matchParams(
        marginTop: Int
    ): LinearLayout.LayoutParams {

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.topMargin = marginTop

        return params
    }

    private fun addSpace(
        root: LinearLayout,
        height: Int
    ) {

        val space = Space(this)

        root.addView(
            space,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        )
    }

    private fun animateView(view: View) {

        val animation =
            AlphaAnimation(
                0.35f,
                1.0f
            )

        animation.duration = 700
        animation.repeatMode =
            AlphaAnimation.REVERSE

        animation.repeatCount = 2

        view.startAnimation(animation)
    }

    private fun HandlerDelay(
        delay: Long,
        action: () -> Unit
    ) {

        android.os.Handler(mainLooper)
            .postDelayed(
                { action() },
                delay
            )
    }

    private fun toast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
