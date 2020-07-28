package net.inherency.finances.domain.budget.template

data class BudgetTemplateData (
        val budgetCategoryId: Int,
        val amount: Int,
        val ordering: String
)