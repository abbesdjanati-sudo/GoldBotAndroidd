private lateinit var priceView: TextView
private lateinit var statusView: TextView
private lateinit var signalView: TextView
private lateinit var analysisView: TextView
private lateinit var tradeView: TextView
private lateinit var accountView: TextView
private lateinit var logView: TextView
private lateinit var startButton: Button
private lateinit var paperButton: Button

private val prices = ArrayList<Double>()
private val handler = Handler(Looper.getMainLooper())

private var botRunning = false
private var paperTrading = false
private var currentPrice = 0.0
private var currentSignal = "WAIT"

private var balance = 10000.0
private val initialBalance = 10000.0
private var riskPercent = 1.0

private var position = ""
private var entryPrice = 0.0
private var stopLoss = 0.0
private var takeProfit = 0.0
private var positionSize = 0.0

private var telegramToken = ""
private var telegramChatId = ""

private var mt5Server = ""
private var mt5Login = ""
private var mt5Password = ""

private val bg = Color.rgb(15, 18, 25)
private val card = Color.rgb(27, 31, 42)
private val gold = Color.rgb(255, 215, 0)
private val green = Color.rgb(0, 220, 120)
private val red = Color.rgb(255, 80, 80)
private val orange = Color.rgb(255, 170, 0)

private val updater = object : Runnable {
    override fun run() {
        if (botRunning) {
            fetchGoldPrice()
        }
        handler.postDelayed(this, 15000L)
    }
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.statusBarColor = bg
    window.navigationBarColor = bg
    loadSettings()
    buildInterface()
    handler.postDelayed(updater, 1000L)
}

override fun onDestroy() {
    handler.removeCallbacks(updater)
    super.onDestroy()
}

private fun buildInterface() {
    val scroll = ScrollView(this)
    val root = LinearLayout(this)
    root.orientation = LinearLayout.VERTICAL
    root.setPadding(24, 24, 24, 40)
    root.setBackgroundColor(bg)
    scroll.addView(root)

    val title = TextView(this)
    title.text = "GOLD BOT"
    title.textSize = 30f
    title.setTextColor(gold)
    title.gravity = Gravity.CENTER
    root.addView(title)

    val subtitle = TextView(this)
    subtitle.text = "XAU/USD Trading Assistant"
    subtitle.textSize = 14f
    subtitle.setTextColor(Color.LTGRAY)
    subtitle.gravity = Gravity.CENTER
    root.addView(subtitle)

    root.addView(space(14))

    statusView = TextView(this)
    statusView.text = "البوت متوقف"
    statusView.textSize = 19f
    statusView.setTextColor(red)
    statusView.gravity = Gravity.CENTER
    statusView.setPadding(15, 15, 15, 15)
    statusView.setBackgroundColor(card)
    root.addView(statusView)

    root.addView(space(10))

    priceView = TextView(this)
    priceView.text = "XAU/USD\n--"
    priceView.textSize = 28f
    priceView.setTextColor(gold)
    priceView.gravity = Gravity.CENTER
    priceView.setPadding(15, 25, 15, 25)
    priceView.setBackgroundColor(card)
    root.addView(priceView)

    root.addView(space(10))

    startButton = Button(this)
    startButton.text = "تشغيل البوت"
    startButton.setOnClickListener { toggleBot() }
    root.addView(startButton)

    val refreshButton = Button(this)
    refreshButton.text = "تحديث السعر"
    refreshButton.setOnClickListener { fetchGoldPrice() }
    root.addView(refreshButton)

    paperButton = Button(this)
    paperButton.text = "Paper Trading: OFF"
    paperButton.setOnClickListener { togglePaperTrading() }
    root.addView(paperButton)

    root.addView(sectionTitle("إشارة السوق"))

    signalView = TextView(this)
    signalView.text = "WAIT"
    signalView.textSize = 30f
    signalView.setTextColor(orange)
    signalView.gravity = Gravity.CENTER
    signalView.setPadding(15, 25, 15, 25)
    signalView.setBackgroundColor(card)
    root.addView(signalView)

    root.addView(sectionTitle("التحليل الفني"))

    analysisView = TextView(this)
    analysisView.text = "EMA 9: --\nEMA 21: --\nRSI 14: --\nMACD: --\nTrend: --\nData points: 0"
    analysisView.textSize = 17f
    analysisView.setTextColor(Color.WHITE)
    analysisView.setPadding(20, 20, 20, 20)
    analysisView.setBackgroundColor(card)
    root.addView(analysisView)

    root.addView(sectionTitle("إدارة الصفقة"))

    tradeView = TextView(this)
    tradeView.text = "Entry: --\nStop Loss: --\nTake Profit: --\nPosition Size: --\nRisk: 1%"
    tradeView.textSize = 17f
    tradeView.setTextColor(Color.WHITE)
    tradeView.setPadding(20, 20, 20, 20)
    tradeView.setBackgroundColor(card)
    root.addView(tradeView)

    root.addView(sectionTitle("الحساب التجريبي"))

    accountView = TextView(this)
    accountView.text = "الرصيد: 10000.00 $\nالصفقة: لا توجد\nP/L: 0.00 $"
    accountView.textSize = 17f
    accountView.setTextColor(Color.LTGRAY)
    accountView.setPadding(20, 20, 20, 20)
    accountView.setBackgroundColor(card)
    root.addView(accountView)

    val closeButton = Button(this)
    closeButton.text = "إغلاق الصفقة التجريبية"
    closeButton.setOnClickListener { closePaperTrade() }
    root.addView(closeButton)

    val riskButton = Button(this)
    riskButton.text = "إعدادات المخاطرة"
    riskButton.setOnClickListener { showRiskDialog() }
    root.addView(riskButton)

    root.addView(sectionTitle("الاتصالات"))

    val telegramButton = Button(this)
    telegramButton.text = "إعدادات Telegram"
    telegramButton.setOnClickListener { showTelegramDialog() }
    root.addView(telegramButton)

    val mt5Button = Button(this)
    mt5Button.text = "إعدادات MT5"
    mt5Button.setOnClickListener { showMt5Dialog() }
    root.addView(mt5Button)

    val resetButton = Button(this)
    resetButton.text = "إعادة الحساب"
    resetButton.setOnClickListener { resetPaperAccount() }
    root.addView(resetButton)

    root.addView(sectionTitle("السجل"))

    logView = TextView(this)
    logView.text = "GoldBot جاهز..."
    logView.textSize = 14f
    logView.setTextColor(Color.LTGRAY)
    logView.setPadding(15, 15, 15, 15)
    logView.setBackgroundColor(card)
    root.addView(logView)

    setContentView(scroll)
}

