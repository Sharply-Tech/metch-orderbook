package tech.sharply.metch.orderbook.domain.model

interface OrderBook {

    fun place(order: Order): Order

    fun update(order: Order): Order

    fun cancel(orderId: Long): Order

}
