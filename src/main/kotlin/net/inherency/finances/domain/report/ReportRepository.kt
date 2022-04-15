package net.inherency.finances.domain.report

interface ReportRepository {
    fun clearTransactionReport()
    fun addTransactionReportRows(sortedRows: List<TransactionReportRow>)
    fun clearBudgetReport()
    fun addBudgetReportRows(sortedRows: List<BudgetReportRow>)
    fun clearBillReport()
    fun addBillReportRows(sortedRows: List<BillReportRow>)
}