private fun sectionTitle(text: String): TextView {
    val view = TextView(this)
    view.text = text
    view.textSize = 20f
    view.setTextColor(gold)
    view.setPadding(5, 14, 5, 8)
    return view
}

private fun space(height: Int): View {
    val view = Space(this)
    view.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        height
    )
    return view
}

private fun toggleBot() {
    botRunning = !botRunning

    if (botRunning) {
        statusView.text = "البوت يعمل"
        statusView.setTextColor(green)
        startButton.text = "إيقاف البوت"
        addLog("تم تشغيل GoldBot")
        fetchGoldPrice()
    } else {
        statusView.text = "البوت متوقف"
        statusView.setTextColor(red)
        startButton.text = "تشغيل البوت"
        addLog("تم إيقاف GoldBot")
    }
}

private fun togglePaperTrading() {
    paperTrading = !paperTrading

    if (paperTrading) {
        paperButton.text = "Paper Trading: ON"
        addLog("تم تشغيل التداول التجريبي")
    } else {
        paperButton.text = "Paper Trading: OFF"
        addLog("تم إيقاف التداول التجريبي")
    }
}

private fun fetchGoldPrice() {
    Thread {
        var connection: HttpURLConnection? = null

        try {
            val url = URL("https://api.gold-api.com/price/XAU")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode

            if (code != 200) {
                runOnUiThread {
                    addLog("خطأ السعر: HTTP $code")
                }
                return@Thread
            }

            val response = connection.inputStream.bufferedReader().use {
                it.readText()
            }

            val json = JSONObject(response)
            val price = json.getDouble("price")

            runOnUiThread {
                onNewPrice(price)
            }
        } catch (e: Exception) {
            runOnUiThread {
                addLog("خطأ الاتصال: ${e.message ?: "Unknown"}")
            }
        } finally {
            connection?.disconnect()
        }
    }.start()
}

