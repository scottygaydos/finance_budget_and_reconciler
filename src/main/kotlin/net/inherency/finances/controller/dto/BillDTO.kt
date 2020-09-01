package net.inherency.finances.controller.dto

import java.time.LocalDate

data class BillDTO (
        val billName: String,
        val dueDayOfMonth: Int,
        val lastPaymentDate: LocalDate,
        val amountPaid: Int,
        val autoPayEnabled: Boolean,
        val description: String
)