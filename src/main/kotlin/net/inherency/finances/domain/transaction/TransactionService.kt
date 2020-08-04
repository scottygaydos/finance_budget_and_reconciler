package net.inherency.finances.domain.transaction

import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
import net.inherency.finances.external.mint.MintFileParser
import org.springframework.stereotype.Service
import java.util.*

@Service
class TransactionService(
        private val transactionRepository: TransactionRepository,
        private val mintClient: MintClient) {

    fun updateMintTransactions(): List<MintTransaction> {
        val sortedMintTransactions = mintClient.downloadAllTransactions().sortedByDescending { it.date }
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


}