package tech.sharply.metch.orderbook.domain.model

/**
 * https://www.investopedia.com/investing/basics-trading-stock-know-your-orders/
 */
enum class OrderType {

    MARKET,
    STOP,
    LIMIT,
    GTC,
    DAY;

}