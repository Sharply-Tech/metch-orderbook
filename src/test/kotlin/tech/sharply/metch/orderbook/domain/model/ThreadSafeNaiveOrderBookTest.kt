package tech.sharply.metch.orderbook.domain.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal

internal class ThreadSafeNaiveOrderBookTest {

    @Test
    fun place() {
        val orderBook = ThreadSafeNaiveOrderBook()

        val order = orderBook.place(1, OrderAction.BID, BigDecimal("25"), BigDecimal("100"), OrderType.DAY)
            .get()

        assertTrue(order != null)
    }
}