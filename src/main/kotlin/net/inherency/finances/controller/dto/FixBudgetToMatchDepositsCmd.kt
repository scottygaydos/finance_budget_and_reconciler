package net.inherency.finances.controller.dto

data class FixBudgetToMatchDepositsCmd (
        val budgetYear: Int,
        val budgetMonth: Int,
        val transactionTypeId: Int
)