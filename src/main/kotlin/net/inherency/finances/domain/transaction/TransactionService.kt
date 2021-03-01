package net.inherency.finances.domain.transaction

import net.inherency.finances.controller.dto.CreateTransactionCmd
import net.inherency.finances.controller.dto.TransactionDTO
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
import net.inherency.finances.external.mint.MintFileParser
import net.inherency.finances.util.DateTimeService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class TransactionService(
        private val transactionRepository: TransactionRepository,
        private val mintClient: MintClient,
        private val dateTimeService: DateTimeService,
        private val budgetCategoryService: BudgetCategoryService,
        private val accountService: AccountService) {

    fun updateMintTransactions(): List<MintTransaction> {
        val sortedMintTransactions = mintClient.downloadAllTransactions()
            .filterNot { it.accountName == "INDIVIDUAL - TOD" } //TODO: Handle this with configuration somehow
            .filterNot { it.accountName == "ONPREM 401k" } //TODO: Handle this with configuration somehow
            .filterNot { it.accountName == "CHASE AUTO ACCOUNT" } //TODO: Handle this with configuration somehow
            .sortedByDescending { it.date }
        transactionRepository.clearAllExistingMintTransactions()
        val headerRow = MintFileParser.HEADER_LINE_VALUES.toMutableList()
        transactionRepository.writeHeaderAndAllTransactions(TabName.MINT_TRANSACTIONS, headerRow, sortedMintTransactions)
        return sortedMintTransactions
    }

    fun listAllCategorizedTransactions(): List<CategorizedTransaction> {
        return transactionRepository.listAllCategorizedTransactions()
    }

    fun listAllReconciledTransactions(): List<ReconciledTransaction> {
        return transactionRepository.listAllReconciledTransactions()
    }

    fun createCategorizedTransactionFromMintTransaction(creditAccount: Account, debitAccount: Account, mintTransaction: MintTransaction, category: BudgetCategoryData) {
        val categorizedTransaction = CategorizedTransaction(
                UUID.randomUUID(),
                mintTransaction.date,
                category.id,
                mintTransaction.originalDescription,
                mintTransaction.description,
                creditAccount.id,
                debitAccount.id,
                mintTransaction.amount,
                mintTransaction.amount,
                true
        )
        transactionRepository.addCategorizedTransactionRow(categorizedTransaction)
    }

    fun create(cmd: CreateTransactionCmd) {
        requireNotNull(budgetCategoryService.readAll().firstOrNull { it.id == cmd.transactionTypeId },
                {"Please provide valid budget category Id"})
        val txDate = cmd.transactionDateString?.let { LocalDate.parse(cmd.transactionDateString) }
                ?: dateTimeService.now()
        val authAmt: Int = transformStringToIntegerForAmounts(cmd.authorizedAmount)
        val settAmt: Int = transformStringToIntegerForAmounts(cmd.settledAmount ?: cmd.authorizedAmount)

        val categorizedTransaction = CategorizedTransaction(
                UUID.randomUUID(),
                txDate,
                cmd.transactionTypeId,
                cmd.description,
                cmd.description,
                cmd.creditAccountId,
                cmd.debitAccountId,
                authAmt,
                settAmt,
                cmd.canReconcile
        )
        transactionRepository.addCategorizedTransactionRow(categorizedTransaction)
    }

    fun create(categorizedTransaction: CategorizedTransaction) {
        transactionRepository.addCategorizedTransactionRow(categorizedTransaction)
    }

    fun reportAllCategorizedTransactionsAfter(cutoffDate: LocalDate = dateTimeService.now().minusDays(91)) :
            List<TransactionDTO> {
        val categories = budgetCategoryService.readAll()
        val accounts = accountService.readAll()
        return transactionRepository.listAllCategorizedTransactions().filter {
            it.date.isAfter(cutoffDate)
        }.map { tx ->
            val creditAccount = accounts.first { it.id == tx.creditAccountId }
            val debitAccount = accounts.first { it.id == tx.debitAccountId }
            TransactionDTO(
                    tx.date,
                    categories.first { it.id == tx.budgetCategoryId }.name,
                    tx.description,
                    tx.authorizedAmount,
                    tx.settledAmount,
                    tx.reconcilable,
                    tx.id.toString(),
                    creditAccount.name,
                    debitAccount.name,
                    "-", //TODO: Remove this?  Is it useful?
                    //TODO : Fix this by configuring accounts
                    if ((creditAccount.name+debitAccount.name).toLowerCase().contains("savor")) {
                        50
                    } else {
                        100
                    }
            )
        }.sortedByDescending { it.transaction_date }
    }

    private fun transformStringToIntegerForAmounts(amt: String): Int {
        return BigDecimal(amt).multiply(BigDecimal("100")).toInt()
    }

}