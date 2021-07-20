package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.naive.NaiveOrderBook
import tech.sharply.metch.orderbook.domain.model.orderbook.Order
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBook
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBookEventsHandler
import tech.sharply.metch.orderbook.domain.model.orderbook.async.AsyncOrderBook
import tech.sharply.metch.orderbook.domain.model.performance.ThreadTracker
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import javax.validation.constraints.DecimalMin

/**
 * This order book internally uses the {@link NaiveOrderBook} and ensures thread safety by restricting all
 * writes to a single thread. </br>
 * Multiple threads can read data without affecting thread safety.
 */
class ThreadSafeAsyncNaiveOrderBook : AsyncOrderBook {

    private val threadTracker = ThreadTracker()

    private val orderBook: OrderBook = NaiveOrderBook(object : OrderBookEventsHandler {
        override fun handle(event: OrderPlacedEvent) {
        }

        override fun handle(event: OrderUpdatedEvent) {
        }

        override fun handle(event: OrderCancelledEvent) {
        }

        override fun handle(event: TradeClosedEvent) {
        }
    }, threadTracker)

    private val executor = Executors.newFixedThreadPool(1)

    override fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        @DecimalMin("0.0000000001") size: BigDecimal,
        type: OrderType
    ): CompletableFuture<Order> {
//        return executor.submit { orderBook.place(clientId, action, price, size, type) } as Future<Order>
        return CompletableFuture.supplyAsync({ orderBook.place(clientId, action, price, size, type) }, executor)
    }

    override fun update(orderId: Long, price: BigDecimal, size: BigDecimal): CompletableFuture<Order?> {
        return CompletableFuture.supplyAsync({ orderBook.update(orderId, price, size) }, executor)
    }

    override fun cancel(orderId: Long): CompletableFuture<Order?> {
        return CompletableFuture.supplyAsync({ orderBook.cancel(orderId) }, executor)
    }

    override fun findById(orderId: Long): CompletableFuture<Order?> {
        return CompletableFuture.supplyAsync { orderBook.findById(orderId) }
    }

    override fun findBestBids(count: Long): CompletableFuture<Collection<Order>> {
        return CompletableFuture.supplyAsync { orderBook.findBestBids(count) }
    }

    override fun findBestAsks(count: Long): CompletableFuture<Collection<Order>> {
        return CompletableFuture.supplyAsync { orderBook.findBestAsks(count) }
    }

    fun getThreadTracker(): ThreadTracker {
        return threadTracker
    }

}