package net.inherency.finances.domain.reconcile

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.transaction.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class ReconcileServiceTest {

    private val transactionService: TransactionService = mock()
    private val accountService: AccountService = mock()

    private val reconcileService = ReconcileService(transactionService, accountService)

    @Test
    fun `Mint and categorized transactions already reconciled do not need to reconcile again`() {
        //GIVEN: A mint transaction
        val txDate = LocalDate.of(2020, 6, 10)
        val description = "Paycheck"
        val originalDescription = "Employer Name Paycheck"
        val amount = 134535
        val credit = CreditOrDebit.CREDIT
        val category = "Income"
        val accountName = "Checking Account"
        val mintTx = MintTransaction(txDate, description, originalDescription, amount, credit, category, accountName)
        whenever(transactionService.updateMintTransactions(true)).thenReturn(listOf(mintTx))

        //AND: A categorized transaction
        val categorizedTx = CategorizedTransaction(
                UUID.randomUUID(), txDate, 1, description, originalDescription, 1, 2, amount, amount, true)
        whenever(transactionService.listAllCategorizedTransactions()).thenReturn(listOf(categorizedTx))

        //AND: These two transactions have reconciled to match each other
        val reconciledTransaction = ReconciledTransaction(mintTx.getIdAsString(), categorizedTx.id)
        whenever(transactionService.listAllReconciledTransactions()).thenReturn(listOf(reconciledTransaction))

        //WHEN: We reconcile the transactions
        val result = reconcileService.reconcile(true)

        //THEN: The transactions are already categorized
        Assertions.assertEquals(1, result.reconciledTransactions.size)
        Assertions.assertEquals(0, result.unreconciledCategorizedTransactions.size)
        Assertions.assertEquals(0, result.unreconciledMintTransactions.size)
        Assertions.assertEquals(Pair(categorizedTx, mintTx), result.reconciledTransactions.toList()[0])
    }

    @Test
    fun `Mint and categorized transactions that are not yet reconciled report as not reconciled`() {
        //GIVEN: A mint transaction
        val txDate = LocalDate.of(2020, 6, 10)
        val description = "Paycheck"
        val originalDescription = "Employer Name Paycheck"
        val credit = CreditOrDebit.CREDIT
        val category = "Income"
        val accountName = "Checking Account"
        val mintTx = MintTransaction(txDate, description, originalDescription, 111, credit, category, accountName)
        whenever(transactionService.updateMintTransactions(true)).thenReturn(listOf(mintTx))
        //AND: Both the mint transaction has an identifiable account
        val accountId = 1
        whenever(accountService.readAll()).thenReturn(
                listOf(Account(accountId, accountName, accountName, accountName, accountName, canManuallyCredit = true,
                        canManuallyDebit = true)))

        //AND: A categorized transaction
        val categorizedTx = CategorizedTransaction(
                UUID.randomUUID(), txDate, 1, description, originalDescription, accountId, 2, 222, 333, true)
        whenever(transactionService.listAllCategorizedTransactions()).thenReturn(listOf(categorizedTx))

        //AND: These two transactions have not reconciled
        whenever(transactionService.listAllReconciledTransactions()).thenReturn(emptyList())

        //WHEN: We reconcile the transactions
        //AND: We do not reconcile these transactions to match anything
        val result = reconcileService.reconcile(true)

        //THEN: Both transactions are reported as unreconciled
        Assertions.assertEquals(0, result.reconciledTransactions.size)
        Assertions.assertEquals(1, result.unreconciledCategorizedTransactions.size)
        Assertions.assertEquals(1, result.unreconciledMintTransactions.size)
        Assertions.assertEquals(mintTx, result.unreconciledMintTransactions[0])
        Assertions.assertEquals(categorizedTx, result.unreconciledCategorizedTransactions[0])
    }

}