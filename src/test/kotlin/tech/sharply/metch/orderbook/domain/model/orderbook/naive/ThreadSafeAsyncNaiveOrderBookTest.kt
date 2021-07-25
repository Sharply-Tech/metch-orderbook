package tech.sharply.metch.orderbook.domain.model.orderbook.naive

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tech.sharply.metch.orderbook.domain.model.performance.StopWatch
import tech.sharply.metch.orderbook.domain.model.performance.ThreadTracker

import tech.sharply.metch.orderbook.domain.model.types.OrderAction
import tech.sharply.metch.orderbook.domain.model.types.OrderType
import tech.sharply.metch.orderbook.util.generateBigDecimal
import tech.sharply.metch.orderbook.util.generateOrderAction
import tech.sharply.metch.orderbook.util.generateOrderType
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class ThreadSafeAsyncNaiveOrderBookTest {

    private val orderBook = ThreadSafeAsyncNaiveOrderBook()

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
    fun placeManyOrders_differentThreads_benchmarkPerformance() {
        val threadTracker = ThreadTracker()
        val stopWatch = StopWatch()
//        TODO: The countdown latch seems to be having a lot of trouble with bigger numbers
        val ordersCount = 1000

        val threadPool = Executors.newFixedThreadPool(10)

        val countDown = CountDownLatch(ordersCount)
        val bidClients = 1..5
        val askClients = 6..10

        stopWatch.start()

        for (i in 1..ordersCount) {
            threadPool.submit {
                val orderAction = generateOrderAction()
                orderBook.place(
                    if (orderAction == OrderAction.ASK) askClients.random().toLong() else bidClients.random().toLong(),
                    generateOrderAction(),
                    generateBigDecimal(),
                    generateBigDecimal(),
                    generateOrderType()
                ).get()
                countDown.countDown()
                // track calling threads
                threadTracker.track("calling-place")
            }
        }

        countDown.await()

        println("Time effort millis: " + stopWatch.stop().toMillis())

        // print calling threads
        println("Calling threads: " + threadTracker.threadsDescription)
        // print execution threads
        println("Execution threads: " + orderBook.getThreadTracker().threadsDescription)
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
                orderBook.place(
                    clients.random().toLong(),
                    generateOrderAction(),
                    generateBigDecimal(),
                    generateBigDecimal(),
                    generateOrderType()
                ).get()
                countDown.countDown()
                // track calling threads
                threadTracker.track("calling-place")
            }
        }

        countDown.await()

        assertEquals(1, orderBook.getThreadTracker().getThreads("place").size)

        // print calling threads
        println("Calling threads: " + threadTracker.threadsDescription)
        // print execution threads
        println("Execution threads: " + orderBook.getThreadTracker().threadsDescription)
    }
}