package tech.sharply.metch.orderbook.domain.events

import org.springframework.context.ApplicationEvent
import tech.sharply.metch.orderbook.domain.model.Trade

class TradeClosedEvent(source: Any, val trade: Trade) : ApplicationEvent(source)