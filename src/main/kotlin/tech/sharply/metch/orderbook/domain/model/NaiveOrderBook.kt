package tech.sharply.metch.orderbook.domain.model

import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.domain.model.types.OrdersComparator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class NaiveOrderBook(private val eventsHandler: OrderBookEventsHandler) : OrderBook {

    private val bidOrders = TreeSet(OrdersComparator(OrderAction.BID))
    private val askOrders = TreeSet(OrdersComparator(OrderAction.ASK))
    private val ordersById = HashMap<Long, Order>()

    private val orderIdSequence = AtomicLong(1)

    override fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        size: BigDecimal,
        type: OrderType
    ): Order {
        val order = NaiveOrder(
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
        if (!ordersById.containsKey(orderId)) {
            throw IllegalArgumentException("Order not found for orderId: $orderId");
        }

        val order = ordersById[orderId]!!

        val updatedOrder = NaiveOrder(
            order.id,
            order.clientId,
            order.action,
            price,
            size,
            order.type,
            order.createdAt,
            order.modifiedAt
        )

        ordersById[order.id] = updatedOrder
        if (updatedOrder.action == OrderAction.BID) {
            bidOrders.add(updatedOrder)
        } else {
            askOrders.add(updatedOrder)
        }

        eventsHandler.handle(OrderUpdatedEvent(this, order))

        return updatedOrder
    }

    override fun cancel(orderId: Long): Order {
        if (!ordersById.containsKey(orderId)) {
            throw IllegalArgumentException("Order not found for orderId: $orderId");
        }

        val order = ordersById[orderId]

        if (order!!.action == OrderAction.BID) {
            bidOrders.remove(order)
        } else {
            askOrders.remove(order)
        }
        ordersById.remove(orderId)

        eventsHandler.handle(OrderPlacedEvent(this, order))

        return order
    }
}

