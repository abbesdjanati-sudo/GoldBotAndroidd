package com.goldbot.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var signalText: TextView
    private lateinit var priceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var activityText: TextView
    private lateinit var startButton: Button

    private var botRunning = false
    private var price = 2645.20

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
        startDemoAnimation()
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

        val version = TextView(this)
        version.text = "  AI TRADING"
        version.textSize = 11f
        version.setTextColor(gray)

        header.addView(logo)
        header.addView(title)
        header.addView(version)

        root.addView(header)

        addSpace(root, 18)

        // STATUS CARD
        val statusCard = createCard()

        val statusTitle = makeText("●  حالة الروبوت", 15f, gray)
        statusCard.addView(statusTitle)

        statusText = makeText("متوقف", 25f, red)
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

        // MARKET CARD
        val marketCard = createCard()

        marketCard.addView(makeText("🥇  الذهب", 15f, gray))

        val pair = makeText("XAU/USD", 24f, white)
        pair.setTypeface(null, Typeface.BOLD)
        marketCard.addView(pair)

        priceText = makeText("$2,645.20", 30f, gold)
        priceText.setTypeface(null, Typeface.BOLD)
        marketCard.addView(priceText)

        val marketStatus = makeText("●  السوق متصل - تجريبي", 14f, green)
        marketCard.addView(marketStatus)

        root.addView(marketCard)

        addSpace(root, 14)

        // SIGNAL CARD
        val signalCard = createCard()

        signalCard.addView(makeText("📊  تحليل السوق", 15f, gray))

        signalText = makeText("⏳  في انتظار التحليل", 23f, gold)
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

        accountCard.addView(makeText("💰  الحساب", 15f, gray))

        balanceText = makeText("الرصيد:  --", 22f, white)
        balanceText.setTypeface(null, Typeface.BOLD)
        accountCard.addView(balanceText)

        val profit = makeText("الربح اليومي:  --", 17f, gray)
        accountCard.addView(profit)

        root.addView(accountCard)

        addSpace(root, 14)

        // ACTIONS
        val actionsCard = createCard()

        actionsCard.addView(makeText("⚡  التحكم السريع", 15f, gray))

        val analyzeButton = Button(this)
        analyzeButton.text = "🔍  تحليل XAU/USD"
        analyzeButton.setTextColor(white)
        analyzeButton.setBackgroundColor(Color.rgb(55, 65, 85))

        analyzeButton.setOnClickListener {
            analyzeMarket()
        }

        actionsCard.addView(analyzeButton, matchParams(8))

        val telegramButton = Button(this)
        telegramButton.text = "🔔  Telegram"
        telegramButton.setTextColor(white)
        telegramButton.setBackgroundColor(Color.rgb(55, 65, 85))

        telegramButton.setOnClickListener {
            toast("Telegram سيتم ربطه لاحقًا")
        }

        actionsCard.addView(telegramButton, matchParams(8))

        root.addView(actionsCard)

        addSpace(root, 14)

        // TRADING SETTINGS
        val settingsCard = createCard()

        settingsCard.addView(makeText("⚙️  إعدادات التداول", 15f, gray))

        settingsCard.addView(makeText("Stop Loss        --", 16f, white))
        settingsCard.addView(makeText("Take Profit      --", 16f, white))
        settingsCard.addView(makeText("Risk              1%", 16f, white))

        root.addView(settingsCard)

        addSpace(root, 14)

        // ACTIVITY
        val activityCard = createCard()

        activityCard.addView(makeText("📜  سجل الروبوت", 15f, gray))

        activityText = makeText(
            "لا توجد صفقات بعد",
            15f,
            gray
        )

        activityCard.addView(activityText)

        root.addView(activityCard)

        addSpace(root, 20)

        val footer = makeText(
            "GoldBot  •  AI Trading Assistant",
            12f,
            gray
        )
        footer.gravity = Gravity.CENTER

        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun toggleBot() {

        botRunning = !botRunning

        if (botRunning) {

            statusText.text = "●  يعمل الآن"
            statusText.setTextColor(green)

            startButton.text = "■  إيقاف الروبوت"
            startButton.setBackgroundColor(red)

            activityText.text =
                "✓ تم تشغيل GoldBot\n" +
                "⏳ جاري مراقبة XAU/USD..."

            signalText.text = "🔄  جاري تحليل السوق..."
            signalText.setTextColor(gold)

            animateView(signalText)

        } else {

            statusText.text = "●  متوقف"
            statusText.setTextColor(red)

            startButton.text = "▶  تشغيل الروبوت"
            startButton.setBackgroundColor(green)

            activityText.text =
                "الروبوت متوقف\n" +
                "اضغط تشغيل للبدء"

            signalText.text = "⏳  في انتظار التحليل"
            signalText.setTextColor(gold)
        }
    }

    private fun analyzeMarket() {

        signalText.text = "🔄  تحليل البيانات..."
        signalText.setTextColor(gold)

        animateView(signalText)

        HandlerDelay(1500) {

            val signals = arrayOf(
                "🟢  BUY  — إشارة شراء",
                "🔴  SELL  — إشارة بيع",
                "🟡  WAIT  — انتظار"
            )

            val signal = signals[(System.currentTimeMillis() % 3).toInt()]

            signalText.text = signal

            when {
                signal.contains("BUY") -> signalText.setTextColor(green)
                signal.contains("SELL") -> signalText.setTextColor(red)
                else -> signalText.setTextColor(gold)
            }

            activityText.text =
                "✓ اكتمل التحليل\n" +
                "XAU/USD\n" +
                "الإشارة: $signal"
        }
    }

    private fun startDemoAnimation() {

        HandlerDelay(2500) {

            price += ((-20..20).random() / 100.0)

            priceText.text = String.format(
                Locale.US,
                "$%,.2f",
                price
            )

            if (botRunning) {
                signalText.text = "🔄  مراقبة السوق..."
                signalText.setTextColor(gold)
            }

            startDemoAnimation()
        }
    }

    private fun createCard(): LinearLayout {

        val cardLayout = LinearLayout(this)
        cardLayout.orientation = LinearLayout.VERTICAL
        cardLayout.setPadding(20, 18, 20, 18)
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
        view.setPadding(0, 5, 0, 5)

        return view
    }

    private fun matchParams(marginTop: Int): LinearLayout.LayoutParams {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.topMargin = marginTop

        return params
    }

    private fun animateView(view: View) {

        val animation = AlphaAnimation(0.35f, 1.0f)
        animation.duration = 700
        animation.repeatMode = AlphaAnimation.REVERSE
        animation.repeatCount = 2

        view.startAnimation(animation)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun HandlerDelay(delay: Long, action: () -> Unit) {

        android.os.Handler(mainLooper).postDelayed({
            action()
        }, delay)
    }
}
