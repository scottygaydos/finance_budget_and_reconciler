package net.inherency.finances.domain.transaction

import com.nhaarman.mockitokotlin2.*
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
import net.inherency.finances.external.mint.MintFileParser
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val mintClient: MintClient = mock()

    private val transactionService = TransactionService(transactionRepository, mintClient)

    @Test
    fun `GIVEN a list of mint transactions will come from an update, when we update mint transactions, then all existing data is cleared before inserting new sorted data`() {
        //GIVEN
        val updatedTransactions = listOf(
            MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 2), "first", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 1), "second", "", 0, CreditOrDebit.DEBIT, "", "")
        )
        whenever(mintClient.downloadAllTransactions()).thenReturn(updatedTransactions)

        //WHEN
        transactionService.updateMintTransactions()

        //THEN
        verify(transactionRepository).clearAllExistingMintTransactions()
    }

    @Test
    fun `GIVEN a list of mint transactions will come from an update, when we update mint transactions, then the transactions are sorted by descending date and inserted after a header`() {
        //GIVEN
        val updatedTransactions = listOf(
            MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", "")
        )
        whenever(mintClient.downloadAllTransactions()).thenReturn(updatedTransactions)

        //WHEN
        transactionService.updateMintTransactions()

        //THEN
        val sortedTransactions = listOf(
            MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", ""),
            MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", "")
        )
        verify(transactionRepository).writeHeaderAndAllTransactions(
                TabName.MINT_TRANSACTIONS, MintFileParser.HEADER_LINE_VALUES.toMutableList(), sortedTransactions)
    }

    @Test
    fun `GIVEN a list of mint transactions will come from an update, when we update mint transactions, then we report those transactions sorted by descending date`() {
        //GIVEN
        val updatedTransactions = listOf(
                MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", "")
        )
        whenever(mintClient.downloadAllTransactions()).thenReturn(updatedTransactions)

        //WHEN
        val response = transactionService.updateMintTransactions()

        //THEN
        val sortedTransactions = listOf(
                MintTransaction(LocalDate.of(2020, 1, 3), "third", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 2), "second", "", 0, CreditOrDebit.DEBIT, "", ""),
                MintTransaction(LocalDate.of(2020, 1, 1), "first", "", 0, CreditOrDebit.DEBIT, "", "")
        )
        assertEquals(sortedTransactions, response)
    }

}