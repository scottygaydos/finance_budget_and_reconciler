package net.inherency.finances.controller.dto

data class TransactionTypeReportDTO (
        val transactionTypeId: Int,
        val transactionTypeName: String,
        val budgetAmount: Int,
        val remainingAmount: Int,
        val sortOrderValue: String
)