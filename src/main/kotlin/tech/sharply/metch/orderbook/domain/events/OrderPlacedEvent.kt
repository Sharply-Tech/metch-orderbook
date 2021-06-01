package tech.sharply.metch.orderbook.domain.events

import tech.sharply.metch.orderbook.domain.events.base.OrderEvent
import tech.sharply.metch.orderbook.domain.model.Order

class OrderPlacedEvent(source: Any, order: Order) : OrderEvent(source, order)