private fun onNewPrice(price: Double) {
    if (price <= 0.0) {
        return
    }

    currentPrice = price
    prices.add(price)

    if (prices.size > 300) {
        prices.removeAt(0)
    }

    priceView.text = String.format(
        Locale.US,
        "XAU/USD\n%.2f $",
        price
    )

    calculateAnalysis()
    updatePaperTrade()
    updateAccount()
}

private fun calculateAnalysis() {
    if (prices.size < 26) {
        analysisView.text =
            "EMA 9: --\n" +
            "EMA 21: --\n" +
            "RSI 14: --\n" +
            "MACD: --\n" +
            "Trend: جمع البيانات...\n" +
            "Data points: ${prices.size}"

        currentSignal = "WAIT"
        signalView.text = "WAIT"
        signalView.setTextColor(orange)
        return
    }

    val ema9 = calculateEma(prices, 9)
    val ema21 = calculateEma(prices, 21)
    val rsi = calculateRsi(prices, 14)
    val macd = calculateMacd(prices)
    val newSignal = generateSignal(ema9, ema21, rsi, macd)

    currentSignal = newSignal

    val trend = when {
        ema9 > ema21 -> "صاعد"
        ema9 < ema21 -> "هابط"
        else -> "محايد"
    }

    analysisView.text = String.format(
        Locale.US,
        "EMA 9: %.2f\nEMA 21: %.2f\nRSI 14: %.2f\nMACD: %.4f\nTrend: %s\nData points: %d",
        ema9,
        ema21,
        rsi,
        macd,
        trend,
        prices.size
    )

    signalView.text = newSignal

    when (newSignal) {
        "BUY" -> {
            signalView.setTextColor(green)
            calculateTradeLevels("BUY")
        }

        "SELL" -> {
            signalView.setTextColor(red)
            calculateTradeLevels("SELL")
        }

        else -> {
            signalView.setTextColor(orange)
        }
    }
}

private fun calculateEma(data: List<Double>, period: Int): Double {
    if (data.isEmpty()) {
        return 0.0
    }

    val startCount = minOf(period, data.size)
    var ema = 0.0

    for (i in 0 until startCount) {
        ema += data[i]
    }

    ema /= startCount.toDouble()

    val multiplier = 2.0 / (period + 1.0)

    for (i in startCount until data.size) {
        ema = ((data[i] - ema) * multiplier) + ema
    }

    return ema
}

private fun calculateRsi(data: List<Double>, period: Int): Double {
    if (data.size <= period) {
        return 50.0
    }

    var gain = 0.0
    var loss = 0.0
    val start = data.size - period

    for (i in start until data.size) {
        val change = data[i] - data[i - 1]

        if (change > 0.0) {
            gain += change
        } else {
            loss += abs(change)
        }
    }

    val averageGain = gain / period
    val averageLoss = loss / period

    if (averageLoss == 0.0) {
        return 100.0
    }

    val rs = averageGain / averageLoss
    return 100.0 - (100.0 / (1.0 + rs))
}

private fun calculateMacd(data: List<Double>): Double {
    if (data.size < 26) {
        return 0.0
    }

    val fast = calculateEma(data, 12)
    val slow = calculateEma(data, 26)

    return fast - slow
}

private fun generateSignal(
    ema9: Double,
    ema21: Double,
    rsi: Double,
    macd: Double
): String {
    var buyScore = 0
    var sellScore = 0

    if (ema9 > ema21) {
        buyScore++
    }

    if (ema9 < ema21) {
        sellScore++
    }

    if (rsi > 50.0 && rsi < 70.0) {
        buyScore++
    }

    if (rsi < 50.0 && rsi > 30.0) {
        sellScore++
    }

    if (macd > 0.0) {
        buyScore++
    }

    if (macd < 0.0) {
        sellScore++
    }

    return when {
        buyScore >= 3 -> "BUY"
        sellScore >= 3 -> "SELL"
        else -> "WAIT"
    }
}

private fun calculateTradeLevels(direction: String) {
    if (currentPrice <= 0.0) {
        return
    }

    val distance = max(currentPrice * 0.002, 1.0)

    if (direction == "BUY") {
        stopLoss = currentPrice - distance
        takeProfit = currentPrice + (distance * 2.0)
    } else {
        stopLoss = currentPrice + distance
        takeProfit = currentPrice - (distance * 2.0)
    }

    val riskMoney = balance * (riskPercent / 100.0)
    val riskDistance = abs(currentPrice - stopLoss)

    positionSize = if (riskDistance > 0.0) {
        riskMoney / riskDistance
    } else {
        0.0
    }

    tradeView.text = String.format(
        Locale.US,
        "Entry: %.2f\nStop Loss: %.2f\nTake Profit: %.2f\nPosition Size: %.4f\nRisk: %.1f%%",
        currentPrice,
        stopLoss,
        takeProfit,
        positionSize,
        riskPercent
    )
}

