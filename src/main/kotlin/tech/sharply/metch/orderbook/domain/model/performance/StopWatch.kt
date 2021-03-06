package tech.sharply.metch.orderbook.domain.model.performance

import java.time.Duration
import java.time.LocalDateTime

class StopWatch {

    lateinit var start: LocalDateTime

    fun start() {
        this.start = LocalDateTime.now()
    }

    fun stop(): Duration {
        return Duration.between(this.start, LocalDateTime.now())
    }

}