package net.inherency.finances.domain.transaction

import net.inherency.finances.domain.transaction.CreditOrDebit
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals


class CreditOrDebitTest {

    private companion object {
        @Suppress("unused")
        @JvmStatic
        fun args(): Stream<Arguments> = Stream.of(
            Arguments.of("CREDIT", CreditOrDebit.CREDIT),
            Arguments.of("DEBIT", CreditOrDebit.DEBIT),
            Arguments.of("SOMETHING_UNKNOWN", CreditOrDebit.UNKNOWN),
            Arguments.of("", CreditOrDebit.UNKNOWN)
        )
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `GIVEN string "{0}" WHEN we call parse THEN we return {1}`(input: String, expectedOutput: CreditOrDebit) {
        //WHEN
        val result = CreditOrDebit.parse(input)

        //THEN
        assertEquals(expectedOutput, result)
    }
}