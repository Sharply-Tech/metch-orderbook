package tech.sharply.metch.orderbook.domain.model

import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent

interface OrderBookEventsHandler {

    fun handle(event: OrderPlacedEvent)

    fun handle(event: OrderUpdatedEvent)

    fun handle(event: OrderCancelledEvent)

}