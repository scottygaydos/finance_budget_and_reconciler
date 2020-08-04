package net.inherency.finances.domain.reconcile

import net.inherency.finances.CommandLineService
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.domain.transaction.TransactionService
import org.springframework.stereotype.Service

@Service
class RemainingMintTransactionsService(
        private val accountService: AccountService,
        private val commandLineService: CommandLineService,
        private val transactionService: TransactionService,
        private val budgetCategoryService: BudgetCategoryService) {

    companion object {
        val AFFIRMATIVE_ANSWERS = listOf("y", "yes", "true")
    }

    fun promptAndHandleRemainingMintTransactions(remainingMintTransactions: List<MintTransaction>) {

        if (remainingMintTransactions.isEmpty()) {
            return
        }

        val accounts = accountService.readAll()
        val globalExternalAccount = accounts.first { it.name == (GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME) }
        val budgetCategories = budgetCategoryService.readAll()

        remainingMintTransactions.forEach { mintTx ->
            val mintAccount = findAccountByMintAccountName(accounts, mintTx)
            val (creditAccount, debitAccount) =
                    determineDebitAndCreditAccounts(mintTx, mintAccount, globalExternalAccount)

            println(mintTx)
            println("Do you want to add this transaction?  To confirm enter one of the following: $AFFIRMATIVE_ANSWERS")
            val doAddTransactionInput = commandLineService.readFromCommandLine()
            if (AFFIRMATIVE_ANSWERS.map { it.toLowerCase() }.contains(doAddTransactionInput.toLowerCase())) {
                val inputOption = selectInputOption(budgetCategories)
                createCategorizedTransaction(creditAccount, debitAccount, mintTx, inputOption)
            }
        }
    }

    private fun createCategorizedTransaction(
            creditAccount: Account, debitAccount: Account, mintTransaction: MintTransaction,
            category: BudgetCategoryData) {
        transactionService.createCategorizedTransactionFromMintTransaction(
                creditAccount, debitAccount, mintTransaction, category)
    }

    private fun selectInputOption(budgetCategories: List<BudgetCategoryData>): BudgetCategoryData {
        println("Please input transaction category.  Input options...")
        budgetCategories.forEach {
            println("${it.id} = ${it.name}")
        }

        //TODO: Remove this hard-coding of shortcut options for some kind of external mapping
        val shortCuts = mapOf("s" to budgetCategories.firstOrNull { it.name == "Spending" },
              "f" to budgetCategories.firstOrNull { it.name == "Food" },
              "g" to budgetCategories.firstOrNull { it.name == "Gas" }
        )
        shortCuts.forEach { (k, v) ->
            if (v != null) {
                println("$k = ${v.name}")
            }
        }

        return try {
            val chosenOption = commandLineService.readFromCommandLine().trim().toLowerCase()
            shortCuts[chosenOption]
                    ?: budgetCategories.firstOrNull { chosenOption.toInt() == it.id }
                    ?: error("Could not find valid matching input option.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error!  Please input a valid option.")
            selectInputOption(budgetCategories)
        }
    }

    private fun determineDebitAndCreditAccounts(mintTx: MintTransaction, mintAccount: Account,
                                                globalExternalAccount: Account): Pair<Account, Account> {
        return when (mintTx.creditOrDebit) {
            CreditOrDebit.CREDIT -> {
                Pair(mintAccount, globalExternalAccount)
            }
            CreditOrDebit.DEBIT -> {
                Pair(globalExternalAccount, mintAccount)
            }
            CreditOrDebit.UNKNOWN -> {
                throw IllegalArgumentException("Please review mint tx for credit/debit status: $mintTx")
            }
        }
    }

    private fun findAccountByMintAccountName(accounts: List<Account>, mintTx: MintTransaction) =
        accounts.firstOrNull { mintTx.accountName == it.mintName || mintTx.accountName == it.mintNameAlt }
                ?: error("Could not find account for $mintTx")


}