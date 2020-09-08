package net.inherency.finances.domain.transaction

import com.nhaarman.mockitokotlin2.*
import net.inherency.finances.controller.dto.CreateTransactionCmd
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.external.google.TabName
import net.inherency.finances.external.mint.MintClient
import net.inherency.finances.external.mint.MintFileParser
import net.inherency.finances.util.DateTimeService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val mintClient: MintClient = mock()
    private val dateTimeService: DateTimeService = mock()
    private val budgetCategoryService: BudgetCategoryService = mock()
    private val accountService: AccountService = mock()

    private val transactionService = TransactionService(
            transactionRepository, mintClient, dateTimeService, budgetCategoryService, accountService)

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

    @Test
    fun `Create a transaction from DTO where all values are provided`() {
        //GIVEN
        val cmd = CreateTransactionCmd("2020-09-07", 1, "gas", 2, 3, "12.34", "12.35", true)
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(BudgetCategoryData(1, "gas", "gas")))

        //WHEN
        transactionService.create(cmd)

        //THEN
        argumentCaptor<CategorizedTransaction>().apply {
            verify(transactionRepository).addCategorizedTransactionRow(capture())
            assertEquals(LocalDate.of(2020, 9, 7), firstValue.date)
            assertEquals(cmd.transactionTypeId, firstValue.budgetCategoryId)
            assertEquals(cmd.description, firstValue.description)
            assertEquals(cmd.description, firstValue.bankPayee)
            assertEquals(cmd.creditAccountId, firstValue.creditAccountId)
            assertEquals(cmd.debitAccountId, firstValue.debitAccountId)
            assertEquals(1234, firstValue.authorizedAmount)
            assertEquals(1235, firstValue.settledAmount)
            assertEquals(true, firstValue.reconcilable)
        }
    }

    @Test
    fun `Create a transaction from DTO where date not provided uses dateTimeService`() {
        //GIVEN
        val cmd = CreateTransactionCmd(null, 1, "gas", 2, 3, "12.34", "12.35", true)
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(BudgetCategoryData(1, "gas", "gas")))
        val defaultTxDate = LocalDate.of(2019, 2, 3)
        whenever(dateTimeService.now()).thenReturn(defaultTxDate)

        //WHEN
        transactionService.create(cmd)

        //THEN
        verify(dateTimeService).now()
        argumentCaptor<CategorizedTransaction>().apply {
            verify(transactionRepository).addCategorizedTransactionRow(capture())
            assertEquals(defaultTxDate, firstValue.date)
        }
    }

    @Test
    fun `Create a transaction from DTO where settled amount not provided uses authorized amount as replacement`() {
        //GIVEN
        val cmd = CreateTransactionCmd("2020-09-07", 1, "gas", 2, 3, "12.34", null, true)
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(BudgetCategoryData(1, "gas", "gas")))

        //WHEN
        transactionService.create(cmd)

        //THEN
        argumentCaptor<CategorizedTransaction>().apply {
            verify(transactionRepository).addCategorizedTransactionRow(capture())
            assertEquals(1234, firstValue.authorizedAmount)
            assertEquals(1234, firstValue.settledAmount)
        }
    }

    @Test
    fun `Create a transaction from DTO fails when it provides an invalid budget category id`() {
        //GIVEN
        val cmd = CreateTransactionCmd("2020-09-07", -1, "gas", 2, 3, "12.34", null, true)
        whenever(budgetCategoryService.readAll()).thenReturn(emptyList())

        //WHEN and THEN
        assertThrows<IllegalArgumentException> {
            transactionService.create(cmd)
        }
    }
}