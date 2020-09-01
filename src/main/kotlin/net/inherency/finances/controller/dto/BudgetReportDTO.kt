package net.inherency.finances.controller.dto

data class BudgetReportDTO (
        val transactionTypeReports: Map<Int, TransactionTypeReportDTO>,
        val totalBudget: Int,
        val totalRemainingBudget: Int,
        val paychecksPreviousMonthArray: List<PaycheckDTO>,
        val totalPaychecksPreviousMonth: Int,
        val discrepancyBetweenTotalBudgetAndPaychecks: Int
)