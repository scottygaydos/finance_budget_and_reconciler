package net.inherency.finances.domain.report

import net.inherency.finances.external.google.GoogleSheetWritable

data class BudgetReportRow(
    val category: String,
    val budget: String,
    val remaining: String,
    val sort: String
): GoogleSheetWritable {
    override fun toGoogleSheetRowList(): List<String> {
        return listOf(
            category,
            budget,
            remaining
        )
    }
}