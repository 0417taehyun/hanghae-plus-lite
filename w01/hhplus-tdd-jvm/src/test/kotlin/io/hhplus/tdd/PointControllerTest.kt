package io.hhplus.tdd

import io.hhplus.tdd.point.PointException
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest(
    @Autowired val mvcMock: MockMvc,
) {
    @MockBean
    lateinit var pointServiceMock: PointService

    @Test
    @DisplayName("Given 100 amount, When trying to charge point, Then returning OK with charged points.")
    fun givenValidAmount_whenCharging_ThenReturnChargedPoint() {
        val userId = 1L
        val chargeAmount = 100L

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$chargeAmount}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }


    @Test
    @DisplayName("Given 0 amount, When trying to charge point, Then returning BadRequest.")
    fun givenInvalidAmount_whenChargingWithAmountLessThanZero_ThenThrowException() {
        val userId = 1L
        val chargeAmount = 0L

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$chargeAmount}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Given 100,001 amount, When trying to charge point, Then returning BadRequest.")
    fun givenInvalidAmount_whenChargingWithAmountOverMaximum_ThenThrowException() {
        val userId = 1L
        val chargeAmount = 100_001L

        `when`(pointServiceMock.charge(userId = userId, amount = chargeAmount))
            .thenThrow(PointException.IllegalAmountChargeException("Total point must be less than maximum point."))

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$chargeAmount}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }


    @Test
    @DisplayName("Given 100 amount, When trying to use point, Then returning OK with used points.")
    fun givenValidAmount_whenUsing_ThenReturnUsedPoint() {
        val userId = 1L
        val useAmount = 100L
        val fakeUpdateMilliseconds = 1_000L

        `when`(pointServiceMock.use(userId = userId, amount = useAmount))
            .thenReturn(UserPoint(id = userId, point = 0L, updateMillis = fakeUpdateMilliseconds))

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$useAmount}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }


    @Test
    @DisplayName("Given 0 amount, When trying to use point, Then returning BadRequest.")
    fun givenInvalidAmount_whenUsingWithAmountLessThanZero_ThenThrowException() {
        val userId = 1L
        val useAmount = 0L

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$useAmount}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Given 100 amount, When trying to use over existing point, Then returning BadRequest.")
    fun givenInvalidAmount_whenUsingWithAmountOverExistingPoint_ThenThrowException() {
        val userId = 1L
        val useAmount = 100L

        `when`(pointServiceMock.use(userId = userId, amount = useAmount))
            .thenThrow(PointException.IllegalAmountUseException("Amount must be less than existing point."))

        mvcMock.perform(
            MockMvcRequestBuilders.patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":$useAmount}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}