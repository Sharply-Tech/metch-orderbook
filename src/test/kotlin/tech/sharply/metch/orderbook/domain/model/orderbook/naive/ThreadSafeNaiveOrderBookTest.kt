package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import tech.sharply.metch.orderbook.domain.model.performance.ThreadTracker

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.util.generateBigDecimal
import tech.sharply.metch.orderbook.util.generateOrderAction
import tech.sharply.metch.orderbook.util.generateOrderType
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class ThreadSafeNaiveOrderBookTest {

    private val log = LoggerFactory.getLogger(javaClass)
    private val orderBook = ThreadSafeNaiveOrderBook()

    @Test
    fun place() {
        val order = orderBook.place(1, OrderAction.BID, BigDecimal("25"), BigDecimal("100"), OrderType.DAY)
            .get()

        assertTrue(order != null)
    }

    /**
     * Measures the time of adding a lot of orders from different threads.
     * Clients stick to one order action to simulate a real scenario better.
     */
    @Test
    fun placeManyOrders_differentThreads() {
        val stopWatch = StopWatch()

        val ordersCount = 20_000

        val threadPool = Executors.newFixedThreadPool(10)

        val countDown = CountDownLatch(ordersCount)
        val bidClients = 1..5
        val askClients = 6..10

        stopWatch.start()

        for (i in 1..ordersCount) {
            threadPool.submit {
                val orderAction = generateOrderAction();
                orderBook.place(
                    if (orderAction == OrderAction.ASK) askClients.random().toLong() else bidClients.random().toLong(),
                    generateOrderAction(),
                    generateBigDecimal(),
                    generateBigDecimal(),
                    generateOrderType()
                ).get()
                countDown.countDown()
            }
        }

        countDown.await()

        stopWatch.stop()
        log.info("Time effort millis: " + stopWatch.lastTaskTimeMillis)

        // log active threads
        log.info(orderBook.getThreadTracker().threadsDescription)
    }

    @Test
    fun checkSystemEnvironment() {
        log.info(System.getenv("GITHUB_USERNAME"))
        log.info(System.getenv("GITHUB_PACKAGES_REPOSITORY_TOKEN"))
    }

    @Test
    fun testActiveThreads() {
        val threadTracker = ThreadTracker()

        val ordersCount = 1000
        val clients = 1..5

        val callingExecutor = Executors.newFixedThreadPool(10)

        val countDown = CountDownLatch(ordersCount)

        for (i in 1..ordersCount) {
            callingExecutor.submit {
                // track calling threads
                threadTracker.track("calling-place")

                orderBook.place(
                    clients.random().toLong(),
                    generateOrderAction(),
                    generateBigDecimal(),
                    generateBigDecimal(),
                    generateOrderType()
                ).get()
                countDown.countDown()
            }
        }

        countDown.await()

        assertEquals(1, orderBook.getThreadTracker().getThreads("place").size)

        // print calling threads
        log.info("Calling threads: " + threadTracker.threadsDescription)
        // print execution threads
        log.info("Execution threads: " + orderBook.getThreadTracker().threadsDescription)

    }
}