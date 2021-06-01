package tech.sharply.metch.orderbook.domain.model

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
import tech.sharply.metch.orderbook.domain.events.base.OrderEvent
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.domain.model.types.OrdersComparator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.validation.constraints.DecimalMin

/**
 * Naive implementation of an OrderBook
 * Orders are grouped into two TreeSets: bids & asks, and are also indexed by their ids.
 */
class NaiveOrderBook(private val eventsHandler: OrderBookEventsHandler) : OrderBook {

    private val bidOrders = TreeSet(OrdersComparator(OrderAction.BID))
    private val askOrders = TreeSet(OrdersComparator(OrderAction.ASK))
    private val ordersById = HashMap<Long, Order>()

    private val orderIdSequence = AtomicLong(1)

    /**
     * Checks if the order can be matched instantly, and, if so, returns the trade
     */
    private fun tryMatch(order: NaiveOrder): Trade? {
        var oppositeOrders: Set<Order?> = askOrders
        if (order.action == OrderAction.ASK) {
            oppositeOrders = bidOrders
            // TODO: Look for a compatible bid order
        }

        var match: Order? = null

        for (possibleMatch in oppositeOrders) {
            if (possibleMatch == null) {
                continue
            }
            if (possibleMatch.isFilled()) {
                continue
            }
            if (possibleMatch.clientId == order.clientId) {
                continue
            }
            // check price compatibility
            if (order.action == OrderAction.BID && order.price < possibleMatch.price) {
                continue
            }
            if (order.action == OrderAction.ASK && order.price > possibleMatch.price) {
                continue
            }
            match = possibleMatch
        }

        if (match == null) {
            // the order couldn't have been matched instantly so we return it unchanged
            return null
        }

        val bid = if (order.action == OrderAction.BID) order else match
        val ask = if (order.action == OrderAction.ASK) order else match

        return this.processTrade(bid, ask)
    }

    /**
     * Processes a trade formed by two orders.
     * If the orders are not compatible then an appropriate {@link IllegalArgumentException} is thrown.
     */
    private fun processTrade(bid: Order, ask: Order): Trade {
        // check the bid exists
//        val bid = ordersById[bidId]
//        if (bid == null || bid.action != OrderAction.BID) {
//            throw IllegalArgumentException("No bid found for id: $bidId")
//        }
//
//        // check the ask exists
//        val ask = ordersById[askId]
//        if (ask == null || ask.action != OrderAction.BID) {
//            throw IllegalArgumentException("No ask found for id: $askId")
//        }

        // check they are compatible
        if (bid.price < ask.price) {
            throw IllegalArgumentException("bid.price < ask.price")
        }

        if (bid.clientId == ask.clientId) {
            throw IllegalArgumentException("the orders cannot have the same client!")
        }

        val tradeSize = bid.size.min(ask.size)

        var bidFilled = false
        var askFilled = false

        if (bid.size <= tradeSize) {
            // remove the bid order
            ordersById.remove(bid.id)
            bidOrders.remove(bid)
            bidFilled = true
            // fully traded
//            eventsPublisher.publishEvent(OrderClosedEvent(this, bid, trade))
        }
        if (ask.size <= tradeSize) {
            // remove the ask order
            ordersById.remove(ask.id)
            askOrders.remove(ask)
            askFilled = true
        }

        if (!bidFilled) {
            this.update(bid.id, bid.price, bid.size.subtract(tradeSize))
        }
        if (!askFilled) {
            this.update(ask.id, ask.price, ask.size.subtract(tradeSize))
        }

//        val tradePrice = if (bid.modifiedAt.isBefore(ask.modifiedAt)) bid.price else ask.price
        val tradePrice = if (bid.modifiedAt.isBefore(ask.modifiedAt)) bid.price else ask.price


        val trade = NaiveTrade(
            bid,
            ask,
            tradePrice,
            tradeSize,
            LocalDateTime.now()
        )

        // emit the event
        handle(TradeClosedEvent(this, trade))

        return trade
    }

    /**
     * Generates a NaiveOrder for the specified data and places it into the correct set.
     * Also, before adding the order to the set the order is verified for any possible matches.
     */
    override fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        @DecimalMin("0.0000000001") size: BigDecimal,
        type: OrderType
    ): Order {
        val order = NaiveOrder(
            orderIdSequence.getAndIncrement(),
            clientId,
            action,
            price,
            size,
            BigDecimal.ZERO,
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

        handle(OrderPlacedEvent(this, order))

        return order
    }

    /**
     * Allows the order's price and size to be updated.
     */
    override fun update(orderId: Long, price: BigDecimal, @DecimalMin("0.0000000001") size: BigDecimal): Order? {
        if (!ordersById.containsKey(orderId)) {
            return null
        }

        val order = ordersById[orderId]!!

        val updatedOrder = order.withPrice(price)
            .withSize(size)

        ordersById[order.id] = updatedOrder
        if (updatedOrder.action == OrderAction.BID) {
            bidOrders.add(updatedOrder)
        } else {
            askOrders.add(updatedOrder)
        }

        handle(OrderUpdatedEvent(this, order))

        return updatedOrder
    }

    /**
     * Cancels an active order.
     * If the order is not found then an IllegalArgumentException is thrown.
     */
    override fun cancel(orderId: Long): Order? {
        if (!ordersById.containsKey(orderId)) {
            return null
        }

        val order = ordersById[orderId]

        if (order!!.action == OrderAction.BID) {
            bidOrders.remove(order)
        } else {
            askOrders.remove(order)
        }
        ordersById.remove(orderId)

        handle(OrderCancelledEvent(this, order))

        return order
    }

    override fun findById(orderId: Long): Order? {
        return ordersById[orderId]
    }

    private fun handle(event: ApplicationEvent) {
        eventsHandler.handle(event)

        // trigger try match on order events
        if (event is OrderEvent) {
            this.tryMatch(event.order as NaiveOrder)
        }
    }
}

