package net.inherency.finances.domain.report

import net.inherency.finances.bigDecimalCentsToCurrency
import net.inherency.finances.controller.dto.BillReportDTO
import net.inherency.finances.controller.dto.BudgetReportDTO
import net.inherency.finances.controller.dto.TransactionDTO
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.intCentsToCurrency
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val accountService: AccountService
) {

    fun updateTransactionReport(categorizedTransactions: List<TransactionDTO>) {
        val rows = categorizedTransactions.map { tx ->
            TransactionReportRow(
                tx.transaction_date,
                tx.category,
                tx.description,
                tx.description,
                tx.credit_account_name,
                tx.debit_account_name,
                intCentsToCurrency(tx.authorized_amount),
                intCentsToCurrency(tx.settled_amount),
                calculateAndFormatBudgetAmount(tx)
            )
        }.sortedByDescending { it.date }

        reportRepository.clearTransactionReport()
        reportRepository.addTransactionReportRows(rows)
    }

    fun updateBillReport(billReportDTO: BillReportDTO) {
        val rows = billReportDTO.billPayments.map {
            BillReportRow(
                it.billName,
                it.dueDayOfMonth.toString(),
                it.lastPaymentDate.toString(),
                intCentsToCurrency(it.amountPaid)
            )
        }.sortedByDescending { it.lastPaymentDate }

        reportRepository.clearBillReport()
        reportRepository.addBillReportRows(rows)
    }

    fun updateBudgetReport(dto: BudgetReportDTO) {
        val rows = dto.transactionTypeReports
            .map { (_,transactionTypeReportDTO ) ->
                BudgetReportRow(
                    transactionTypeReportDTO.transactionTypeName,
                    intCentsToCurrency(transactionTypeReportDTO.budgetAmount),
                    intCentsToCurrency(transactionTypeReportDTO.remainingAmount),
                    transactionTypeReportDTO.sortOrderValue
                )
            }.sortedBy { it.sort }
        val totalRowValues = dto.transactionTypeReports
            .map { (_,transactionTypeReportDTO ) ->
                Pair(transactionTypeReportDTO.budgetAmount, transactionTypeReportDTO.remainingAmount)
            }.reduce{ acc, next ->  sumPairs(acc, next)}
        val totalRow = BudgetReportRow(
            "Total",
            intCentsToCurrency(totalRowValues.first),
            intCentsToCurrency(totalRowValues.second),
            "z"
        )


        reportRepository.clearBudgetReport()
        reportRepository.addBudgetReportRows(rows.plus(totalRow))
    }

    private fun sumPairs(p1: Pair<Int, Int>, p2: Pair<Int, Int>): Pair<Int, Int> {
        return Pair(p1.first + p2.first, p1.second + p2.second)
    }

    private fun calculateAndFormatBudgetAmount(tx: TransactionDTO): String {
        val credit = accountService.findByName(tx.credit_account_name)
        val debit = accountService.findByName(tx.debit_account_name)
        val multiplier = credit.budgetMultiplier.min(debit.budgetMultiplier)
        return bigDecimalCentsToCurrency(tx.settled_amount.toBigDecimal().multiply(multiplier))
    }

}