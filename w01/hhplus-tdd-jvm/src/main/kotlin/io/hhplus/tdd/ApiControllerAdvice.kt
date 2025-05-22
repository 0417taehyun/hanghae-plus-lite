package io.hhplus.tdd

import io.hhplus.tdd.point.PointException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorMessage = ex.bindingResult
            .fieldErrors
            .map {
                mapOf(
                    "field" to it.field,
                    "details" to (it.defaultMessage ?: "유효하지 않은 필드입니다.")
                )
            }

        return ResponseEntity(mapOf("code" to "400", "message" to errorMessage), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(PointException.IllegalAmountChargeException::class)
    fun handleIllegalAmountChargeException(exception: PointException.IllegalAmountChargeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "최대 금액을 넘어서는 충전을 시도했습니다."),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(PointException.IllegalAmountUseException::class)
    fun handleIllegalAmountUseException(exception: PointException.IllegalAmountUseException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "보유 중인 금액을 넘어서는 사용을 시도했습니다."),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}