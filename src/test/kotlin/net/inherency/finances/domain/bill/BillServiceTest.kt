package net.inherency.finances.domain.bill

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.controller.dto.BillReportDTO
import net.inherency.finances.domain.transaction.CategorizedTransaction
import net.inherency.finances.domain.transaction.TransactionService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

class BillServiceTest {

    private val billRepository: BillRepository = mock()
    private val transactionService: TransactionService = mock()
    private val billService = BillService(billRepository, transactionService)

    data class BillArgs (
            val waterAccountId: Int = 1,
            val waterDueDayOfMonth: Int = 1,
            val waterVisibleInReports: Boolean = true,
            val waterBillPayDate: LocalDate = LocalDate.of(2020, 8, 20),

            val rentAccountId: Int = 2,
            val rentDueDayOfMonth: Int = 1,
            val rentVisibleInReports: Boolean = true,
            val rentBillPayDate: LocalDate = LocalDate.of(2020, 8, 21)
    )

    private fun generateBillToPaymentDate(billArgs: BillArgs = BillArgs()): Map<BillData, LocalDate> {
        val waterBill = BillData(billArgs.waterAccountId, "Water", "Water Bill", billArgs.waterDueDayOfMonth,
                autoPayEnabled = true, showInUIReports = true)
        val rentBill = BillData(billArgs.rentAccountId, "Rent", "Rent Bill", billArgs.rentDueDayOfMonth,
                autoPayEnabled = false, showInUIReports = true)
        return mapOf(
                waterBill to billArgs.waterBillPayDate,
                rentBill to billArgs.rentBillPayDate
        )
    }

    @Test
    fun `findAllBillsReportableViaUI succeeds when all data is valid`() {
        val billPaymentInfo = generateBillToPaymentDate()
        whenever(billRepository.readAll()).thenReturn(billPaymentInfo.keys.toList())

        whenever(transactionService.listAllCategorizedTransactions()).thenReturn(
                billPaymentInfo.map {
                    CategorizedTransaction(UUID.randomUUID(), it.value, 1, "", "",
                            it.key.accountId, 10, 101, 101, true)
                }
        )

        val result = billService.findAllBillsReportableViaUI()

        val expectedResultList = billPaymentInfo.map {
            BillReportDTO(it.key.accountId, it.key.name, it.key.description, it.key.dueDayOfMonth,
                    it.key.autoPayEnabled, it.value)
        }

        assertEquals(expectedResultList, result)
    }

    @Test
    fun `findAllBillsReportableViaUI fails when more than one bill was set with the same account id`() {
        val billPaymentInfo = generateBillToPaymentDate(BillArgs(
                waterAccountId = 1,
                rentAccountId = 1
        ))
        whenever(billRepository.readAll()).thenReturn(billPaymentInfo.keys.toList())

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI fails when a bill is set to be due on a day less than 1 (an invalid day of the month)`() {
        val billPaymentInfo = generateBillToPaymentDate(BillArgs(
                waterDueDayOfMonth = 0
        ))
        whenever(billRepository.readAll()).thenReturn(billPaymentInfo.keys.toList())

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI fails when a bill is set to be due on a day greater than 31 (an invalid day of the month)`() {
        val billPaymentInfo = generateBillToPaymentDate(BillArgs(
                waterDueDayOfMonth = 32
        ))
        whenever(billRepository.readAll()).thenReturn(billPaymentInfo.keys.toList())

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI filters out bills that are not supposed to show in UI reports`() {
        val billPaymentInfo = generateBillToPaymentDate()
        val hiddenOldCreditCardBill = BillData(3, "Old CC", "Old Card", 10, autoPayEnabled = false, showInUIReports = false)
        val bills = listOf(billPaymentInfo.entries.toList()[0].key, billPaymentInfo.entries.toList()[1].key, hiddenOldCreditCardBill)
        whenever(billRepository.readAll()).thenReturn(bills)
        whenever(transactionService.listAllCategorizedTransactions()).thenReturn(
                billPaymentInfo.map {
                    CategorizedTransaction(UUID.randomUUID(), it.value, 1, "", "",
                            it.key.accountId, 10, 101, 101, true)
                }.plus(CategorizedTransaction(UUID.randomUUID(), LocalDate.now(), 13, "", "",
                        hiddenOldCreditCardBill.accountId, 10, 101, 101, true))
        )

        val result = billService.findAllBillsReportableViaUI()

        val expectedResultList = billPaymentInfo.map {
            BillReportDTO(it.key.accountId, it.key.name, it.key.description, it.key.dueDayOfMonth,
                    it.key.autoPayEnabled, it.value)
        }
        assertEquals(expectedResultList, result)
    }

    @Test
    fun `findAllBillsReportableViaUI does not fail when a bill has bad dueDayOfMonth data if it is hidden from reports`() {
        val billPaymentInfo = generateBillToPaymentDate()
        val hiddenOldCreditCardBill = BillData(3, "Old CC", "Old Card", 32, autoPayEnabled = false, showInUIReports = false)
        val bills = listOf(billPaymentInfo.entries.toList()[0].key, billPaymentInfo.entries.toList()[1].key, hiddenOldCreditCardBill)
        whenever(billRepository.readAll()).thenReturn(bills)

        assertDoesNotThrow { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI reports correct last payment date`() {
        val oldestTransactionDate = LocalDate.of(2020, 8, 1)
        val middleTransactionDate = LocalDate.of(2020, 8, 3)
        val mostRecentTransactionDate = LocalDate.of(2020, 8, 9)
        val rentAccountId = 1
        val waterAccountId = 2
        val billPaymentInfo = generateBillToPaymentDate(
                BillArgs(
                        rentAccountId = rentAccountId, rentBillPayDate = middleTransactionDate,
                        waterAccountId = waterAccountId, waterBillPayDate = middleTransactionDate))
        whenever(billRepository.readAll()).thenReturn(billPaymentInfo.keys.toList())

        whenever(transactionService.listAllCategorizedTransactions()).thenReturn(
                billPaymentInfo.map {
                    CategorizedTransaction(UUID.randomUUID(), oldestTransactionDate, 1, "", "",
                            it.key.accountId, 10, 101, 101, true)
                }.plus(CategorizedTransaction(UUID.randomUUID(), mostRecentTransactionDate, 1, "", "",
                        rentAccountId, 10, 101, 101, true))
                .plus(CategorizedTransaction(UUID.randomUUID(), mostRecentTransactionDate, 1, "", "",
                        waterAccountId, 10, 101, 101, true))
        )

        val result = billService.findAllBillsReportableViaUI()

        val expectedResultList = billPaymentInfo.map {
            BillReportDTO(it.key.accountId, it.key.name, it.key.description, it.key.dueDayOfMonth,
                    it.key.autoPayEnabled, mostRecentTransactionDate)
        }

        assertEquals(expectedResultList, result)
    }

}