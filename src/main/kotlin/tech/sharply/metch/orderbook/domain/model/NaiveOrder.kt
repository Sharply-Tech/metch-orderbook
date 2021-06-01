package tech.sharply.metch.orderbook.domain.model

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.time.LocalDateTime


data class NaiveOrder(
    override val id: Long,
    override val clientId: Long,
    override val action: OrderAction,
    override val price: BigDecimal,
    override val size: BigDecimal,
    override val type: OrderType,
    override val createdAt: LocalDateTime,
    override val modifiedAt: LocalDateTime
) : Order {

    override fun toString(): String {
        return """
            ImmutableOrder: {
                id: $id,
                clientId: $clientId,
                action: $action,
                price: $price,
                size: $size,
                type: $type,
                createdAt: $createdAt,
                modifiedAt: $modifiedAt
            }
        """.trimIndent()
    }

}
