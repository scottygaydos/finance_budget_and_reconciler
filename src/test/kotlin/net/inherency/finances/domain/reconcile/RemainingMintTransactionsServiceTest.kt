package net.inherency.finances.domain.reconcile

import com.nhaarman.mockitokotlin2.*
import net.inherency.finances.CommandLineService
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.reconcile.RemainingMintTransactionsService.Companion.AFFIRMATIVE_ANSWERS
import net.inherency.finances.domain.reconcile.rule.BudgetCategoryRule
import net.inherency.finances.domain.reconcile.rule.BudgetCategoryRuleService
import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.domain.transaction.TransactionService
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RemainingMintTransactionsServiceTest {

    private val accountService: AccountService = mock()
    private val commandLineService: CommandLineService = mock()
    private val transactionService: TransactionService = mock()
    private val budgetCategoryService: BudgetCategoryService = mock()
    private val budgetCategoryRuleService: BudgetCategoryRuleService = mock()
    private val debitAndCreditAccountFactory: DebitAndCreditAccountFactory = DebitAndCreditAccountFactory()

    private val remainingMintTransactionsService = RemainingMintTransactionsService(
            accountService, commandLineService, transactionService, budgetCategoryService, budgetCategoryRuleService,
            debitAndCreditAccountFactory
    )

    @Test
    fun `Given no remaining mint transactions, when we prompt and handle remaining mint transactions, then we do nothing`() {
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(emptyList())

        verifyZeroInteractions(accountService)
        verifyZeroInteractions(commandLineService)
        verifyZeroInteractions(transactionService)
        verifyZeroInteractions(budgetCategoryService)
    }

    @Test
    fun `Given one remaining mint transaction and all configurations are available and match, when we prompt and handle remaining mint transactions, then we categorize the transaction successfully`() {
        //GIVEN:
        //A matching account to debit and the global external account to credit can be found
        val checkingAccount = Account(1, "Checking", "Checking Account", "Checking", "",
                canManuallyCredit = true, canManuallyDebit = true)
        val globalExternalAccountToDebit = Account(2, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME,
                "Generic account to represent other parties", GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "",
                canManuallyCredit = true, canManuallyDebit = true)
        whenever(accountService.readAll()).thenReturn(listOf(checkingAccount, globalExternalAccountToDebit))

        //When prompted, user chooses "Spending" by inputting "y" to confirm desire to categorize, then "s"
        doReturn("y", "s").whenever(commandLineService).readFromCommandLine()

        //The "Spending" budget category can be found
        val spendingCategory = BudgetCategoryData(1, "Spending", "Spending Category")
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(spendingCategory))

        //One remaining mint transaction
        val remainingMintTransaction = MintTransaction(LocalDate.of(2020, 8, 4), "Refund", "Purchase Refund", 1200,
                CreditOrDebit.CREDIT, "Shopping", "Checking")
        val remainingMintTransactions = listOf(remainingMintTransaction)

        //WHEN
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(remainingMintTransactions)

        //THEN
        verify(transactionService).createCategorizedTransactionFromMintTransaction(checkingAccount,
        globalExternalAccountToDebit, remainingMintTransaction, spendingCategory)
    }

    @Test
    fun `Given one remaining mint transaction and all configurations are available and match, when we prompt and handle remaining mint transactions, then we categorize the transaction successfully after navigating the input options incorrectly, then correctly`() {
        //GIVEN:
        //A matching account to debit and the global external account to credit can be found
        val creditCardAccount = Account(1, "Credit Card x1111", "Personal Credit Card", "Credit Card (1111)", "",
                canManuallyCredit = true, canManuallyDebit = true)
        val globalExternalAccountToCredit = Account(2, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME,
                "Generic account to represent other parties", GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "",
                canManuallyCredit = true, canManuallyDebit = true)
        whenever(accountService.readAll()).thenReturn(listOf(creditCardAccount, globalExternalAccountToCredit))

        //When prompted, user chooses "Spending" by inputting affirmative to confirm desire to categorize,
        // then a series of incorrect options, followed by "s" for spending
        doReturn(AFFIRMATIVE_ANSWERS[0], "x", "2145254324", "s").whenever(commandLineService).readFromCommandLine()

        //The "Spending" budget category can be found
        val spendingCategory = BudgetCategoryData(1, "Spending", "Spending Category")
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(spendingCategory))

        //One remaining mint transaction
        val remainingMintTransaction = MintTransaction(LocalDate.of(2020, 8, 4), "Target", "Target Purchase", 1200,
                CreditOrDebit.DEBIT, "Shopping", "Credit Card (1111)")
        val remainingMintTransactions = listOf(remainingMintTransaction)

        //WHEN
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(remainingMintTransactions)

        //THEN
        verify(transactionService).createCategorizedTransactionFromMintTransaction(globalExternalAccountToCredit,
                creditCardAccount, remainingMintTransaction, spendingCategory)
    }

    @Test
    fun `Given one remaining mint transaction and all configurations are available and match, when we prompt to handle the remaining transaction and the user chooses not to handle, then we do not create a categorized transaction`() {
        //GIVEN:
        //A matching account to debit and the global external account to credit can be found
        val creditCardAccount = Account(1, "Credit Card x1111", "Personal Credit Card", "Credit Card (1111)", "",
                canManuallyCredit = true, canManuallyDebit = true)
        val globalExternalAccountToCredit = Account(2, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME,
                "Generic account to represent other parties", GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "",
                canManuallyCredit = true, canManuallyDebit = true)
        whenever(accountService.readAll()).thenReturn(listOf(creditCardAccount, globalExternalAccountToCredit))

        //When prompted, user chooses not to categorize the mint transaction
        doReturn("n").whenever(commandLineService).readFromCommandLine()

        //The "Spending" budget category can be found
        val spendingCategory = BudgetCategoryData(1, "Spending", "Spending Category")
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(spendingCategory))

        //One remaining mint transaction
        val remainingMintTransaction = MintTransaction(LocalDate.of(2020, 8, 4), "Target", "Target Purchase", 1200,
                CreditOrDebit.DEBIT, "Shopping", "Credit Card (1111)")
        val remainingMintTransactions = listOf(remainingMintTransaction)

        //WHEN
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(remainingMintTransactions)

        //THEN
        verifyZeroInteractions(transactionService)
    }

    @Test
    fun `Given two remaining mint transactions and all configurations are available and match, when we prompt and handle remaining mint transactions and choose to ignore the first and categorize the second, then we categorize only the second transaction successfully`() {
        //GIVEN:
        //A matching account to debit and the global external account to credit can be found
        val creditCardAccount = Account(1, "Credit Card x1111", "Personal Credit Card", "Credit Card (1111)", "",
                canManuallyCredit = true, canManuallyDebit = true)
        val globalExternalAccountToCredit = Account(2, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME,
                "Generic account to represent other parties", GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "",
                canManuallyCredit = true, canManuallyDebit = true)
        whenever(accountService.readAll()).thenReturn(listOf(creditCardAccount, globalExternalAccountToCredit))

        //When prompted, user "n" for no for the first transaction, the affirmative for the second transaction.
        //The user chooses "Spending" by inputting "s"
        doReturn("n", AFFIRMATIVE_ANSWERS[1], "s").whenever(commandLineService).readFromCommandLine()

        //The "Spending" budget category can be found
        val spendingCategory = BudgetCategoryData(1, "Spending", "Spending Category")
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(spendingCategory))

        //Two remaining mint transactions
        val remainingMintTransactionOne = MintTransaction(LocalDate.of(2020, 8, 4), "Target", "Target - Candy", 100,
                CreditOrDebit.DEBIT, "Shopping", "Credit Card (1111)")
        val remainingMintTransactionTwo = MintTransaction(LocalDate.of(2020, 8, 4), "Target", "Target - Coffee", 500,
                CreditOrDebit.DEBIT, "Shopping", "Credit Card (1111)")
        val remainingMintTransactions = listOf(remainingMintTransactionOne, remainingMintTransactionTwo)

        //WHEN
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(remainingMintTransactions)

        //THEN
        verify(transactionService).createCategorizedTransactionFromMintTransaction(globalExternalAccountToCredit,
                creditCardAccount, remainingMintTransactionTwo, spendingCategory)
        verify(transactionService, times(0)).createCategorizedTransactionFromMintTransaction(
                globalExternalAccountToCredit, creditCardAccount, remainingMintTransactionOne, spendingCategory)
    }

    @Test
    fun `Given one remaining mint transaction and all configurations are available and match, when the system checks the rules and finds a match, the  system categorizes the transaction automatically`() {
        //GIVEN:
        //A matching account to debit and the global external account to credit can be found
        val checkingAccount = Account(1, "Checking", "Checking Account", "Checking", "",
                canManuallyCredit = true, canManuallyDebit = true)
        val globalExternalAccount = Account(2, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME,
                "Generic account to represent other parties", GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "",
                canManuallyCredit = true, canManuallyDebit = true)
        whenever(accountService.readAll()).thenReturn(listOf(checkingAccount, globalExternalAccount))

        //The system finds a matching rule for the transaction and category
        val spendingCategory = BudgetCategoryData(1, "Spending", "Spending Category")
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(spendingCategory))
        whenever(budgetCategoryRuleService.findMatchingRuleForAutoCategorization(any())).thenReturn(BudgetCategoryRule(
                spendingCategory, checkingAccount, globalExternalAccount))

        //One remaining mint transaction
        val remainingMintTransaction = MintTransaction(LocalDate.of(2020, 8, 4), "Refund", "Purchase Refund", 1200,
                CreditOrDebit.CREDIT, "Shopping", "Checking")
        val remainingMintTransactions = listOf(remainingMintTransaction)

        //WHEN
        remainingMintTransactionsService.promptAndHandleRemainingMintTransactions(remainingMintTransactions)

        //THEN
        verify(transactionService).createCategorizedTransactionFromMintTransaction(globalExternalAccount,
                checkingAccount, remainingMintTransaction, spendingCategory)

        //No input was required
        verifyZeroInteractions(commandLineService)
    }

}