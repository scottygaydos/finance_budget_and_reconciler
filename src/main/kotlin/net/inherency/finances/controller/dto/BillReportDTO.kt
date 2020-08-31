package net.inherency.finances.controller.dto

import java.time.LocalDate

data class BillReportDTO (
        val accountId: Int,
        val name: String,
        val description: String,
        val dueDayOfMonth: Int,
        val autoPayEnabled: Boolean,
        val lastDatePaid: LocalDate
)