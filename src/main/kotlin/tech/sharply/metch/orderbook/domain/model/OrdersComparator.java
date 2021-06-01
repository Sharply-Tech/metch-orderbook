package tech.sharply.metch.orderbook.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Comparator;

@AllArgsConstructor
@Getter
public class OrdersComparator implements Comparator<Order> {

	@NonNull
	private final OrderAction action;

	/**
	 * Sorts the orders using Price Time Priority
	 */
	private int compareOrders(Order order1, Order order2) {
		if (order1 == null || order2 == null) {
			throw new NullPointerException();
		}
		if (order1 == order2 || order1.equals(order2)) {
			return 0;
		}
		int result = order1.getPrice().compareTo(order2.getPrice());

		if (result != 0) {
			return (action == OrderAction.BID ? -result : result);
		} else {
			// prices are equal => compare by last modified timestamp
			result = order1.getModifiedAt().compareTo(order2.getModifiedAt());
			if (result != 0) {
				return result;
			} else {
				// if they were modified in the same exact time
				return order1.getCreatedAt().compareTo(order2.getCreatedAt());
			}
		}
	}

	@Override
	public int compare(Order o1, Order o2) {
		return compareOrders(o1, o2);
	}

}
