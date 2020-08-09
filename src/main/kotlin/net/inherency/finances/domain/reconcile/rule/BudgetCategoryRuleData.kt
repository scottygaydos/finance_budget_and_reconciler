package net.inherency.finances.domain.reconcile.rule

import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.budget.category.BudgetCategoryData

data class BudgetCategoryRuleData (
        val descriptionToMatch: String,
        val accountIdToCredit: Int?,
        val accountIdToDebit: Int?,
        val budgetCategoryId: Int
)

data class BudgetCategoryRule (
        val category: BudgetCategoryData,
        val debitAccount: Account?,
        val creditAccount: Account?
)