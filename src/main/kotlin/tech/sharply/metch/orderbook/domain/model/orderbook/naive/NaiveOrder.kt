package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import tech.sharply.metch.orderbook.domain.model.orderbook.Order
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
    override val filled: BigDecimal,
    override val type: OrderType,
    override val createdAt: LocalDateTime,
    override val modifiedAt: LocalDateTime,
) : Order {

    companion object {

        fun withPrice(order: NaiveOrder, price: BigDecimal): NaiveOrder {
            return NaiveOrder(
                order.id,
                order.clientId,
                order.action,
                price,
                order.size,
                order.filled,
                order.type,
                order.createdAt,
                order.modifiedAt
            )
        }

        fun withSize(order: NaiveOrder, size: BigDecimal): NaiveOrder {
            return NaiveOrder(
                order.id,
                order.clientId,
                order.action,
                order.price,
                size,
                order.filled,
                order.type,
                order.createdAt,
                order.modifiedAt
            )
        }

        fun withFilled(order: NaiveOrder, filled: BigDecimal): NaiveOrder {
            return NaiveOrder(
                order.id,
                order.clientId,
                order.action,
                order.price,
                order.size,
                filled,
                order.type,
                order.createdAt,
                order.modifiedAt
            )
        }

    }



    override fun withPrice(price: BigDecimal): NaiveOrder {
        return withPrice(this, price)
    }

    override fun withSize(size: BigDecimal): NaiveOrder {
        return withSize(this, size)
    }

    override fun withFilled(filled: BigDecimal): NaiveOrder {
        return withFilled(this, filled)
    }

    override fun toString(): String {
        return """
            NaiveOrder: {
                id: $id,
                clientId: $clientId,
                action: $action,
                price: $price,
                size: $size,
                filled: $filled,
                remaining: ${remainingSize()},
                type: $type,
                createdAt: $createdAt,
                modifiedAt: $modifiedAt
            }
        """.trimIndent()
    }

}
