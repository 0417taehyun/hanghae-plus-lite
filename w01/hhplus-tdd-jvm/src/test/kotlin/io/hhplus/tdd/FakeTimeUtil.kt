package io.hhplus.tdd

import io.hhplus.tdd.common.TimeUtil

class FakeTimeUtil(
    private val fixedTime: Long
): TimeUtil {
    override fun getCurrentTimeInMilliSeconds(): Long {
        return fixedTime
    }
}
