package net.inherency.finances.domain.reconcile.rule

import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.stereotype.Service

@Service
class BudgetCategoryRuleService(
        private val budgetCategoryRuleRepository: BudgetCategoryRuleRepository,
        private val accountService: AccountService,
        private val budgetCategoryService: BudgetCategoryService) {

    fun findMatchingRuleForAutoCategorization(mintTx: MintTransaction): BudgetCategoryRule? {
        val rules = budgetCategoryRuleRepository
                .readAll()
                .filter { validateRuleOnlyRoutesOneAccount(it) }
        val ruleMatch =  rules.firstOrNull { rule ->
            ruleDescriptionDoesMatchTransaction(rule, mintTx)
        }
        return validateRuleOrReturnNull(ruleMatch)
    }

    private fun ruleDescriptionDoesMatchTransaction(rule: BudgetCategoryRuleData, mintTx: MintTransaction) =
            rule.descriptionToMatch == mintTx.description || rule.descriptionToMatch == mintTx.originalDescription

    private fun validateRuleOnlyRoutesOneAccount(rule: BudgetCategoryRuleData) =
            (rule.accountIdToCredit != null && rule.accountIdToDebit == null)
                || (rule.accountIdToCredit == null && rule.accountIdToDebit != null)


    private fun validateRuleOrReturnNull(ruleMatch: BudgetCategoryRuleData?): BudgetCategoryRule? {
        if (ruleMatch == null) {
            return null
        }

        val creditAccount = findAccount(ruleMatch.accountIdToCredit)
        val debitAccount = findAccount(ruleMatch.accountIdToDebit)

        if (creditAccount == null && debitAccount == null) {
            return null
        }
        if (creditAccount != null && debitAccount != null) {
            return null
        }

        val category = budgetCategoryService.readAll()
                .firstOrNull { it.id == ruleMatch.budgetCategoryId }

        return if (category == null) {
            null
        } else {
            BudgetCategoryRule(category, debitAccount, creditAccount)
        }
    }

    private fun findAccount(id: Int?): Account? {
        return accountService.readAll().firstOrNull { it.id == id}
    }

}