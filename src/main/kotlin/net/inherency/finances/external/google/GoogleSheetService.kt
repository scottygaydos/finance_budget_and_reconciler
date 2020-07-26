package net.inherency.finances.external.google

import net.inherency.finances.external.mint.MintFileParser
import net.inherency.finances.external.mint.MintTransactionFactory
import net.inherency.finances.external.parseGoogleSheetLocalDate
import net.inherency.finances.external.parseGoogleSheetAmount
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.stereotype.Service

@Service
class GoogleSheetService(
        private val googleSheetClient: GoogleSheetClient,
        private val mintTransactionFactory: MintTransactionFactory) {

    fun listAllMintTransactionsInTransactionsTab(): List<MintTransaction> {
        val recordsInTab = googleSheetClient.listValuesInTab(TabName.MINT_TRANSACTIONS)
        if (recordsInTab.isEmpty()) {
            return emptyList()
        }
        val recordsWithoutHeader = recordsInTab.subList(1, recordsInTab.size)
        return mintTransactionFactory.listOfListOfStringsToListOfMintTransactions(
                recordsWithoutHeader, { parseGoogleSheetLocalDate(it) }, { parseGoogleSheetAmount(it) })
    }

    fun clearTabAndWriteAllValuesToSheetAfterSorting(tab: TabName, mintTransactions: List<MintTransaction>) {
        val sortedMintTransactions = mintTransactions.sortedByDescending { it.date }
        clearAllExistingValuesInTab(tab)
        writeHeaderAndAllTransactions(tab, sortedMintTransactions)
    }

    private fun writeHeaderAndAllTransactions(tab: TabName, sortedMintTransactions: List<MintTransaction>) {
        val headerRow = MintFileParser.HEADER_LINE_VALUES.toMutableList()
        val allRows = sortedMintTransactions.map { it.toGoogleSheetRowList().toMutableList() }.toMutableList()
        allRows.add(0, headerRow)
        googleSheetClient.writeAllValuesToTab(tab, allRows)
    }

    private fun clearAllExistingValuesInTab(tab: TabName) {
        googleSheetClient.clearAllDataInTab(tab)
    }
}