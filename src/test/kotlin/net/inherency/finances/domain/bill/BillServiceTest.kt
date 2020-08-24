package net.inherency.finances.domain.bill

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BillServiceTest {

    private val billRepository: BillRepository = mock()
    private val billService = BillService(billRepository)

    @Test
    fun `findAllBillsReportableViaUI succeeds when all data is valid`() {
        val bills = listOf(
                BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true),
                BillData(2, "Rent", "Rent Bill", 1, autoPayEnabled = false, showInUIReports = true)
        )
        whenever(billRepository.readAll()).thenReturn(bills)

        val result = billService.findAllBillsReportableViaUI()

        assertEquals(bills, result)
    }

    @Test
    fun `findAllBillsReportableViaUI fails when more than one bill was set with the same account id`() {
        val bills = listOf(
                BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true),
                BillData(1, "Rent", "Rent Bill", 1, autoPayEnabled = false, showInUIReports = true)
        )
        whenever(billRepository.readAll()).thenReturn(bills)

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI fails when a bill is set to be due on a day less than 1 (an invalid day of the month)`() {
        val bills = listOf(
                BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true),
                BillData(2, "Rent", "Rent Bill", 0, autoPayEnabled = false, showInUIReports = true)
        )
        whenever(billRepository.readAll()).thenReturn(bills)

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI fails when a bill is set to be due on a day greater than 31 (an invalid day of the month)`() {
        val bills = listOf(
                BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true),
                BillData(2, "Rent", "Rent Bill", 32, autoPayEnabled = false, showInUIReports = true)
        )
        whenever(billRepository.readAll()).thenReturn(bills)

        assertThrows<IllegalStateException> { billService.findAllBillsReportableViaUI() }
    }

    @Test
    fun `findAllBillsReportableViaUI filters out bills that are not supposed to show in UI reports`() {
        val visibleWaterBill = BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true)
        val visibleRentBill = BillData(2, "Rent", "Rent Bill", 1, autoPayEnabled = false, showInUIReports = true)
        val hiddenOldCreditCardBill = BillData(3, "Old CC", "Old Card", 10, autoPayEnabled = false, showInUIReports = false)
        val bills = listOf(visibleWaterBill, visibleRentBill, hiddenOldCreditCardBill)
        whenever(billRepository.readAll()).thenReturn(bills)

        val result = billService.findAllBillsReportableViaUI()

        val expectedList = listOf(visibleWaterBill, visibleRentBill)
        assertEquals(expectedList, result)
    }

    @Test
    fun `findAllBillsReportableViaUI does not fail when a bill has bad dueDayOfMonth data if it is hidden from reports`() {
        val bills = listOf(
                BillData(1, "Water", "Water Bill", 2, autoPayEnabled = true, showInUIReports = true),
                BillData(1, "Rent", "Rent Bill", -1, autoPayEnabled = false, showInUIReports = false)
        )
        whenever(billRepository.readAll()).thenReturn(bills)

        assertDoesNotThrow { billService.findAllBillsReportableViaUI() }
    }

}