package io.hhplus.tdd.common

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class UserIdReentrantLock: LockManager {
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    override fun <T> withLock(userId: Long, action: () -> T): T {
        val lock = locks.computeIfAbsent(userId) { ReentrantLock() }
        return lock.withLock { action() }
    }
}
