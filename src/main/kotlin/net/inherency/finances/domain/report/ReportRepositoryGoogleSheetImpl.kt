package net.inherency.finances.domain.report

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryGoogleSheetImpl(
    private val googleSheetClient: GoogleSheetClient): ReportRepository {

    companion object {
        val Transactions_HEADER_LINE_VALUES =
            listOf("Date","Budget Category","Description","Bank Payee","Credit Account","Debit Account","Authorized Amt","Settled Amt","Budget Amount")
        val Budget_HEADER_LINE_VALUES =
            listOf("Category","Budget","Remaining")
        val Bills_HEADER_LINE_VALUES =
            listOf("Bill","Due Day","Last Payment Date", "Amount Paid")
    }

    override fun clearTransactionReport() {
        googleSheetClient.clearAllDataInTab(TabName.TransactionsReport)
    }

    override fun addTransactionReportRows(sortedRows: List<TransactionReportRow>) {
        val allRows = sortedRows
            .map { it.toGoogleSheetRowList() }.toMutableList()
        allRows.add(0, Transactions_HEADER_LINE_VALUES)
        googleSheetClient.writeAllValuesToTab(TabName.TransactionsReport, allRows)
    }

    override fun clearBudgetReport() {
        googleSheetClient.clearAllDataInTab(TabName.BudgetReport)
    }

    override fun addBudgetReportRows(sortedRows: List<BudgetReportRow>) {
        val allRows = sortedRows
            .map { it.toGoogleSheetRowList() }.toMutableList()
        allRows.add(0, Budget_HEADER_LINE_VALUES)
        googleSheetClient.writeAllValuesToTab(TabName.BudgetReport, allRows)
    }

    override fun clearBillReport() {
        googleSheetClient.clearAllDataInTab(TabName.BillsReport)
    }

    override fun addBillReportRows(sortedRows: List<BillReportRow>) {
        val allRows = sortedRows
            .map { it.toGoogleSheetRowList() }.toMutableList()
        allRows.add(0, Bills_HEADER_LINE_VALUES)
        googleSheetClient.writeAllValuesToTab(TabName.BillsReport, allRows)
    }
}