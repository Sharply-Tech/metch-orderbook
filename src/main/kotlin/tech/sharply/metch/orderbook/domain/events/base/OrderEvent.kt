package tech.sharply.metch.orderbook.domain.events.base

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.model.Order

open class OrderEvent(source: Any, val order: Order) : ApplicationEvent(source)
