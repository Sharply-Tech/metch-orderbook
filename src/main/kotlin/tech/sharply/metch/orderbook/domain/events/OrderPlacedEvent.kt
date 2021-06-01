package tech.sharply.metch.orderbook.domain.events

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.model.Order

sealed class OrderPlacedEvent(source: Any, val order: Order) : ApplicationEvent(source)
