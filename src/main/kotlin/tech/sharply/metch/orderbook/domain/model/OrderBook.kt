package tech.sharply.metch.orderbook.domain.model

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal

interface OrderBook {

    fun place(
        clientId: Long,
        action: OrderAction,
        price: BigDecimal,
        size: BigDecimal,
        type: OrderType,
    ): Order

    /**
     * Only the order's price or quantity can be updated
     */
    fun update(
        orderId: Long,
        price: BigDecimal,
        size: BigDecimal,
    ): Order

    fun cancel(orderId: Long): Order

}
