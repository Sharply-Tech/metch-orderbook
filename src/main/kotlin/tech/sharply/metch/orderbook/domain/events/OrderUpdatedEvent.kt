package tech.sharply.metch.orderbook.domain.events

import tech.sharply.metch.orderbook.domain.events.base.OrderEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.Order

class OrderUpdatedEvent(source: Any, order: Order) : OrderEvent(source, order)
