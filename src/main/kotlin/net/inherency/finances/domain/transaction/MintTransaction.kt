package net.inherency.finances.domain.transaction

import java.time.LocalDate

data class MintTransaction(
        val date: LocalDate,
        val description: String,
        val originalDescription: String,
        val amount: Int,
        val creditOrDebit: CreditOrDebit,
        val category: String,
        val accountName: String
) {

    fun toGoogleSheetRowList(): List<String> {
        return listOf(
                date.toString(),
                description,
                originalDescription,
                amount.toString(),
                creditOrDebit.toString(),
                category,
                accountName
        )
    }
}