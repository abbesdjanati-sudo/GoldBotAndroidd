import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// =====================================================
// GOLD BOT PRO - V1
// Educational Trading Engine
// =====================================================

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
)

enum class Signal {
    BUY,
    SELL,
    WAIT
}

data class Analysis(
    val signal: Signal,
    val confidence: Int,
    val price: Double,
    val ema20: Double?,
    val ema50: Double?,
    val rsi: Double?,
    val atr: Double?,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val reason: String
)

data class Trade(
    val type: Signal,
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val lot: Double,
    val riskMoney: Double
)

// =====================================================
// INDICATORS
// =====================================================

object Indicators {

    fun sma(
        values: List<Double>,
        period: Int
    ): Double? {

        if (values.size < period) return null

        return values
            .takeLast(period)
            .average()
    }

    fun ema(
        values: List<Double>,
        period: Int
    ): Double? {

        if (values.size < period) return null

        val multiplier = 2.0 / (period + 1)

        var result =
            values.take(period).average()

        for (i in period until values.size) {

            result =
                ((values[i] - result) * multiplier) +
                        result
        }

        return result
    }

    fun rsi(
        values: List<Double>,
        period: Int = 14
    ): Double? {

        if (values.size <= period) return null

        var gain = 0.0
        var loss = 0.0

        for (i in 1..period) {

            val change =
                values[i] - values[i - 1]

            if (change >= 0) {
                gain += change
            } else {
                loss += abs(change)
            }
        }

        var avgGain = gain / period
        var avgLoss = loss / period

        for (i in period + 1 until values.size) {

            val change =
                values[i] - values[i - 1]

            val currentGain =
                max(change, 0.0)

            val currentLoss =
                max(-change, 0.0)

            avgGain =
                ((avgGain * (period - 1)) +
                        currentGain) / period

            avgLoss =
                ((avgLoss * (period - 1)) +
                        currentLoss) / period
        }

        if (avgLoss == 0.0)
            return 100.0

        val rs =
            avgGain / avgLoss

        return 100.0 -
                (100.0 / (1.0 + rs))
    }

    fun atr(
        candles: List<Candle>,
        period: Int = 14
    ): Double? {

        if (candles.size <= period)
            return null

        val ranges =
            mutableListOf<Double>()

        for (i in 1 until candles.size) {

            val current =
                candles[i]

            val previous =
                candles[i - 1]

            val r1 =
                current.high - current.low

            val r2 =
                abs(
                    current.high -
                            previous.close
                )

            val r3 =
                abs(
                    current.low -
                            previous.close
                )

            ranges.add(
                max(
                    r1,
                    max(r2, r3)
                )
            )
        }

        if (ranges.size < period)
            return null

        return ranges
            .takeLast(period)
            .average()
    }

    fun highest(
        values: List<Double>,
        period: Int
    ): Double? {

        if (values.size < period)
            return null

        return values
            .takeLast(period)
            .maxOrNull()
    }

    fun lowest(
        values: List<Double>,
        period: Int
    ): Double? {

        if (values.size < period)
            return null

        return values
            .takeLast(period)
            .minOrNull()
    }
}

// =====================================================
// RISK MANAGER
// =====================================================

class RiskManager(
    private val balance: Double,
    private val riskPercent: Double
) {

    fun riskMoney(): Double {

        return balance *
                riskPercent /
                100.0
    }

    fun calculateLot(
        entry: Double,
        stopLoss: Double
    ): Double {

        val distance =
            abs(entry - stopLoss)

        if (distance <= 0)
            return 0.0

        /*
         * هذا حساب تعليمي.
         * حجم اللوت الحقيقي يعتمد على
         * مواصفات العقد لدى الوسيط.
         */

        val risk =
            riskMoney()

        val estimatedLot =
            risk / (distance * 100.0)

        return estimatedLot
            .coerceIn(0.01, 5.0)
    }
}

// =====================================================
// STRATEGY
// =====================================================

class GoldStrategy {

