package io.hhplus.tdd.point

import io.hhplus.tdd.common.LockManager
import io.hhplus.tdd.common.TimeUtil
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
    private val timeUtil: TimeUtil,
    private val lockManager: LockManager,
) {
    fun charge(userId: Long, amount: Long): UserPoint {
        return lockManager.withLock(userId = userId) {
            val existingUserPoint = userPointTable.selectById(id = userId)
            val totalAmount = existingUserPoint.point + amount

            if (totalAmount > MAXIMUM_POINT) {
                throw PointException.IllegalAmountChargeException("Total point must be less than $MAXIMUM_POINT.")
            }

            val updatedUserPoint = userPointTable.insertOrUpdate(id = userId, amount = totalAmount)
            pointHistoryTable.insert(id = userId, amount = amount, transactionType = TransactionType.CHARGE, updateMillis = timeUtil.getCurrentTimeInMilliSeconds())

            updatedUserPoint
        }
    }

    fun use(userId: Long, amount: Long): UserPoint {
        return lockManager.withLock(userId = userId) {
            val existingUserPoint = userPointTable.selectById(id = userId)
            val balancedAmount = existingUserPoint.point - amount

            if (balancedAmount < 0) {
                throw PointException.IllegalAmountUseException("Amount must be less than existing point.")
            }

            val updatedUserPoint = userPointTable.insertOrUpdate(id = userId, amount = balancedAmount)
            pointHistoryTable.insert(id = userId, amount = amount, transactionType = TransactionType.USE, updateMillis = timeUtil.getCurrentTimeInMilliSeconds())

            updatedUserPoint
        }

    }

    fun get(userId: Long): UserPoint {
        return lockManager.withLock(userId = userId) {
            userPointTable.selectById(id = userId)
        }
    }

    fun getHistories(userId: Long): List<PointHistory> {
        return lockManager.withLock(userId = userId) {
            pointHistoryTable.selectAllByUserId(userId = userId)
        }
    }

    companion object {
        private const val MAXIMUM_POINT = 100_000L
    }
}
