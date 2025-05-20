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
        val existingUserPoint = userPointTable.selectById(id = userId)
        val totalAmount = existingUserPoint.point + amount

        if (totalAmount > MAXIMUM_POINT) {
            throw PointException.IllegalAmountChargeException("Total point must be less than $MAXIMUM_POINT.")
        }

        val updatedUserPoint = userPointTable.insertOrUpdate(id = userId, amount = totalAmount)
        pointHistoryTable.insert(id = userId, amount = amount, transactionType = TransactionType.CHARGE, updateMillis = timeUtil.getCurrentTimeInMilliSeconds())

        return updatedUserPoint
    }

    companion object {
        private const val MAXIMUM_POINT = 100_000L
    }
}
