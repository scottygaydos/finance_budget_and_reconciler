package net.inherency.finances.controller.dto

data class BillReportDTO (val billPayments: List<BillDTO>) {

    @Suppress("unused")
    private val balanceRemainingBills: List<Any>
        get() { return emptyList() }
}