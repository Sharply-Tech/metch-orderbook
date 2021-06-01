package tech.sharply.metch.orderbook.domain.model

import org.hibernate.validator.internal.util.stereotypes.Immutable
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

class NaiveOrderBook(private val eventsHandler: OrderBookEventsHandler) : OrderBook {

    private val bidOrders = TreeSet<Order>()
    private val askOrders = TreeSet<Order>()
    private val ordersById = HashMap<Long, Order>()

    private val orderIdSequence = AtomicLong(1)

    override fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        size: BigDecimal,
        type: OrderType
    ): Order {
        val order = ImmutableOrder(
            orderIdSequence.getAndIncrement(),
            clientId,
            action,
            price,
            size,
            type,
            LocalDateTime.now(),
            LocalDateTime.now()
        )

        ordersById[order.id] = order
        if (action == OrderAction.BID) {
            bidOrders.add(order)
        } else {
            askOrders.add(order)
        }

        eventsHandler.handle(OrderPlacedEvent(this, order))

        return order
    }

    override fun update(orderId: Long, price: BigDecimal, size: BigDecimal): Order {
        TODO("Not yet implemented")
    }

    override fun cancel(orderId: Long): Order {
        TODO("Not yet implemented")
    }
}

class ImmutableOrder(
    override val id: Long,
    override val clientId: Long,
    override val action: OrderAction,
    override val price: BigDecimal,
    override val size: BigDecimal,
    override val type: OrderType,
    override val createdAt: LocalDateTime,
    override val modifiedAt: LocalDateTime
) : Order
