package tech.sharply.metch.orderbook.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

class NaiveOrderBook : OrderBook {

    private val bidOrders = TreeSet<Order>()
    private val askOrders = TreeSet<Order>()
    private val ordersById = HashMap<Long, Order>()

    override fun place(order: Order): Order {
        TODO("Try to match instantly before adding it to any collection")
        ordersById[order.id] = order
        if (order.action == OrderAction.ASK) {
            askOrders.add(order)
        } else {
            bidOrders.add(order)
        }
    }

    override fun update(order: Order): Order {
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
