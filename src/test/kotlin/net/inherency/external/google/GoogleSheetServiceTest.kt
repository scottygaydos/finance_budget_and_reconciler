package net.inherency.external.google

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.external.mint.MintFileParser
import net.inherency.external.mint.MintTransactionFactory
import net.inherency.external.mint.CreditOrDebit
import net.inherency.external.mint.MintTransaction
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class GoogleSheetServiceTest {

    private val googleSheetClient = mock<GoogleSheetClient>()
    private val mintTransactionFactory = MintTransactionFactory()
    private val googleSheetService = GoogleSheetService(googleSheetClient, mintTransactionFactory)

    @Test
    fun `GIVEN the transactions tab and a list of mint transactions, when we call clearTabAndWriteAllValuesToSheetAfterSorting, then all existing data is cleared before inserting new data`() {
        //GIVEN
        val tabName = TabName.MINT_TRANSACTIONS
        val mintTransactions = listOf(
                MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", "")
        )

        //WHEN
        googleSheetService.clearTabAndWriteAllValuesToSheetAfterSorting(tabName, mintTransactions)

        //THEN
        val inOrder = inOrder(googleSheetClient)
        inOrder.verify(googleSheetClient).clearAllDataInTab(tabName)
        inOrder.verify(googleSheetClient).writeAllValuesToTab(eq(tabName), any())
    }

    @Test
    fun `GIVEN the transactions tab and a list of mint transactions, when we call clearTabAndWriteAllValuesToSheetAfterSorting, the transactions are sorted and inserted after a header`() {
        //GIVEN
        val tabName = TabName.MINT_TRANSACTIONS
        val mintTransactions = listOf(
                MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", "")
        )

        //WHEN
        googleSheetService.clearTabAndWriteAllValuesToSheetAfterSorting(tabName, mintTransactions)

        //THEN
        val expectedFileOutput = listOf(
                MintFileParser.HEADER_LINE_VALUES,
                listOf("2020-01-03", "third", "", "0", "DEBIT","",""),
                listOf("2020-01-02", "second", "", "0", "DEBIT","",""),
                listOf("2020-01-01", "first", "", "0", "DEBIT","",""))
        argumentCaptor<List<List<String>>>().apply {
            verify(googleSheetClient).writeAllValuesToTab(eq(tabName), capture())
            assertEquals(expectedFileOutput, firstValue)
        }
    }

    @Test
    fun `GIVEN transaction records with a header exist in the sheet WHEN we call listAllMintTransactionsInTransactionsTab THEN we report those records without the header`() {
        //GIVEN
        //1. find data in sheet (mock client)
        whenever(googleSheetClient.listValuesInTab(TabName.MINT_TRANSACTIONS)).thenReturn(
                listOf(
                        MintFileParser.HEADER_LINE_VALUES,
                        listOf("2020-01-03", "third", "", "0", "DEBIT","",""),
                        listOf("2020-01-02", "second", "", "0", "DEBIT","",""),
                        listOf("2020-01-01", "first", "", "0", "DEBIT","",""))
        )

        //WHEN
        val result = googleSheetService.listAllMintTransactionsInTransactionsTab()

        //THEN
        assertEquals(3, result.size)

        assertEquals(LocalDate.of(2020, 1, 3), result[0].date)
        assertEquals("third", result[0].description)

        assertEquals(LocalDate.of(2020, 1, 2), result[1].date)
        assertEquals("second", result[1].description)

        assertEquals(LocalDate.of(2020, 1, 1), result[2].date)
        assertEquals("first", result[2].description)

    }

}