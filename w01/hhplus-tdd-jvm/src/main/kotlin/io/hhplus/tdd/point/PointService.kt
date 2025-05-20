package io.hhplus.tdd.point

import io.hhplus.tdd.common.TimeUtil
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
    private val timeUtil: TimeUtil
) {
    fun charge(userId: Long, amount: Long): UserPoint {
        /*** Flow
         * 1. Get existent UserPoint
         * 2. Add an amount from request
         *   2-1. If the total amount is over the policy, throw exception.
         *      - cf. We currently set the maximum point to 100,000.
         * 3. Add a history
         */
        TODO("Will be implemented in another commit.")
    }
}
