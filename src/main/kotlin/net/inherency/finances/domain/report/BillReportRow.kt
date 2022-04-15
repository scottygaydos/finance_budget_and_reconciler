package net.inherency.finances.domain.report

import net.inherency.finances.external.google.GoogleSheetWritable

data class BillReportRow (
    val bill: String,
    val dueDay: String,
    val lastPaymentDate: String,
    val amountPaid: String
): GoogleSheetWritable {
    override fun toGoogleSheetRowList(): List<String> {
        return listOf(
            bill,
            dueDay,
            lastPaymentDate,
            amountPaid
        )
    }
}