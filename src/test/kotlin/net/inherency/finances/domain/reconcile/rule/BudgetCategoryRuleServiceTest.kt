package net.inherency.finances.domain.reconcile.rule

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.budget.category.BudgetCategoryData
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.domain.transaction.MintTransaction
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetCategoryRuleServiceTest {

    private val budgetCategoryRuleRepository: BudgetCategoryRuleRepository = mock()
    private val accountService: AccountService = mock()
    private val budgetCategoryService: BudgetCategoryService = mock()

    private val budgetCategoryRuleService = BudgetCategoryRuleService(
            budgetCategoryRuleRepository, accountService, budgetCategoryService)

    @Test
    fun `A transaction that matches a rule by description will return the rule`() {
        //GIVEN
        val description = "testDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), description, "_",
        123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(description, ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(ruleCreditAccountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertEquals(categoryName, rule?.category?.name)
    }

    @Test
    fun `A transaction that matches a rule by ANY description and debit account will return the rule`() {
        //GIVEN
        val description = "This will be ignored"
        val accountNameInMintTx = "Paypal Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), description, "_",
            123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleDebitAccountId = 3
        val ruleBudgetCategoryId = 16
        val categoryName = "Non-Budget Transaction"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
            BudgetCategoryRuleData(description, null, ruleDebitAccountId, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
            Account(1, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
            Account(ruleDebitAccountId, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
            BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Transaction that does not affect a budget")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertEquals(categoryName, rule?.category?.name)
    }

    @Test
    fun `A transaction that matches a rule by ANY description and credit account will return the rule`() {
        //GIVEN
        val description = "This will be ignored"
        val accountNameInMintTx = "Savings Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), description, "_",
            123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 25
        val ruleBudgetCategoryId = 16
        val categoryName = "Non-Budget Transaction"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
            BudgetCategoryRuleData(description, ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
            Account(1, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
            Account(ruleCreditAccountId, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
            BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Transaction that does not affect a budget")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertEquals(categoryName, rule?.category?.name)
    }

    @Test
    fun `A transaction that matches a rule by original description will return the rule`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(ruleCreditAccountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertEquals(categoryName, rule?.category?.name)
    }

    @Test
    fun `Return null when no rule match is found`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData("different description", ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(ruleCreditAccountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

    @Test
    fun `Return null when rule is misconfigured to not provide either a credit account id or debit account id`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val accountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, null, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(accountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

    @Test
    fun `Return null when rule is misconfigured to provide both a credit account id and debit account id`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleDebitAccountId = 4
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, ruleCreditAccountId, ruleDebitAccountId, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(ruleCreditAccountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

    @Test
    fun `Return null when rule is configured to provide a credit account id, but there is no account for that id`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(4, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

    @Test
    fun `Return null when rule is configured to provide a debit account id, but there is no account for that id`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleDebitAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, null, ruleDebitAccountId, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(4, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(ruleBudgetCategoryId, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

    @Test
    fun `Return null when rule is configured to use a budget category id, but there is no category for that id`() {
        //GIVEN
        val originalDescription = "testOriginalDescriptionValue"
        val accountNameInMintTx = "Checking Account"
        val mintTx = MintTransaction(LocalDate.of(2020, 8, 9), "_", originalDescription,
                123, CreditOrDebit.DEBIT, "", accountNameInMintTx)

        val ruleCreditAccountId = 3
        val ruleBudgetCategoryId = 7
        val categoryName = "Food"
        whenever(budgetCategoryRuleRepository.readAll()).thenReturn(listOf(
                BudgetCategoryRuleData(originalDescription, ruleCreditAccountId, null, ruleBudgetCategoryId)
        ))
        whenever(accountService.readAll()).thenReturn(listOf(
                Account(ruleCreditAccountId, GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME, "", "", "", true, canManuallyDebit = true),
                Account(1, accountNameInMintTx, "", accountNameInMintTx, "", true, canManuallyDebit = true)
        ))
        whenever(budgetCategoryService.readAll()).thenReturn(listOf(
                BudgetCategoryData(8, categoryName, "Food Category")
        ))

        //WHEN
        val rule = budgetCategoryRuleService.findMatchingRuleForAutoCategorization(mintTx)

        //THEN
        assertNull(rule)
    }

}