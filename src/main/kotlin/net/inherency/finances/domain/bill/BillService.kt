package net.inherency.finances.domain.bill

import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service

@Service
class BillService(private val billRepository: BillRepository) {

    fun findAllBillsReportableViaUI(): List<BillData> {
        val bills = billRepository
                .readAll()
                .filter { it.showInUIReports }
        validateBillEntries(bills)
        return bills
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