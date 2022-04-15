package net.inherency.finances.domain.bill

import net.inherency.finances.controller.dto.BillDTO
import net.inherency.finances.controller.dto.BillReportDTO
import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BillService(private val billRepository: BillRepository, private val transactionService: TransactionService) {

    private val allBills = mutableListOf<BillData>()

    fun findAllBills(): List<BillData> {
        if (allBills.isEmpty()) {
            allBills.addAll(billRepository.readAll())
        }
        return allBills
    }

    fun findAllBillsReportableViaUI(): BillReportDTO {
        val bills = billRepository
                .readAll()
                .filter { it.showInUIReports }
        validateBillEntries(bills)

        return BillReportDTO(
                findLastPaymentInfoForBills(bills).mapNotNull { (bill, lastPaymentDateAndAmount) ->
                    lastPaymentDateAndAmount?.let {
                        BillDTO(
                                bill.name,
                                bill.dueDayOfMonth,
                                lastPaymentDateAndAmount.first,
                                lastPaymentDateAndAmount.second,
                                bill.autoPayEnabled,
                                bill.description
                        )
                    }
                }
        )
    }

    private fun findLastPaymentInfoForBills(bills: List<BillData>): Map<BillData, Pair<LocalDate, Int>?> {
        return transactionService.listAllCategorizedTransactions()
                .filter { bills.map { bill -> bill.accountId }.contains(it.creditAccountId) }
                .groupBy { it.creditAccountId }
                .mapKeys { bills.first { bill -> bill.accountId == it.key } }
                .mapValues { txs ->
                    txs.value.maxBy { it.date } }
                .mapValues { tx ->
                    if (tx.value == null) {
                        null
                    }
                    else {
                        Pair(tx.value!!.date, tx.value!!.settledAmount)
                    }
                }
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