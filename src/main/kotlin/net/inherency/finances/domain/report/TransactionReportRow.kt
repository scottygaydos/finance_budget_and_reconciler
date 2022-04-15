package net.inherency.finances.domain.report

import net.inherency.finances.external.google.GoogleSheetWritable
import java.time.LocalDate

data class TransactionReportRow (
    val date: LocalDate,
    val budgetCategory: String,
    val description: String,
    val bankPayee: String,
    val creditAccount: String,
    val debitAccount: String,
    val authorizedAmount: String,
    val settledAmount: String,
    val budgetAmount: String
): GoogleSheetWritable {
    override fun toGoogleSheetRowList(): List<String> {
        return listOf(
            date.toString(),
            budgetCategory,
            description,
            bankPayee,
            creditAccount,
            debitAccount,
            authorizedAmount,
            settledAmount,
            budgetAmount
        )
    }
}