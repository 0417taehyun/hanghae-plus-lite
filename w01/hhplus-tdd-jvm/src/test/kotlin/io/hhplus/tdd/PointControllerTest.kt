package io.hhplus.tdd

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PointControllerTest {
    @Test
    @DisplayName("Given existing user point, When passing matched user id, Then returning the matched user point successfully.")
    fun givenExistingUserPoint_whenPassingMatchedUserId_ThenReturnUserPointSuccessfully() {
        // Given
        val userId = 1L
        val point = 10_000L
        val fakeUpdateMilliseconds = 1_000L

        val pointServiceMock = mock(PointService::class.java)

        val pointController = PointController(pointServiceMock)

        `when`(pointServiceMock.get(userId = userId)).thenReturn(UserPoint(id = userId, point = point, updateMillis = fakeUpdateMilliseconds))

        // When
        val result = pointController.point(id = userId)

        // Then
        assertThat(result).isEqualTo(UserPoint(id = userId, point = point, updateMillis = fakeUpdateMilliseconds))
    }

    @Test
    @DisplayName("Given two transactions, When passing matched user id, Then returning a history of two transactions successfully.")
    fun givenExistingPointHistory_whenPassingMatchedUserId_ThenReturningListOfPointHistorySuccessfully() {
        // Given
        val userId = 1L
        val amount = 10_000L
        val fakeUpdateMilliseconds = 1_000L

        val pointServiceMock = mock(PointService::class.java)

        val pointController = PointController(pointServiceMock)

        val chargeTransaction = PointHistory(id = 1L, userId = userId, type = TransactionType.CHARGE, amount = amount, timeMillis = fakeUpdateMilliseconds)
        val useTransaction = PointHistory(id = 2L, userId = userId, type = TransactionType.USE, amount = amount, timeMillis = fakeUpdateMilliseconds)

        `when`(pointServiceMock.getHistories(userId = userId)).thenReturn(listOf(chargeTransaction, useTransaction))

        // When
        val result = pointController.history(id = userId)

        // Then
        assertThat(result).isEqualTo(listOf(chargeTransaction, useTransaction))
    }
}