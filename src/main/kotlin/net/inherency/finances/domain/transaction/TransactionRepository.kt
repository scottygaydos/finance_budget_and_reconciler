package net.inherency.finances.domain.transaction

import net.inherency.finances.external.google.TabName

interface TransactionRepository {
    fun addCategorizedTransactionRow(categorizedTransaction: CategorizedTransaction)
    fun listAllMintTransactions(): List<MintTransaction>
    fun listAllCategorizedTransactions(): List<CategorizedTransaction>
    fun listAllReconciledTransactions(): List<ReconciledTransaction>
    fun clearAllExistingMintTransactions()
    fun writeHeaderAndAllTransactions(
            tabName: TabName, headerRow: MutableList<String>, sortedItems: List<Transaction>)
}