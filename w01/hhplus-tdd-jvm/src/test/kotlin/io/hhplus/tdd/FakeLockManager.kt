package io.hhplus.tdd

import io.hhplus.tdd.common.LockManager

class FakeLockManager: LockManager {
    override fun <T> withLock(userId: Long, action: () -> T): T {
        return action()
    }
}
