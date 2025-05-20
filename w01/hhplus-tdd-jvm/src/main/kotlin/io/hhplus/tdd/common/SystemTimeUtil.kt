package io.hhplus.tdd.common

import org.springframework.stereotype.Component

@Component
class SystemTimeUtil: TimeUtil {
    override fun getCurrentTimeInMilliSeconds(): Long {
        return System.currentTimeMillis()
    }
}
