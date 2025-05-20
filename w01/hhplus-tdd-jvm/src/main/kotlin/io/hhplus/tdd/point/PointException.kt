package io.hhplus.tdd.point

sealed class PointException(message: String): RuntimeException(message) {
    class IllegalAmountChargeException(message: String): PointException(message = message)
    class IllegalAmountUseException(message: String): PointException(message = message)
}
