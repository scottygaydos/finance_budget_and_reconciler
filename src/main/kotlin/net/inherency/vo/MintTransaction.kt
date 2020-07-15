package net.inherency.vo

import java.time.LocalDate

data class MintTransaction(
        val date: LocalDate,
        val description: String,
        val originalDescription: String,
        val amount: Int,
        val creditOrDebit: CreditOrDebit,
        val category: String,
        val accountName: String
)