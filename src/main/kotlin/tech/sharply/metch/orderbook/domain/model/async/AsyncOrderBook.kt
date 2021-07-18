package tech.sharply.metch.orderbook.domain.model.async

import tech.sharply.metch.orderbook.domain.model.Order
import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

interface AsyncOrderBook {

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
    ): CompletableFuture<Order>

    /**
     * Updates the order's price and size.
     * @see Order
     */
    fun update(
        orderId: Long,
        price: BigDecimal,
        size: BigDecimal,
    ): CompletableFuture<Order?>

    /**
     * Cancels an active order, identifying it by it's id.
     * @see Order
     */
    fun cancel(orderId: Long): CompletableFuture<Order?>

    fun findById(orderId: Long): CompletableFuture<Order?>

    /**
     * Finds the best [count] bid orders.
     */
    fun findBestBids(count: Long): CompletableFuture<Collection<Order>>

    /**
     * Finds the best [count] ask orders.
     */
    fun findBestAsks(count: Long): CompletableFuture<Collection<Order>>

}