package tech.sharply.metch.orderbook.domain.model

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.util.generateBigDecimal
import tech.sharply.metch.orderbook.util.generateLong
import tech.sharply.metch.orderbook.util.generateOrderAction
import tech.sharply.metch.orderbook.util.generateOrderType
import java.math.BigDecimal

internal class NaiveOrderBookTest {

    private val log = LoggerFactory.getLogger(javaClass)

    private var orderBook: OrderBook = NaiveOrderBook(object : OrderBookEventsHandler {
        override fun handle(event: OrderPlacedEvent) {
            log.info("Order created: " + event.order.toString())
        }

        override fun handle(event: OrderUpdatedEvent) {
            log.info("Order updated: " + event.order.toString())
        }

        override fun handle(event: OrderCancelledEvent) {
            log.info("Order cancelled: " + event.order.toString())
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
        for (i in 1..1_000) {
            orderBook.place(generateLong(), generateOrderAction(), generateBigDecimal(),
                generateBigDecimal(), generateOrderType())
        }
    }

    // TODO: Implement JMH benchmarks
}