package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBook
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBookEventsHandler
import tech.sharply.metch.orderbook.domain.model.orderbook.Trade
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.util.generateBigDecimal
import tech.sharply.metch.orderbook.util.generateOrderAction
import tech.sharply.metch.orderbook.util.generateOrderType
import java.math.BigDecimal

internal class NaiveOrderBookTest {

    private val log = LoggerFactory.getLogger(javaClass)

    private var orderBook: OrderBook = NaiveOrderBook(object : OrderBookEventsHandler {
        override fun handle(event: OrderPlacedEvent) {
//            log.info("Order created: " + event.order.toString())
        }

        override fun handle(event: OrderUpdatedEvent) {
//            log.info("Order updated: " + event.order.toString())
        }

        override fun handle(event: OrderCancelledEvent) {
//            log.info("Order cancelled: " + event.order.toString())
        }

        override fun handle(event: TradeClosedEvent) {
            log.info("Trade closed: " + event.trade.toString())
        }
    }, null)

    @BeforeEach
    fun init() {

    }

    @Test
    fun place() {
        orderBook.place(1, OrderAction.ASK, BigDecimal.TEN, BigDecimal.valueOf(100L), OrderType.DAY)
    }

    @Test
    fun placeMany() {
        val stopWatch = StopWatch()
        stopWatch.start()

        val clients = 1..5
        for (i in 1..1_000) {
            orderBook.place(
                clients.random().toLong(),
                generateOrderAction(),
                generateBigDecimal(),
                generateBigDecimal(),
                generateOrderType()
            )
        }

        stopWatch.stop()
        log.info("Time effort millis: " + stopWatch.lastTaskTimeMillis)
    }

    @Test
    fun givenTwoCompatibleOrders_testTradePriceAndSizeAreCorrect() {
        val trades = ArrayList<Trade>()
        this.orderBook = NaiveOrderBook(object : OrderBookEventsHandler {
            override fun handle(event: OrderPlacedEvent) {
            }

            override fun handle(event: OrderUpdatedEvent) {
            }

            override fun handle(event: OrderCancelledEvent) {
            }

            override fun handle(event: TradeClosedEvent) {
                trades.add(event.trade)
            }
        }, null)

        val bid1 = orderBook.place(
            1,
            OrderAction.BID,
            BigDecimal("30"),
            BigDecimal("100"),
            OrderType.DAY
        )

        Thread.sleep(5)

        val ask1 = orderBook.place(
            2,
            OrderAction.ASK,
            BigDecimal("25"),
            BigDecimal("70"),
            OrderType.DAY
        )

        val trade = trades[0]
        assertEquals(trade.price, bid1.price)
        assertEquals(trade.size, ask1.size)

        // check ask was removed

    }

    @Test
    fun givenSetOfOrders_testMatchesAreCorrectlyIdentifiedAndClosed() {
//        TODO: Check trade prices are correct
        val trades = ArrayList<Trade>()
        this.orderBook = NaiveOrderBook(object : OrderBookEventsHandler {
            override fun handle(event: OrderPlacedEvent) {
            }

            override fun handle(event: OrderUpdatedEvent) {
            }

            override fun handle(event: OrderCancelledEvent) {
            }

            override fun handle(event: TradeClosedEvent) {
                trades.add(event.trade)
            }
        }, null)

        val clients = HashMap<String, Long>()
        clients["COSMIN"] = 1
        clients["CEZAR"] = 2
        clients["RUX"] = 3

        val bid1 = orderBook.place(
            clients["COSMIN"]!!,
            OrderAction.BID,
            BigDecimal("90"),
            BigDecimal("100"),
            OrderType.DAY
        )

        val ask2 = orderBook.place(
            clients["COSMIN"]!!,
            OrderAction.ASK,
            BigDecimal("80"),
            BigDecimal("100"),
            OrderType.DAY
        )
        // bid1 & ask2 should not match because bid1.client == ask2.client

        val ask3 = orderBook.place(
            clients["CEZAR"]!!,
            OrderAction.ASK,
            BigDecimal("91"),
            BigDecimal("100"),
            OrderType.DAY
        )
        // bid1 & ask3 should not match because bid1.price < ask3.price

        // o4
        val ask4 = orderBook.place(
            clients["CEZAR"]!!,
            OrderAction.ASK,
            BigDecimal("90"),
            BigDecimal("120"),
            OrderType.DAY
        )

        // bid1 & ask4 should match tradeSize = 100, bid.filled = true, ask.remainingSize = 20

        val bid5 = orderBook.place(
            clients["RUX"]!!,
            OrderAction.BID,
            BigDecimal("91"),
            BigDecimal("120"),
            OrderType.DAY
        )

        // bid5 & ask2 should match tradeSize = 100, bid.remaining = 20, ask.filled = true

        // bid5 & ask4 should match tradeSize = 20, bid.filled = true, ask.filled = true

        for (trade in trades) {
            log.info(
                "trade ask(id=${trade.ask.id}, price=${trade.ask.price}, size=${trade.ask.size}, client=${
                    getKeyByValue(
                        clients,
                        trade.ask.clientId
                    )
                }), " +
                        "bid(id=${trade.bid.id}, price=${trade.bid.price}, size=${trade.bid.size}, client=${
                            getKeyByValue(
                                clients,
                                trade.bid.clientId
                            )
                        }), " +
                        "tx size=${trade.size}, tx price=${trade.price}"
            )
        }
        assertEquals(3, trades.size)

        // bid1 & ask4
        val firstTrade: Trade = trades[0]
        Assertions.assertNotNull(firstTrade)
        assertEquals(bid1.id, firstTrade.bid.id)
        assertEquals(ask4.id, firstTrade.ask.id)
        assertTrue(firstTrade.size.compareTo(BigDecimal.valueOf(100)) == 0)

        // bid5 & ask2
        val secondTrade: Trade = trades[1]
        Assertions.assertNotNull(secondTrade)
        assertEquals(bid5.id, secondTrade.bid.id)
        assertEquals(ask2.id, secondTrade.ask.id)
        assertTrue(secondTrade.size.compareTo(BigDecimal.valueOf(100)) == 0)

        // bid5 & ask4
        val thirdTrade: Trade = trades[2]
        Assertions.assertNotNull(thirdTrade)
        assertEquals(bid5.id, thirdTrade.bid.id)
        assertEquals(ask4.id, thirdTrade.ask.id)
        assertTrue(thirdTrade.size.compareTo(BigDecimal.valueOf(20)) == 0)
    }

    // TODO: Implement JMH benchmarks

    private fun getKeyByValue(map: Map<String, Long>, value: Long): String? {
        val matchingKeys = map.filterValues { it == value }.keys
        if (matchingKeys.isEmpty()) {
            return null
        }
        return matchingKeys.first()
    }
}