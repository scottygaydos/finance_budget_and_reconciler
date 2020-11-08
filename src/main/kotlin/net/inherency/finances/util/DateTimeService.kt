package net.inherency.finances.util

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class DateTimeService(private val clock: Clock = Clock.systemDefaultZone()) {

    fun now(): LocalDate {
        return LocalDate.now(clock)
    }

    fun fromYearAndMonth(year: Int, month: Int): LocalDate {
        return LocalDate.of(year, month, 1)
    }

}