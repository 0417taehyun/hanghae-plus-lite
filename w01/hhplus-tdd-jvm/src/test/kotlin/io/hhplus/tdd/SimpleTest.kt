package io.hhplus.tdd

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleTest {
    @Test
    fun one_plus_one_must_be_two() {
        //given
        val one = 1
        val two = 1

        // when
        val result = one + two

        // then
        assertThat(result).isEqualTo(2)
    }
}