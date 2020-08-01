package net.inherency.finances.domain.transaction

import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
import net.inherency.finances.external.mint.MintFileParser
import org.springframework.stereotype.Service

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

    fun listAllMintTransactions(): List<MintTransaction> {
        return transactionRepository.listAllMintTransactions()
    }

    fun listAllReconciledTransactions(): List<ReconciledTransaction> {
        return transactionRepository.listAllReconciledTransactions()
    }

}