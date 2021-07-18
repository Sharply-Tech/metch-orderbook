package tech.sharply.metch.orderbook.domain.model.performance

import java.util.concurrent.ConcurrentHashMap
import java.util.HashSet
import java.util.function.BinaryOperator
import java.lang.StringBuilder
import kotlin.jvm.JvmOverloads
import tech.sharply.metch.orderbook.domain.model.performance.ThreadTracker

/**
 * Can be used to track active threads in an app.
 */
class ThreadTracker {
    private val threadsByKey: MutableMap<String, MutableSet<ThreadInfo>> = ConcurrentHashMap()
    val threads: Set<ThreadInfo>
        get() = threadsByKey.values.stream()
            .reduce(HashSet()) { allThreads: MutableSet<ThreadInfo>, keyThreads: Set<ThreadInfo>? ->
                allThreads.addAll(
                    keyThreads!!
                )
                allThreads
            }

    /**
     * @return Returns the set of registered threads for the specified key.
     */
    fun getThreads(key: String): MutableSet<ThreadInfo> {
        if (!threadsByKey.containsKey(key)) {
            threadsByKey[key] = ConcurrentHashMap.newKeySet()
        }
        return threadsByKey[key]!!
    }

    fun getThreadsDescription(key: String): String {
        val info = StringBuilder()
        for (thread in getThreads(key)) {
            info.append(thread.toString()).append("\n")
        }
        return info.toString()
    }

    val threadsDescription: String
        get() {
            val info = StringBuilder()
            for (key in threadsByKey.keys) {
                info.append("\n\n-----> Key: ").append(key).append("\n").append(getThreadsDescription(key))
            }
            return info.toString()
        }
    /**
     * Regiters specified thread info to the specified key.
     */
    /**
     * Registers current thread info to the specified key.
     */
    @JvmOverloads
    fun track(key: String, thread: Thread = Thread.currentThread()): ThreadInfo {
        val threadInfo = getThreadInfo(thread)
        getThreads(key).add(threadInfo)
        return threadInfo
    }

    companion object {
        fun getThreadInfo(thread: Thread): ThreadInfo {
            return ThreadInfo(thread.id, thread.name)
        }
    }
}