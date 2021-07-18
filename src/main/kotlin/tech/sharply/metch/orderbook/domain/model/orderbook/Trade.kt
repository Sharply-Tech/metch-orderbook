package tech.sharply.metch.orderbook.domain.model.orderbook

import java.math.BigDecimal
import java.time.LocalDateTime

interface Trade {

    val bid: Order
    val ask: Order
    val price: BigDecimal
    val size: BigDecimal
    val createdAt: LocalDateTime

}