    fun analyze(
        candles: List<Candle>
    ): Analysis {

        val closes =
            candles.map { it.close }

        val price =
            closes.last()

        val ema20 =
            Indicators.ema(
                closes,
                20
            )

        val ema50 =
            Indicators.ema(
                closes,
                50
            )

        val rsi =
            Indicators.rsi(
                closes,
                14
            )

        val atr =
            Indicators.atr(
                candles,
                14
            )

        if (
            ema20 == null ||
            ema50 == null ||
            rsi == null ||
            atr == null
        ) {

            return Analysis(
                signal = Signal.WAIT,
                confidence = 0,
                price = price,
                ema20 = ema20,
                ema50 = ema50,
                rsi = rsi,
                atr = atr,
                stopLoss = null,
                takeProfit = null,
                reason = "بيانات غير كافية"
            )
        }

        var buyScore = 0
        var sellScore = 0

        val reasons =
            mutableListOf<String>()

        // Trend

        if (ema20 > ema50) {

            buyScore += 30

            reasons.add(
                "الاتجاه العام صاعد"
            )

        } else if (ema20 < ema50) {

            sellScore += 30

            reasons.add(
                "الاتجاه العام هابط"
            )
        }

        // Price

        if (price > ema20) {

            buyScore += 20

        } else if (price < ema20) {

            sellScore += 20
        }

        // RSI

        if (rsi in 50.0..68.0) {

            buyScore += 25

        } else if (rsi in 32.0..50.0) {

            sellScore += 25
        }

        // Momentum

        val recentHigh =
            Indicators.highest(
                closes,
                10
            )

        val recentLow =
            Indicators.lowest(
                closes,
                10
            )

        if (
            recentHigh != null &&
            price >= recentHigh
        ) {

            buyScore += 15
        }

        if (
            recentLow != null &&
            price <= recentLow
        ) {

            sellScore += 15
        }

        // ==========================================
        // BUY
        // ==========================================

        if (buyScore >= 65) {

            val sl =
                price - atr * 1.5

            val tp =
                price + atr * 3.0

            return Analysis(
                signal = Signal.BUY,
                confidence = buyScore,
                price = price,
                ema20 = ema20,
                ema50 = ema50,
                rsi = rsi,
                atr = atr,
                stopLoss = sl,
                takeProfit = tp,
                reason =
                    reasons.joinToString(" + ")
            )
        }

        // ==========================================
        // SELL
        // ==========================================

        if (sellScore >= 65) {

            val sl =
                price + atr * 1.5

            val tp =
                price - atr * 3.0

            return Analysis(
                signal = Signal.SELL,
                confidence = sellScore,
                price = price,
                ema20 = ema20,
                ema50 = ema50,
                rsi = rsi,
                atr = atr,
                stopLoss = sl,
                takeProfit = tp,
                reason =
                    reasons.joinToString(" + ")
            )
        }

        return Analysis(
            signal = Signal.WAIT,
            confidence =
                max(
                    buyScore,
                    sellScore
                ),
            price = price,
            ema20 = ema20,
            ema50 = ema50,
            rsi = rsi,
            atr = atr,
            stopLoss = null,
            takeProfit = null,
            reason =
                "لا توجد إشارة قوية"
        )
    }
}

// =====================================================
// GOLD BOT
// =====================================================

class GoldBot(
    private val balance: Double
) {

    private val strategy =
        GoldStrategy()

    private val riskManager =
        RiskManager(
            balance = balance,
            riskPercent = 1.0
        )

    private val history =
        mutableListOf<Trade>()

    fun analyze(
        candles: List<Candle>
    ) {

        val result =
            strategy.analyze(candles)

        println()
        println("================================")
        println("          GOLD BOT PRO")
        println("================================")

        println(
            "Price      : %.2f"
                .format(result.price)
        )

        println(
            "EMA20      : %s"
                .format(result.ema20)
        )

        println(
            "EMA50      : %s"
                .format(result.ema50)
        )

        println(
            "RSI        : %s"
                .format(result.rsi)
        )

        println(
            "ATR        : %s"
                .format(result.atr)
        )

        println(
            "Signal     : ${result.signal}"
        )

        println(
            "Confidence : ${result.confidence}%"
        )

        println(
            "Reason     : ${result.reason}"
        )

        if (
            result.signal != Signal.WAIT &&
            result.stopLoss != null &&
            result.takeProfit != null
        ) {

            val lot =
                riskManager.calculateLot(
                    result.price,
                    result.stopLoss
                )

            println(
                "Stop Loss  : %.2f"
                    .format(result.stopLoss)
            )

            println(
                "Take Profit: %.2f"
                    .format(result.takeProfit)
            )

            println(
                "Lot        : %.2f"
                    .format(lot)
            )

            println(
                "Risk       : %.2f USD"
                    .format(
                        riskManager.riskMoney()
                    )
            )

            history.add(
                Trade(
                    type = result.signal,
                    entry = result.price,
                    stopLoss = result.stopLoss,
                    takeProfit = result.takeProfit,
                    lot = lot,
                    riskMoney =
                        riskManager.riskMoney()
                )
            )
        }

        println("================================")
    }

    fun showHistory() {

        println()
        println("TRADE HISTORY")

        if (history.isEmpty()) {

            println(
                "لا توجد صفقات"
            )

            return
        }

        history.forEachIndexed {
                index,
                trade ->

            println(
                "${index + 1}. " +
                        "${trade.type} | " +
                        "Entry=${trade.entry} | " +
                        "SL=${trade.stopLoss} | " +
                        "TP=${trade.takeProfit} | " +
                        "Lot=${trade.lot}"
            )
        }
    }
}

// =====================================================
// TEST DATA
// =====================================================

fun generateMarketData(): List<Candle> {

    val result =
        mutableListOf<Candle>()

    var price = 2650.0

    for (i in 0 until 120) {

        val movement =
            when {

                i < 30 ->
                    0.8

                i < 60 ->
                    1.2

                i < 90 ->
                    1.8

                else ->
                    1.4
            }

        val open =
            price

        val close =
            open + movement

        val high =
            close + 2.0

        val low =
            open - 1.5

        result.add(
            Candle(
                time = i.toLong(),
                open = open,
                high = high,
                low = low,
                close = close,
                volume = 1000.0
            )
        )

        price =
            close
    }

    return result
}

// =====================================================
// MAIN
// =====================================================

fun main() {

    println()
    println("Starting GOLD BOT PRO...")
    println()

    val marketData =
        generateMarketData()

    val bot =
        GoldBot(
            balance = 1000.0
        )

    bot.analyze(
        marketData
    )

    bot.showHistory()

    println()
    println("Bot finished.")
}
