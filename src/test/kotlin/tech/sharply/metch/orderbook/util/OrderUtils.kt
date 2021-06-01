package tech.sharply.metch.orderbook.util

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.validation.constraints.Min

fun generateOrderAction(): OrderAction {
    val rnd = Random()

    return if (rnd.nextBoolean()) OrderAction.ASK else OrderAction.BID
}

fun generateOrderType(): OrderType {
    val rnd = Random()
    val target = rnd.nextInt(OrderType.values().size)

    for ((i, type) in OrderType.values().withIndex()) {
        if (i == target) {
            return type
        }
    }

    return OrderType.DAY
}

fun generateLong(): Long {
    return generateLong(10_000)
}

fun generateLong(bound: Int): Long {
    val rnd = Random()

    return rnd.nextInt(bound).toLong()
}

fun generateBigDecimal(@Min(1) bound: Int): BigDecimal {
    val rnd = Random()

    return BigDecimal.valueOf(rnd.nextDouble() * bound)
        .setScale(2, RoundingMode.HALF_UP)
}

fun generateBigDecimal(): BigDecimal {
    return generateBigDecimal(100)
}