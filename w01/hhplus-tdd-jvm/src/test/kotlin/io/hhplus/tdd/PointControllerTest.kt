package io.hhplus.tdd

import io.hhplus.tdd.point.PointController
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.UserPoint
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
}