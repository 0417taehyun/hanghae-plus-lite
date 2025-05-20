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

    fun use(userId: Long, amount: Long): UserPoint {
        val existingUserPoint = userPointTable.selectById(id = userId)
        val balancedAmount = existingUserPoint.point - amount

        if (balancedAmount < 0) {
            throw PointException.IllegalAmountUseException("Amount must be less than existing point.")
        }

        val updatedUserPoint = userPointTable.insertOrUpdate(id = userId, amount = balancedAmount)
        pointHistoryTable.insert(id = userId, amount = amount, transactionType = TransactionType.USE, updateMillis = timeUtil.getCurrentTimeInMilliSeconds())

        return updatedUserPoint
    }

    fun get(userId: Long): UserPoint {
        /*** Flow
         * - Get UserPoint by using usrId
         */
        TODO("Will be implemented in another commit")
    }

    companion object {
        private const val MAXIMUM_POINT = 100_000L
    }
}
