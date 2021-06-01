package tech.sharply.metch.orderbook.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class NaiveTrade(
    override val bid: Order,
    override val ask: Order,
    override val price: BigDecimal,
    override val size: BigDecimal,
    override val createdAt: LocalDateTime
) : Trade {

    override fun toString(): String {
        return """
            NaiveTrade: {
                bid: $bid,
                ask: $ask,
                price: $price,
                size: $size,
                createdAt: $createdAt
            }
        """.trimIndent()
    }

}