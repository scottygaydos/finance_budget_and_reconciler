package net.inherency.finances.controller.dto

import java.time.LocalDate

data class PaycheckDTO (
        val date: LocalDate,
        val amount: Int
)