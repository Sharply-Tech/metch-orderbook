package tech.sharply.metch.orderbook.domain.model.orderbook

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.time.LocalDateTime

interface Order {

    val id: Long
    val clientId: Long
    val action: OrderAction
    val price: BigDecimal
    val size: BigDecimal
    val filled: BigDecimal
    val type: OrderType
    val createdAt: LocalDateTime
    val modifiedAt: LocalDateTime

    fun remainingSize(): BigDecimal {
        return size.subtract(filled)
    }

    fun isFilled(): Boolean {
        return filled.compareTo(size) == 0
    }

    fun withPrice(price: BigDecimal): Order

    fun withSize(size: BigDecimal): Order

    fun withFilled(filled: BigDecimal): Order
}