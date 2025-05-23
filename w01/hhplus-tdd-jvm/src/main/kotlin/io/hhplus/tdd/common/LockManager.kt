package io.hhplus.tdd.common

interface LockManager {
    fun <T> withLock(userId: Long, action: () -> T): T
}