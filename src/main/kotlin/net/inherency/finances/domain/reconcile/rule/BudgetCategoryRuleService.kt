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

    private var rules: List<BudgetCategoryRuleData> = emptyList()

    fun findMatchingRuleForAutoCategorization(mintTx: MintTransaction): BudgetCategoryRule? {
        queryRulesIfNeeded()
        val rules = budgetCategoryRuleRepository
                .readAll()
                .filter { validateRuleOnlyRoutesOneAccount(it) }
        val ruleMatch =
             rules.firstOrNull { rule -> ruleDescriptionDoesMatchTransaction(rule, mintTx) } ?:
             rules.firstOrNull { rule -> ruleDescriptionUsesWildcardsAndDoesMatchTransaction(rule, mintTx) } ?:
             rules.firstOrNull { rule -> ruleDescriptionIsAnythingAndDebitAccountMatches(rule, mintTx) } ?:
             rules.firstOrNull { rule -> ruleDescriptionIsAnythingAndCreditAccountMatches(rule, mintTx) }
        return validateRuleOrReturnNull(ruleMatch)
    }

    private fun queryRulesIfNeeded() {
        if (rules.isEmpty()) {
            rules = budgetCategoryRuleRepository
                .readAll()
                .filter { validateRuleOnlyRoutesOneAccount(it) }
        }
    }

    private fun ruleDescriptionDoesMatchTransaction(rule: BudgetCategoryRuleData, mintTx: MintTransaction) =
            rule.descriptionToMatch == mintTx.description || rule.descriptionToMatch == mintTx.originalDescription

    private fun ruleDescriptionUsesWildcardsAndDoesMatchTransaction(rule: BudgetCategoryRuleData,
                                                                    mintTx: MintTransaction): Boolean {
        return if (rule.descriptionToMatch.startsWith("*") && rule.descriptionToMatch.endsWith("*")) {
            val description = rule.descriptionToMatch.removePrefix("*").removeSuffix("*")
            mintTx.description.contains(description) || mintTx.originalDescription.contentEquals(description)
        } else {
            false
        }
    }

    private fun ruleDescriptionIsAnythingAndDebitAccountMatches(rule: BudgetCategoryRuleData, mintTx: MintTransaction)
    : Boolean {
        if (rule.descriptionToMatch == "*") {
            val debitAccount = findAccount(rule.accountIdToDebit) ?: return false
            return listOf(debitAccount.mintName, debitAccount.mintNameAlt).contains(mintTx.getDebitAccountName())
        } else {
            return false
        }
    }

    private fun ruleDescriptionIsAnythingAndCreditAccountMatches(rule: BudgetCategoryRuleData, mintTx: MintTransaction)
            : Boolean {
        if (rule.descriptionToMatch == "*") {
            val creditAccount = findAccount(rule.accountIdToCredit) ?: return false
            return listOf(creditAccount.mintName, creditAccount.mintNameAlt).contains(mintTx.getCreditAccountName())
        } else {
            return false
        }
    }

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