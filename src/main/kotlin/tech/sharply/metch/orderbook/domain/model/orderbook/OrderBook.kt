package tech.sharply.metch.orderbook.domain.model.orderbook

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal

interface OrderBook {

    /**
     * Generates and places the order for the specified data
     * @see Order
     */
    fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        size: BigDecimal,
        type: OrderType,
    ): Order

    /**
     * Updates the order's price and size.
     * @see Order
     */
    fun update(
        orderId: Long,
        price: BigDecimal,
        size: BigDecimal,
    ): Order?

    /**
     * Cancels an active order, identifying it by it's id.
     * @see Order
     */
    fun cancel(orderId: Long): Order?

    fun findById(orderId: Long): Order?

    /**
     * Finds the best [count] bid orders.
     */
    fun findBestBids(count: Long): Collection<Order>

    /**
     * Finds the best [count] ask orders.
     */
    fun findBestAsks(count: Long): Collection<Order>
}