private fun openPaperTrade(direction: String) {
    if (!paperTrading || position.isNotEmpty() || currentPrice <= 0.0) {
        return
    }

    position = direction
    entryPrice = currentPrice
    calculateTradeLevels(direction)

    addLog("فتح $direction عند ${formatPrice(entryPrice)}")

    sendTelegram(
        "GoldBot Paper Trade\n" +
            "Signal: $direction\n" +
            "Entry: ${formatPrice(entryPrice)}\n" +
            "SL: ${formatPrice(stopLoss)}\n" +
            "TP: ${formatPrice(takeProfit)}"
    )
}

private fun updatePaperTrade() {
    if (!paperTrading) {
        return
    }

    if (position.isEmpty()) {
        if (currentSignal == "BUY") {
            openPaperTrade("BUY")
        } else if (currentSignal == "SELL") {
            openPaperTrade("SELL")
        }
        return
    }

    if (position == "BUY") {
        if (currentPrice <= stopLoss || currentPrice >= takeProfit) {
            closePaperTrade()
        }
    } else if (position == "SELL") {
        if (currentPrice >= stopLoss || currentPrice <= takeProfit) {
            closePaperTrade()
        }
    }
}

private fun closePaperTrade() {
    if (position.isEmpty()) {
        addLog("لا توجد صفقة مفتوحة")
        return
    }

    val difference = if (position == "BUY") {
        currentPrice - entryPrice
    } else {
        entryPrice - currentPrice
    }

    val profit = difference * positionSize
    balance += profit

    addLog(
        "إغلاق $position | P/L: " +
            String.format(Locale.US, "%.2f $", profit)
    )

    sendTelegram(
        "GoldBot Trade Closed\n" +
            "Direction: $position\n" +
            "Entry: ${formatPrice(entryPrice)}\n" +
            "Exit: ${formatPrice(currentPrice)}\n" +
            "P/L: ${String.format(Locale.US, "%.2f", profit)} $"
    )

    position = ""
    entryPrice = 0.0
    stopLoss = 0.0
    takeProfit = 0.0
    positionSize = 0.0

    updateAccount()
}

private fun updateAccount() {
    val floating = if (position.isEmpty()) {
        0.0
    } else {
        val difference = if (position == "BUY") {
            currentPrice - entryPrice
        } else {
            entryPrice - currentPrice
        }

        difference * positionSize
    }

    val positionText = if (position.isEmpty()) {
        "لا توجد"
    } else {
        "$position @ ${formatPrice(entryPrice)}"
    }

    accountView.text = String.format(
        Locale.US,
        "الرصيد: %.2f $\nالصفقة: %s\nP/L العائم: %.2f $",
        balance,
        positionText,
        floating
    )
}

private fun showRiskDialog() {
    val input = EditText(this)
    input.hint = "Risk %"
    input.setText(riskPercent.toString())

    AlertDialog.Builder(this)
        .setTitle("إدارة المخاطرة")
        .setMessage("حدد نسبة المخاطرة لكل صفقة تجريبية.")
        .setView(input)
        .setPositiveButton("حفظ") { _, _ ->
            val value = input.text.toString().toDoubleOrNull()

            if (value != null && value > 0.0 && value <= 10.0) {
                riskPercent = value
                addLog("تم ضبط المخاطرة إلى $riskPercent%")

                if (currentSignal == "BUY" || currentSignal == "SELL") {
                    calculateTradeLevels(currentSignal)
                }
            } else {
                addLog("قيمة المخاطرة غير صالحة")
            }
        }
        .setNegativeButton("إلغاء", null)
        .show()
}

