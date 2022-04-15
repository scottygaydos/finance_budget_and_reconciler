package net.inherency.finances.domain.reconcile

import net.inherency.finances.CommandLineService
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.bill.BillData
import net.inherency.finances.domain.bill.BillService
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.reconcile.rule.BudgetCategoryRule
import net.inherency.finances.domain.reconcile.rule.BudgetCategoryRuleService
import net.inherency.finances.domain.transaction.CategorizedTransaction
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.domain.transaction.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RemainingMintTransactionsService(
        private val accountService: AccountService,
        private val commandLineService: CommandLineService,
        private val transactionService: TransactionService,
        private val budgetCategoryService: BudgetCategoryService,
        private val budgetCategoryRuleService: BudgetCategoryRuleService,
        private val debitAndCreditAccountFactory: DebitAndCreditAccountFactory,
        private val billService: BillService) {

    private val log = LoggerFactory.getLogger(RemainingMintTransactionsService::class.java)

    fun promptAndHandleRemainingMintTransactions(remainingMintTransactions: List<MintTransaction>) {

        if (remainingMintTransactions.isEmpty()) {
            return
        }

        val accounts = accountService.readAll()
        val globalExternalAccount = accounts.first { it.name == (GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME) }
        val budgetCategories = budgetCategoryService.readAll()

        val toCategorize = mutableListOf<MintTransaction>()
        val rowsToAdd = mutableListOf<CategorizedTransaction>()

        remainingMintTransactions
            .filter { it.date.isAfter(LocalDate.of(2021, 1, 31)) } //TODO: Should I remove this and back populate?
            .filter { it.date.isAfter(LocalDate.now().withDayOfMonth(1).minusMonths(1))} //TODO: Should I remove this and back populate?
            .sortedBy { it.date }
            .forEach { mintTx ->
                val mintAccount = findAccountByMintAccountName(accounts, mintTx)
                val (creditAccount, debitAccount) =
                        determineCreditAndDebitAccounts(mintTx, mintAccount, globalExternalAccount)
                val matchingRule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)
                if (matchingRule != null) {
                    rowsToAdd.add(createTransactionFromRule(matchingRule, creditAccount, debitAccount, mintTx))
                } else {
                    toCategorize.add(mintTx)
                }
        }

        log.info("Beginning manual transaction review...")
        toCategorize.forEach { mintTx ->
            val mintAccount = findAccountByMintAccountName(accounts, mintTx)
            val (creditAccount, debitAccount) =
                determineCreditAndDebitAccounts(mintTx, mintAccount, globalExternalAccount)
            val doCategorize = askToCategorizeTransaction(mintTx)
            if (doCategorize) {
                val inputOption = selectInputOption(budgetCategories)
                rowsToAdd.add(createCategorizedTransaction(creditAccount, debitAccount, mintTx, inputOption, false))
            }
        }

        rowsToAdd.sortBy { it.date }
        log.info("Inserting ${rowsToAdd.size} CATEGORIZED_TRANSACTION rows")
        transactionService.createBatch(rowsToAdd)
        log.info("*** Remaining Mint Transaction service complete ***")
    }

    private fun askToCategorizeTransaction(mintTx: MintTransaction): Boolean {
        log.info(mintTx.toString())
        log.info("Do you want to add this transaction?")
        return commandLineService.readConfirmation()
    }

    private fun createTransactionFromRule(matchingRule: BudgetCategoryRule, creditAccount: Account, debitAccount: Account,
                                          mintTx: MintTransaction): CategorizedTransaction {
        val creditAccountForRule = matchingRule.creditAccount ?: creditAccount
        val debitAccountForRule = matchingRule.debitAccount ?: debitAccount
        log.info("Creating transaction based on rule...")
        log.info("Transaction: $mintTx")
        log.info("Rule: $matchingRule")
        log.info("")
        return createCategorizedTransaction(creditAccountForRule, debitAccountForRule, mintTx, matchingRule.category, true)
    }

    private fun createCategorizedTransaction(
            creditAccount: Account, debitAccount: Account, mintTransaction: MintTransaction,
            category: BudgetCategoryData, usedRule: Boolean): CategorizedTransaction {

        try {
            val creditAccountToUse = if (usedRule && isManualBillTransaction(category, usedRule) && !isCreditCardPayment(mintTransaction)) {
                val billOptions = billService.findAllBills()
                    .filter { it.budgetCategoryId == category.id }
                val billData = if (billOptions.size == 1) {
                    billOptions.first()
                } else {
                    log.info("This is a bill. Please type the account id number to use:")
                    val billsByChoiceIndex = logAndMapBillData(billOptions)
                    val billAccountSelection = commandLineService.readFromCommandLine()
                    billsByChoiceIndex[billAccountSelection.toInt()]
                }
                accountService.readAll().first { it.id == billData?.accountId }
            } else {
                creditAccount
            }
            return transactionService.createCategorizedTransactionFromMintTransaction(
                creditAccountToUse, debitAccount, mintTransaction, category)
        } catch (e: Exception) {
            log.error("Invalid selection. Please try again.", e)
            return createCategorizedTransaction(creditAccount, debitAccount, mintTransaction, category, usedRule)
        }
    }

    private fun isCreditCardPayment(mintTransaction: MintTransaction) =
        mintTransaction.category.contains("Credit Card Payment")

    private fun logAndMapBillData(billOptions: List<BillData>): Map<Int, BillData> {
        var counter = 1
        val resultMap = mutableMapOf<Int, BillData>()
        billOptions.forEach {
            log.info("$counter = ${it.name} (${it.description})")
            resultMap[counter] = it
            counter++
        }
        return resultMap
    }

    private fun isManualBillTransaction(category: BudgetCategoryData, usedRule: Boolean): Boolean {
        if (usedRule) {
            return false
        }

        return when(category.name) {
            "Utilities" -> true
            "Mortgage" -> true
            "Car" -> true
            "Car Insurance" -> true
            "Credit Card Payment" -> true
            else -> false
        }
    }

    private fun selectInputOption(budgetCategories: List<BudgetCategoryData>): BudgetCategoryData {
        log.info("Please input transaction category.  Input options...")
        budgetCategories.forEach {
            log.info("${it.id} = ${it.name}")
        }

        //TODO: Remove this hard-coding of shortcut options for some kind of external mapping
        val shortCuts = mapOf("s" to budgetCategories.firstOrNull { it.name == "Spending" },
              "f" to budgetCategories.firstOrNull { it.name == "Food" },
              "g" to budgetCategories.firstOrNull { it.name == "Gas" }
        )
        shortCuts.forEach { (k, v) ->
            if (v != null) {
                log.info("$k = ${v.name}")
            }
        }

        return try {
            val chosenOption = commandLineService.readFromCommandLine().trim().toLowerCase()
            shortCuts[chosenOption]
                    ?: budgetCategories.firstOrNull { chosenOption.toInt() == it.id }
                    ?: error("Could not find valid matching input option.")
        } catch (e: Exception) {
            log.error("Error!  Please input a valid option.", e)
            selectInputOption(budgetCategories)
        }
    }

    private fun determineCreditAndDebitAccounts(mintTx: MintTransaction, mintAccount: Account,
                                                globalExternalAccount: Account): Pair<Account, Account> {
        return debitAndCreditAccountFactory.determineCreditAndDebitAccounts(
                mintTx, mintAccount, globalExternalAccount)
    }

    private fun findAccountByMintAccountName(accounts: List<Account>, mintTx: MintTransaction) =
        accounts.firstOrNull { mintTx.accountName == it.mintName || mintTx.accountName == it.mintNameAlt }
                ?: error("Could not find account for $mintTx")


}