package io.hhplus.tdd

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class PointServiceTest {
    @Test
    @DisplayName("Given 10,000 existing points, When charging 90,000 points, Then total becoming 100,000 points successfully.")
    fun givenExistingPoints_whenChargingPoints_ThenAddingPointsSuccessfully() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 90_000L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)

        val pointService = PointService(userPointTable = userPointTable, pointHistoryTable = pointHistoryTableMock, timeUtil = fakeTimeUtil)

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))
        `when`(userPointTable.insertOrUpdate(id = userId, amount = existingPoint + amount))
            .thenReturn(UserPoint(id = userId, point = existingPoint + amount, updateMillis = fakeUpdateMilliseconds))
        `when`(
            pointHistoryTableMock.insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.CHARGE,
                updateMillis = fakeUpdateMilliseconds
            )
        ).thenReturn(
            PointHistory(
                id = 1L,
                userId = userId,
                amount = amount,
                type = TransactionType.CHARGE,
                timeMillis = fakeUpdateMilliseconds
            )
        )

        // When
        val result = pointService.charge(userId = userId, amount = amount)

        // Then
        assertThat(result)
            .isEqualTo(UserPoint(id = userId, point = existingPoint + amount, updateMillis = fakeUpdateMilliseconds))

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, times(1)).insertOrUpdate(id = userId, amount = existingPoint + amount)
        verify(pointHistoryTableMock, times(1))
            .insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.CHARGE,
                updateMillis = fakeUpdateMilliseconds
            )
    }

    @Test
    @DisplayName("Given 10,000 existing points, When charging 90,001 points, Then throw IllegalAmountChargeException.")
    fun givenExistingPoints_whenChargingPointsOverMaximum_ThenThrowException() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 90_001L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)

        val pointService = PointService(userPointTable = userPointTable, pointHistoryTable = pointHistoryTableMock, timeUtil = fakeTimeUtil)

        `when`(userPointTable.selectById(id = userId)).
        thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))

        // When
        // Then
        assertThrows<PointException.IllegalAmountChargeException> {
            pointService.charge(userId = userId, amount = amount)
        }

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, never()).insertOrUpdate(id = userId, amount = existingPoint + amount)
        verify(pointHistoryTableMock, never()).insert(id = userId, amount = amount, transactionType = TransactionType.CHARGE, updateMillis = fakeUpdateMilliseconds)
    }

    @Test
    @DisplayName("Given 10,000 existing points, When using 10,000 points, Then left point becoming 0 successfully.")
    fun givenExistingPoints_whenUsingPoints_ThenBalancingPointsSuccessfully() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 10_000L
        val fakeUpdateMilliseconds = 1000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)

        val pointService = PointService(userPointTable = userPointTable, pointHistoryTable = pointHistoryTableMock, timeUtil = fakeTimeUtil)

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))
        `when`(userPointTable.insertOrUpdate(id = userId, amount = existingPoint - amount))
            .thenReturn(UserPoint(id = userId, point = existingPoint - amount, updateMillis = fakeUpdateMilliseconds))
        `when`(
            pointHistoryTableMock.insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.USE,
                updateMillis = fakeUpdateMilliseconds
            )
        ).thenReturn(
            PointHistory(
                id = 1L,
                userId = userId,
                amount = amount,
                type = TransactionType.USE,
                timeMillis = fakeUpdateMilliseconds
            )
        )

        // When
        val result = pointService.use(userId = userId, amount = amount)

        // Then
        assertThat(result)
            .isEqualTo(UserPoint(id = userId, point = existingPoint - amount, updateMillis = fakeUpdateMilliseconds))

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, times(1)).insertOrUpdate(id = userId, amount = existingPoint - amount)
        verify(pointHistoryTableMock, times(1))
            .insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.USE,
                updateMillis = fakeUpdateMilliseconds
            )
    }
}