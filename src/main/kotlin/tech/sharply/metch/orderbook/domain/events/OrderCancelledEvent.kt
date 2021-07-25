package tech.sharply.metch.orderbook.domain.events

import tech.sharply.metch.orderbook.domain.events.base.OrderBookEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.Order

class OrderCancelledEvent(source: Any, val order: Order) : OrderBookEvent(source)
