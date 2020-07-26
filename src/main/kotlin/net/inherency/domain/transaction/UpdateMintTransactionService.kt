package net.inherency.domain.transaction

import net.inherency.external.google.GoogleSheetService
import net.inherency.external.google.TabName
import net.inherency.external.mint.MintClient
import net.inherency.external.mint.MintTransaction
import org.springframework.stereotype.Service

@Service
class UpdateMintTransactionService(
        private val googleSheetService: GoogleSheetService,
        private val mintClient: MintClient) {

    fun update(): List<MintTransaction> {
        val allMintTransactions = downloadAllMintTransactions()
        googleSheetService.clearTabAndWriteAllValuesToSheetAfterSorting(TabName.MINT_TRANSACTIONS, allMintTransactions)
        return googleSheetService.listAllMintTransactionsInTransactionsTab()
    }

    private fun downloadAllMintTransactions(): List<MintTransaction> {
        return mintClient.downloadAllTransactions()
    }

}