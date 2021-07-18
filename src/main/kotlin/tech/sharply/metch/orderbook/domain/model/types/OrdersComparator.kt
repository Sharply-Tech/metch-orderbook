package tech.sharply.metch.orderbook.domain.model.types

import tech.sharply.metch.orderbook.domain.model.orderbook.Order
import java.lang.NullPointerException
import java.util.Comparator

class OrdersComparator(private val action: OrderAction) : Comparator<Order?> {

    /**
     * Sorts the orders using Price Time Priority
     */
    private fun compareOrders(order1: Order?, order2: Order?): Int {
        if (order1 == null || order2 == null) {
            throw NullPointerException()
        }
        if (order1 == order2) {
            return 0
        }
        var result = order1.price.compareTo(order2.price)

        return if (result != 0) {
            if (action === OrderAction.BID) -result else result
        } else {
            // prices are equal => compare by last modified timestamp
            result = order1.modifiedAt.compareTo(order2.modifiedAt)
            if (result != 0) {
                result
            } else {
                // if they were modified in the same exact time
                order1.createdAt.compareTo(order2.createdAt)
            }
        }
    }

    override fun compare(o1: Order?, o2: Order?): Int {
        return compareOrders(o1, o2)
    }
}