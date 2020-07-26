package net.inherency.finances.domain.transaction

import net.inherency.finances.external.google.GoogleSheetService
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
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