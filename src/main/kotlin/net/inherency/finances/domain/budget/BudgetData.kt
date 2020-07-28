package net.inherency.finances.domain.budget

data class BudgetData (
        val month: Int,
        val year: Int,
        val budgetCategoryId: Int,
        val amount: Int
)