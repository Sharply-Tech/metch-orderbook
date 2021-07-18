package tech.sharply.metch.orderbook.domain.model

import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.validation.constraints.DecimalMin

class ThreadSafeNaiveOrderBook {

    private var orderBook: OrderBook = NaiveOrderBook(object : OrderBookEventsHandler {
        override fun handle(event: OrderPlacedEvent) {
        }

        override fun handle(event: OrderUpdatedEvent) {
        }

        override fun handle(event: OrderCancelledEvent) {
        }

        override fun handle(event: TradeClosedEvent) {
        }
    })

    private val executor = Executors.newFixedThreadPool(1);

    fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        @DecimalMin("0.0000000001") size: BigDecimal,
        type: OrderType
    ): CompletableFuture<Order> {
//        return executor.submit { orderBook.place(clientId, action, price, size, type) } as Future<Order>
        return CompletableFuture.supplyAsync({orderBook.place(clientId, action, price, size, type)}, executor)
    }


}