private fun showTelegramDialog() {
    val layout = LinearLayout(this)
    layout.orientation = LinearLayout.VERTICAL
    layout.setPadding(30, 10, 30, 10)

    val tokenInput = EditText(this)
    tokenInput.hint = "Telegram Bot Token"
    tokenInput.setText(telegramToken)
    layout.addView(tokenInput)

    val chatInput = EditText(this)
    chatInput.hint = "Telegram Chat ID"
    chatInput.setText(telegramChatId)
    layout.addView(chatInput)

    AlertDialog.Builder(this)
        .setTitle("Telegram")
        .setView(layout)
        .setPositiveButton("حفظ") { _, _ ->
            telegramToken = tokenInput.text.toString().trim()
            telegramChatId = chatInput.text.toString().trim()
            saveSettings()
            addLog("تم حفظ Telegram")
        }
        .setNeutralButton("اختبار") { _, _ ->
            telegramToken = tokenInput.text.toString().trim()
            telegramChatId = chatInput.text.toString().trim()
            saveSettings()
            sendTelegram("GoldBot Telegram Test")
        }
        .setNegativeButton("إلغاء", null)
        .show()
}

private fun sendTelegram(message: String) {
    if (telegramToken.isEmpty() || telegramChatId.isEmpty()) {
        return
    }

    Thread {
        var connection: HttpURLConnection? = null

        try {
            val encoded = URLEncoder.encode(message, "UTF-8")
            val url = URL(
                "https://api.telegram.org/bot" +
                    telegramToken +
                    "/sendMessage?chat_id=" +
                    telegramChatId +
                    "&text=" +
                    encoded
            )

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val code = connection.responseCode

            runOnUiThread {
                if (code == 200) {
                    addLog("Telegram: تم الإرسال")
                } else {
                    addLog("Telegram HTTP: $code")
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                addLog("Telegram Error: ${e.message ?: "Unknown"}")
            }
        } finally {
            connection?.disconnect()
        }
    }.start()
}

private fun showMt5Dialog() {
    val layout = LinearLayout(this)
    layout.orientation = LinearLayout.VERTICAL
    layout.setPadding(30, 10, 30, 10)

    val serverInput = EditText(this)
    serverInput.hint = "MT5 Server"
    serverInput.setText(mt5Server)
    layout.addView(serverInput)

    val loginInput = EditText(this)
    loginInput.hint = "MT5 Login"
    loginInput.setText(mt5Login)
    layout.addView(loginInput)

    val passwordInput = EditText(this)
    passwordInput.hint = "MT5 Password"
    passwordInput.setText(mt5Password)
    passwordInput.inputType = 129
    layout.addView(passwordInput)

    AlertDialog.Builder(this)
        .setTitle("MT5 Settings")
        .setMessage("الإعدادات تُحفظ فقط. التنفيذ الحقيقي يحتاج MT5 Desktop/VPS أو Bridge.")
        .setView(layout)
        .setPositiveButton("حفظ") { _, _ ->
            mt5Server = serverInput.text.toString().trim()
            mt5Login = loginInput.text.toString().trim()
            mt5Password = passwordInput.text.toString()
            saveSettings()
            addLog("تم حفظ إعدادات MT5")
        }
        .setNegativeButton("إلغاء", null)
        .show()
}

private fun resetPaperAccount() {
    balance = initialBalance
    position = ""
    entryPrice = 0.0
    stopLoss = 0.0
    takeProfit = 0.0
    positionSize = 0.0
    addLog("تمت إعادة الحساب إلى 10000 $")
    updateAccount()
}

private fun saveSettings() {
    getSharedPreferences("goldbot_settings", MODE_PRIVATE)
        .edit()
        .putString("telegram_token", telegramToken)
        .putString("telegram_chat_id", telegramChatId)
        .putString("mt5_server", mt5Server)
        .putString("mt5_login", mt5Login)
        .putString("mt5_password", mt5Password)
        .apply()
}

private fun loadSettings() {
    val preferences = getSharedPreferences(
        "goldbot_settings",
        MODE_PRIVATE
    )

    telegramToken = preferences.getString("telegram_token", "") ?: ""
    telegramChatId = preferences.getString("telegram_chat_id", "") ?: ""
    mt5Server = preferences.getString("mt5_server", "") ?: ""
    mt5Login = preferences.getString("mt5_login", "") ?: ""
    mt5Password = preferences.getString("mt5_password", "") ?: ""
}

private fun addLog(message: String) {
    if (!::logView.isInitialized) {
        return
    }

    val old = logView.text.toString()
    val combined = "$message\n$old"

    logView.text = if (combined.length > 6000) {
        combined.substring(0, 6000)
    } else {
        combined
    }
}

private fun formatPrice(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}
