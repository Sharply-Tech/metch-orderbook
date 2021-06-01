package tech.sharply.metch.orderbook.domain.model

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.events.OrderCancelledEvent
import tech.sharply.metch.orderbook.domain.events.OrderPlacedEvent
import tech.sharply.metch.orderbook.domain.events.OrderUpdatedEvent
import tech.sharply.metch.orderbook.domain.events.TradeClosedEvent

interface OrderBookEventsHandler {

    fun handle(event: OrderPlacedEvent)

    fun handle(event: OrderUpdatedEvent)

    fun handle(event: OrderCancelledEvent)

    fun handle(event: TradeClosedEvent)

    fun handle(event: ApplicationEvent) {
        when (event) {
            is OrderPlacedEvent -> return handle(event)
            is OrderUpdatedEvent -> return handle(event)
            is OrderCancelledEvent -> return handle(event)
            is TradeClosedEvent -> return handle(event)
        }
    }
}