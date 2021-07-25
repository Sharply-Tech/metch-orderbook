package tech.sharply.metch.orderbook.domain.events

import tech.sharply.metch.orderbook.domain.events.base.OrderBookEvent
import tech.sharply.metch.orderbook.domain.model.orderbook.Trade

class TradeClosedEvent(source: Any, val trade: Trade) : OrderBookEvent(source)