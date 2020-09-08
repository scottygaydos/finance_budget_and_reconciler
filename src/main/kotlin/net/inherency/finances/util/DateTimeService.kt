package net.inherency.finances.util

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DateTimeService {

    fun now(): LocalDate {
        return LocalDate.now()
    }

}