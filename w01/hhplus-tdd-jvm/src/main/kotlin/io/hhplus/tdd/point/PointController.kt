package io.hhplus.tdd.point

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointService,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return pointService.get(userId = id)
    }

    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointService.getHistories(userId = id)
    }

    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody @Valid chargeRequest: ChargeRequest,
    ): UserPoint {
        return pointService.charge(userId = id, amount = chargeRequest.amount)
    }

    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody @Valid useRequest: UseRequest,
    ): UserPoint {
        return pointService.use(userId = id, amount = useRequest.amount)
    }

    data class ChargeRequest(
        @field:Positive(message = "충전하려는 포인트는 0보다 커야 합니다.")
        val amount: Long,
    )

    data class UseRequest(
        @field:Positive(message = "사용하려는 포인트는 0보다 커야 합니다.")
        val amount: Long,
    )
}