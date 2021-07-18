package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent
import tech.sharply.metch.orderbook.domain.events.base.OrderEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.Order
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBook
import tech.sharply.metch.orderbook.domain.model.orderbook.OrderBookEventsHandler
import tech.sharply.metch.orderbook.domain.model.orderbook.Trade
import tech.sharply.metch.orderbook.domain.model.performance.ThreadTracker
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.domain.model.types.OrdersComparator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors
import javax.validation.constraints.DecimalMin

/**
 * Naive implementation of an OrderBook
 * Orders are grouped into two TreeSets: bids & asks, and are also indexed by their ids.
 * WARNING: This implementation is not thread safe!
 */
class NaiveOrderBook(private val eventsHandler: OrderBookEventsHandler,
                     private val threadTracker: ThreadTracker?) : OrderBook {

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
        }

        if (oppositeOrders.isEmpty()) {
            return null
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
            break
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
        // check the orders are compatible
        if (bid.price < ask.price) {
            throw IllegalArgumentException("bid.price < ask.price")
        }

        if (bid.clientId == ask.clientId) {
            throw IllegalArgumentException("the orders cannot have the same client!")
        }

        val tradeSize = bid.remainingSize().min(ask.remainingSize())

        var bidFilled = false
        var askFilled = false

        // bid fully filled
        if (bid.remainingSize() <= tradeSize) {
            // remove the bid order
            ordersById.remove(bid.id)
            bidOrders.remove(bid)
            bidFilled = true
            // fully traded
//            eventsPublisher.publishEvent(OrderClosedEvent(this, bid, trade))
        }
        // ask fully filled
        if (ask.remainingSize() <= tradeSize) {
            // remove the ask order
            ordersById.remove(ask.id)
            askOrders.remove(ask)
            askFilled = true
        }

        var remainingBid: NaiveOrder? = null;
        // bid partially filled
        if (!bidFilled) {
            remainingBid = fill(bid.id, tradeSize)
        }
        var remainingAsk: NaiveOrder? = null
        // ask partially filled
        if (!askFilled) {
            remainingAsk = fill(ask.id, tradeSize)
        }

        // the price of the trade is the price of the initiator order
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

        // emit event that order was updated and also will trigger finding the next match
        if (remainingBid != null) {
//            this.tryMatch(remainingBid)
            handle(OrderUpdatedEvent(this, remainingBid))
        } else if (remainingAsk != null) {
//            this.tryMatch(remainingAsk)
            handle(OrderUpdatedEvent(this, remainingAsk))
        }

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

        threadTracker?.track("place")

        return order
    }

    /**
     * Allows the order's price and size to be updated.
     */
    override fun update(orderId: Long, price: BigDecimal, @DecimalMin("0.0000000001") size: BigDecimal): Order? {
        if (!ordersById.containsKey(orderId)) {
            return null
        }

        val initialOrder = ordersById[orderId]!!

        val updatedOrder = initialOrder.withPrice(price)
            .withSize(size)

        ordersById[initialOrder.id] = updatedOrder
        if (updatedOrder.action == OrderAction.BID) {
            bidOrders.remove(initialOrder)
            bidOrders.add(updatedOrder)
        } else {
            askOrders.remove(initialOrder)
            askOrders.add(updatedOrder)
        }

        handle(OrderUpdatedEvent(this, updatedOrder))

        threadTracker?.track("update")

        return updatedOrder
    }

    private fun fill(orderId: Long, @DecimalMin("0.0000000001") by: BigDecimal): NaiveOrder? {
        if (!ordersById.containsKey(orderId)) {
            return null
        }

        val initialOrder = ordersById[orderId]!!

        if (initialOrder.remainingSize() < by) {
            throw IllegalArgumentException("Can only fill order: $orderId by ${initialOrder.remainingSize()}")
        }

        val updatedOrder = initialOrder.withFilled(initialOrder.filled.add(by))

        ordersById[initialOrder.id] = updatedOrder
        if (updatedOrder.action == OrderAction.BID) {
            bidOrders.remove(initialOrder)
            bidOrders.add(updatedOrder)
        } else {
            askOrders.remove(initialOrder)
            askOrders.add(updatedOrder)
        }

        // The OrderUpdatedEvent is not emitted here to not trigger finding the next match before the current one
        // is emitted. It will cause incorrect final trades order.
//        handle(OrderUpdatedEvent(this, updatedOrder))

        threadTracker?.track("fill")

        return updatedOrder as NaiveOrder
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

        threadTracker?.track("cancel")

        return order
    }

    override fun findById(orderId: Long): Order? {
        threadTracker?.track("findById")

        return ordersById[orderId]
    }

    override fun findBestBids(count: Long): Collection<Order> {
        threadTracker?.track("findBestBids")

        return bidOrders.stream()
            .filter { order -> order != null }
            .map { order -> order!! }
            .limit(count)
            .collect(Collectors.toList())
    }

    override fun findBestAsks(count: Long): Collection<Order> {
        threadTracker?.track("findBestAsks")

        return askOrders.stream()
            .filter { order -> order != null }
            .map { order -> order!! }
            .limit(count)
            .collect(Collectors.toList())
    }

    /**
     * handling internal events is performed sync to ensure that when one
     */
    private fun handle(event: ApplicationEvent) {
        eventsHandler.handle(event)

        // trigger try match on order events
        if (event is OrderEvent && event !is OrderCancelledEvent) {
            this.tryMatch(event.order as NaiveOrder)
        }
    }
}

