package net.inherency.finances.domain.bill

import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BillRepositoryGoogleSheetImplTest {

    private lateinit var billRepository: BillRepository

    @Mock
    private lateinit var googleSheetClient: GoogleSheetClient

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        billRepository = BillRepositoryGoogleSheetImpl(googleSheetClient)
    }

    @Test
    fun `Repository should successfully map rows, ignoring the first row because it is a header`() {
        val input = listOf(
                listOf("Account Id", "Name", "Description", "Due Day Of Month", "AutoPay Enabled?", "Show In UI Reports?"),
                listOf("7", "Water", "Water Bill", "16", "FALSE", "TRUE"),
                listOf("13", "HOA", "HOA Bill", "1", "TRUE", "FALSE")
        )

        whenever(googleSheetClient.listValuesInTab(TabName.BILLS)).thenReturn(input)

        val result = billRepository.readAll()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(BillData(7, "Water", "Water Bill", 16, autoPayEnabled = false, showInUIReports = true),
                result[0])
        Assertions.assertEquals(BillData(13, "HOA", "HOA Bill", 1, autoPayEnabled = true, showInUIReports = false),
                result[1])
    }

}