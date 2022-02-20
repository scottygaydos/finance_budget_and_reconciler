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
        val creditAccount: Account?) {
                override fun toString(): String {
                        val newLine = System.lineSeparator()
                        return "BudgetCategoryRule" + newLine +
                                "   Category=${category.name}" + newLine +
                                "   Credit=${creditAccount?.name}" + newLine +
                                "   Debit=${debitAccount?.name}"
                }
        }
