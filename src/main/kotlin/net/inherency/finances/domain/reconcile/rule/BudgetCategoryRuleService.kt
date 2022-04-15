package net.inherency.finances.domain.reconcile.rule

import me.xdrop.fuzzywuzzy.FuzzySearch
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.transaction.MintTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BudgetCategoryRuleService(
        private val budgetCategoryRuleRepository: BudgetCategoryRuleRepository,
        private val accountService: AccountService,
        private val budgetCategoryService: BudgetCategoryService) {

    private var rules: List<BudgetCategoryRuleData> = emptyList()
    private val log = LoggerFactory.getLogger(BudgetCategoryRuleService::class.java)

    fun findMatchingRuleForAutoCategorization(mintTx: MintTransaction): BudgetCategoryRule? {
        var ruleMatch = matchBasedOnMintCategory(mintTx)
        if (ruleMatch == null) {
            queryRulesIfNeeded()
            val rules = budgetCategoryRuleRepository
                .readAll()
                .filter { validateRuleOnlyRoutesOneAccount(it) }
            ruleMatch =
                rules.firstOrNull { rule -> ruleDescriptionDoesMatchTransaction(rule, mintTx) } ?:
                rules.firstOrNull { rule -> ruleDescriptionUsesWildcardsAndDoesMatchTransaction(rule, mintTx) } ?:
                rules.firstOrNull { rule -> ruleDescriptionIsAnythingAndDebitAccountMatches(rule, mintTx) } ?:
                rules.firstOrNull { rule -> ruleDescriptionIsAnythingAndCreditAccountMatches(rule, mintTx) }
        }

        return validateRuleOrReturnNull(ruleMatch)
    }

    private fun nonBudgetCategory(categories: List<BudgetCategoryData>): BudgetCategoryData {
        return categories.first { it.name == "Non Budget" }
    }

    private fun matchBasedOnMintCategory(mintTx: MintTransaction): BudgetCategoryRuleData? {
        val accounts = accountService.readAll()
        val categories = budgetCategoryService.readAll()
        val globalExternalAccount = accounts.first { it.name == GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME }
        val checkingAccount = accounts.first { it.name.contains("Checking") }

        val creditCardPayment = "Credit Card Payment"
        if (mintTx.category == creditCardPayment) {
            return handleMintCategoryCreditCardPayment(mintTx, creditCardPayment, accounts, categories, checkingAccount)
        }

        val food = "Food"
        val spending = "Spending"
        val savings = "Savings"
        val gas = "Gas"
        val utilities = "Utilities"
        val mortgage = "Mortgage"
        val carInsurance = "Car Insurance"
        val health = "Health"
        val car = "Car"
        val tax = "Tax Deductible Donation"
        val income = "Income"
        val reimbursement = "Blaine Shared Card Payment"

        val foodRule = QuickRule(food, globalExternalAccount, null)
        val spendingRule = QuickRule(spending, globalExternalAccount, null)
        val myCategoryAndCreditAccountByMintCategory = mapOf(
            "Paycheck" to QuickRule(income, checkingAccount, null),
            "Blaine Shared" to QuickRule(reimbursement, checkingAccount, null),
            "Auto Payment" to QuickRule(car, accounts.first { it.name.contains("Car Pmt") }, null),
            "Charity" to QuickRule(tax, globalExternalAccount, null),
            "Health & Fitness" to QuickRule(health, globalExternalAccount, null),
            "Food & Dining" to foodRule,
            "Groceries" to foodRule,
            "Restaurants" to foodRule,
            "Fast Food" to foodRule,
            "Shopping" to spendingRule,
            "Entertainment" to spendingRule,
            "Savings" to QuickRule(savings, globalExternalAccount, null),
            "Gas & Fuel" to QuickRule(gas, globalExternalAccount, null),
            "Electric" to QuickRule(utilities, accounts.first { it.name.contains("Electric") }, null),
            "Water" to QuickRule(utilities, accounts.first { it.name.contains("Water") }, null),
            "Gas" to QuickRule(utilities, accounts.first { it.name.contains("Gas") }, null),
            "Trash" to QuickRule(utilities, accounts.first { it.name.contains("Trash") }, null),
            "Mortgage & Rent" to QuickRule(mortgage, accounts.first { it.name.contains("Mortgage") }, null),
            "HOA" to QuickRule(mortgage, accounts.first { it.name.contains("HOA") }, null),
            "Auto Insurance" to QuickRule(carInsurance, accounts.first { it.name.contains("Car Ins") }, null)
        )
        val mapResult: QuickRule? = myCategoryAndCreditAccountByMintCategory[mintTx.category]
        return if (mapResult != null) {
            val ruleCategory = categories.first { it.name == mapResult.myCategoryName }
            val result = BudgetCategoryRuleData("", mapResult.creditAccount?.id, mapResult.debitAccount?.id, ruleCategory.id)
            log.info("Using rule from mint category - ${mintTx.category}: $result")
            return result
        } else {
            null
        }
    }

    data class QuickRule (
        val myCategoryName: String,
        val creditAccount: Account?,
        val debitAccount: Account?
    )

    @Suppress("SameParameterValue")
    private fun handleMintCategoryCreditCardPayment(mintTx: MintTransaction, creditCardPayment: String,
                                                    accounts: List<Account>,
                                                    categories: List<BudgetCategoryData>,
                                                    checkingAccount: Account): BudgetCategoryRuleData? {
        if (accounts.first { mintTx.accountName == it.mintName || mintTx.accountName == it.mintNameAlt } == checkingAccount) {
            val result = BudgetCategoryRuleData("", null, checkingAccount.id, nonBudgetCategory(categories).id)
            log.info("Using rule from mint category - ${mintTx.category}: $result")
            return result
        }
        val billAccount = findBestBillMatch(mintTx, accounts)
        return if (billAccount == null) {
            null
        } else {
            val category = categories.first { it.name == creditCardPayment }
            val result = BudgetCategoryRuleData("", billAccount.id, null, category.id)
            log.info("Using rule from mint category - ${mintTx.category}: $result")
            return result
        }
    }

    private fun findBestBillMatch(tx: MintTransaction, accounts: List<Account> ): Account? {
        val creditCardAccounts = accounts.filter { it.name.contains("CC") }
        val billsByMatchRatio = creditCardAccounts.associateBy {
            calculateMatchRatioOfBillToTransaction(it, tx)
        }.toSortedMap()
        return billsByMatchRatio[billsByMatchRatio.lastKey()]
    }

    private fun calculateMatchRatioOfBillToTransaction(acct: Account, tx: MintTransaction): Int {
        return if (acct.mintName == tx.accountName || acct.mintNameAlt ==  tx.accountName) {
            100
        } else {
            FuzzySearch.ratio(acct.description, tx.description)
                .coerceAtLeast(FuzzySearch.ratio(acct.description, tx.originalDescription))
                .coerceAtLeast(FuzzySearch.ratio(acct.name, tx.description))
                .coerceAtLeast(FuzzySearch.ratio(acct.name, tx.originalDescription))
        }
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
        return if (rule.descriptionToMatch.startsWith("*") && rule.descriptionToMatch.endsWith("*") && rule.descriptionToMatch.trim().length > 1) {
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
            BudgetCategoryRule(category, debitAccount, creditAccount, ruleMatch.descriptionToMatch)
        }
    }

    private fun findAccount(id: Int?): Account? {
        return accountService.readAll().firstOrNull { it.id == id}
    }

}