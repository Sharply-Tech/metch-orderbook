package tech.sharply.metch.orderbook.domain.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
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
    })

    @BeforeEach
    fun init() {

    }

    @Test
    fun place() {
        orderBook.place(1, OrderAction.ASK, BigDecimal.TEN, BigDecimal.valueOf(100L), OrderType.DAY)
    }

    @Test
    fun placeMany() {
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
        })

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
        val trades = ArrayList<Trade>()
        this.orderBook = NaiveOrderBook(object : OrderBookEventsHandler {
            override fun handle(event: OrderPlacedEvent) {
            }

            override fun handle(event: OrderUpdatedEvent) {
            }

            override fun handle(event: OrderCancelledEvent) {
            }

            override fun handle(event: TradeClosedEvent) {
                log.info("Trade closed: " + event.trade.toString())
            }
        })

        val clients = HashMap<String, Long>()
        clients["COSMIN"] = 1
        clients["CEZAR"] = 2
        clients["RUX"] = 3

        // o1
        val bid1 = orderBook.place(
            clients["COSMIN"]!!,
            OrderAction.BID,
            BigDecimal("90"),
            BigDecimal("100"),
            OrderType.DAY
        )

        // o2
        val ask1 = orderBook.place(
            clients["COSMIN"]!!,
            OrderAction.ASK,
            BigDecimal("80"),
            BigDecimal("100"),
            OrderType.DAY
        )
        // bid1 & ask1 should not match because bid1.client == ask1.client

        // o3
        val ask2 = orderBook.place(
            clients["CEZAR"]!!,
            OrderAction.ASK,
            BigDecimal("91"),
            BigDecimal("100"),
            OrderType.DAY
        )
        // bid1 & ask2 should not match because bid1.price < ask2.price

        // o4
        val ask3 = orderBook.place(
            clients["CEZAR"]!!,
            OrderAction.ASK,
            BigDecimal("90"),
            BigDecimal("120"),
            OrderType.DAY
        )

        // bid1 & ask3 should match

        val bid2 = orderBook.place(
            clients["RUX"]!!,
            OrderAction.BID,
            BigDecimal("81"),
            BigDecimal("120"),
            OrderType.DAY
        )

        assertEquals(2, trades.size)

        // bid2 & ask1 should match

        // bid1 & ask3
        val firstTrade: Trade = trades[0]
        Assertions.assertNotNull(firstTrade)
        assertEquals(bid1.id, firstTrade.bid.id)
        assertEquals(ask3.id, firstTrade.ask.id)

        // bid2 & ask1
        val secondTrade: Trade = trades[1]
        Assertions.assertNotNull(firstTrade)
        assertEquals(bid2.id, secondTrade.bid.id)
        assertEquals(ask1.id, secondTrade.ask.id)
    }

    // TODO: Implement JMH benchmarks
}