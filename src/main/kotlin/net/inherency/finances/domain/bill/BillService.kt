package net.inherency.finances.domain.bill

import net.inherency.finances.controller.dto.BillReportDTO
import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BillService(private val billRepository: BillRepository, private val transactionService: TransactionService) {

    fun findAllBillsReportableViaUI(): List<BillReportDTO> {
        val bills = billRepository
                .readAll()
                .filter { it.showInUIReports }
        validateBillEntries(bills)

        return findLastPaidDateForBills(bills).mapNotNull { (bill, lastPaymentDate) ->
            lastPaymentDate?.let {
                BillReportDTO(
                        bill.accountId,
                        bill.name,
                        bill.description,
                        bill.dueDayOfMonth,
                        bill.autoPayEnabled,
                        lastPaymentDate)
            }
        }
    }

    private fun findLastPaidDateForBills(bills: List<BillData>): Map<BillData, LocalDate?> {
        return transactionService.listAllCategorizedTransactions()
                .filter { bills.map { bill -> bill.accountId }.contains(it.creditAccountId) }
                .groupBy { it.creditAccountId }
                .mapKeys { bills.first { bill -> bill.accountId == it.key } }
                .mapValues { txs ->
                    txs.value.maxBy { it.date }?.date }

    }

    private fun validateBillEntries(bills: List<BillData>) {
        if (bills.isNotEmpty()) {
            bills.forEach {
                check(objectIsUnique(bills) { bill -> bill.accountId }) { "Each bill must be for a unique account" }
                check(it.dueDayOfMonth >= 1) {"Bills must be due on or after the 1st of the month"}
                check(it.dueDayOfMonth <= 31) {"Bills must be due on or before the 31st of the month"}
            }
        }
    }

}