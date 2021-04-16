package net.inherency.finances.domain.transaction

import net.inherency.finances.external.google.CategorizedTransactionFactory
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintTransactionFactory
import net.inherency.finances.external.parseGoogleSheetAmount
import net.inherency.finances.external.parseGoogleSheetLocalDate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TransactionRepositoryGoogleSheetImpl(
    private val googleSheetClient: GoogleSheetClient,
    private val mintTransactionFactory: MintTransactionFactory,
    private val categorizedTransactionFactory: CategorizedTransactionFactory): TransactionRepository {

    override fun addCategorizedTransactionRow(categorizedTransaction: CategorizedTransaction) {
        val singleRowList = listOf(categorizedTransaction.toGoogleSheetRowList())
        googleSheetClient.writeAllValuesToTab(TabName.CATEGORIZED_TRANSACTIONS, singleRowList)
    }

    override fun listAllMintTransactions(): List<MintTransaction> {
        val recordsInTab = googleSheetClient.listValuesInTab(TabName.MINT_TRANSACTIONS)
        if (recordsInTab.isEmpty()) {
            return emptyList()
        }
        val recordsWithoutHeader = recordsInTab.subList(1, recordsInTab.size)
        return mintTransactionFactory.listOfListOfStringsToListOfMintTransactions(
                recordsWithoutHeader, { parseGoogleSheetLocalDate(it) }, { parseGoogleSheetAmount(it) })
    }

    override fun listAllCategorizedTransactions(): List<CategorizedTransaction> {
        val recordsInTab = googleSheetClient.listValuesInTab(TabName.CATEGORIZED_TRANSACTIONS)
        if (recordsInTab.isEmpty()) {
            return emptyList()
        }
        val recordsWithoutHeader = recordsInTab.subList(1, recordsInTab.size)
        return categorizedTransactionFactory.listOfListOfStringsToCategorizedTransactions(recordsWithoutHeader)
    }

    override fun listAllReconciledTransactions(): List<ReconciledTransaction> {
        val recordsInTab = googleSheetClient.listValuesInTab(TabName.RECONCILED_TRANSACTIONS)
        if (recordsInTab.isEmpty()) {
            return emptyList()
        }
        val recordsWithoutHeader = recordsInTab.subList(1, recordsInTab.size)
        return recordsWithoutHeader.map { ReconciledTransaction(it[0], UUID.fromString(it[1])) }
    }

    override fun clearAllExistingMintTransactions() {
        googleSheetClient.clearAllDataInTab(TabName.MINT_TRANSACTIONS)
    }

    override fun writeHeaderAndAllTransactions(
            tabName: TabName, headerRow: MutableList<String>, sortedItems: List<Transaction>) {
        val allRows = sortedItems
                .map { it.toGoogleSheetRowList() }.toMutableList()
        allRows.add(0, headerRow)
        googleSheetClient.writeAllValuesToTab(tabName, allRows)
    }
}