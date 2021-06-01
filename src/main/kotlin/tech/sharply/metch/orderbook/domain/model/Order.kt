package tech.sharply.metch.orderbook.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

interface Order {

    val id: Long
    val clientId: Long
    val action: OrderAction
    val price: BigDecimal
    val size: BigDecimal
    val type: OrderType
    val createdAt: LocalDateTime
    val modifiedAt: LocalDateTime

}