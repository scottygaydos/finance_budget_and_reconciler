package net.inherency.finances.integration

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.tomakehurst.wiremock.WireMockServer
import com.nhaarman.mockitokotlin2.*
import net.inherency.finances.CommandLineService
import net.inherency.finances.ReconcileCommandLineRunner
import net.inherency.finances.config.ConfigKey
import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.domain.account.AccountService.Companion.GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME
import net.inherency.finances.domain.reconcile.rule.BudgetCategoryRuleRepository
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.format.datetime.standard.DateTimeFormatterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class ReconcileAndCategorizeTransactionsTest {

    @Autowired
    private lateinit var reconcileCommandLineRunner: ReconcileCommandLineRunner

    @Autowired
    private lateinit var budgetCategoryRuleRepository: BudgetCategoryRuleRepository

    @SpyBean
    private lateinit var configurationService: ConfigurationService

    @MockBean
    private lateinit var googleSheetClient: GoogleSheetClient

    private lateinit var wireMock: WireMockServer

    @MockBean
    private lateinit var commandLineService: CommandLineService

    @AfterEach
    fun teardown() {
        this.wireMock.stop()
    }

    @BeforeEach
    fun beforeAll() {
        val wireMock = WireMockServer(PORT)
        wireMock.start()
        this.wireMock = wireMock

        //This allows us to continue setting non-sensitive variables in application.yml, but programmatically set
        //sensitive environment variables so we don't have to document/teach other devs/testers
        //how to configure their environment to run this test.
        doReturn("12345@!@$%").whenever(configurationService).getString(ConfigKey.GOOGLE_SHEET_ID)
        doReturn("finances").whenever(configurationService).getString(ConfigKey.GOOGLE_APP_NAME)
        doReturn("{key:val}").whenever(configurationService).getString(ConfigKey.GOOGLE_AUTH_JSON)
        doReturn("fake@fake.com").whenever(configurationService).getString(ConfigKey.MINT_LOGIN_EMAIL)
        doReturn("very_secure_password").whenever(configurationService).getString(ConfigKey.MINT_LOGIN_PASSWORD)

        //Clear rules cache
        budgetCategoryRuleRepository.readAll(true)
    }

    @Test
    @DisabledIfSystemProperty(named = "testEnvironment", matches = "ci")
    fun `System will use command line to categorize mint transactions that are not yet reconciled`() {
        //GIVEN: Mint website requires login and has three transactions to report
        stubMintLogin(wireMock)
        stubDownloadMintTransactionFileResponse(wireMock, "ReconcileAndCategorizeTransactionsTest")

        //AND: The Google sheets categorized transactions tab already has one transaction categorized
        whenever(googleSheetClient.listValuesInTab(TabName.CATEGORIZED_TRANSACTIONS)).thenReturn(categorizedTransactionsFromGoogleSheet)

        //AND: Accounts exist for these transactions as well as categories
        whenever(googleSheetClient.listValuesInTab(TabName.ACCOUNTS)).thenReturn(accounts)
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORIES)).thenReturn(budgetCategories)

        //AND: There are no automatic classify rules
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORY_RULES)).thenReturn(emptyBudgetCategoryRules)

        //WHEN: We ask to reconcile and categorize transactions
        //AND: We categorize A as Food
        //AND: We categorize B as Gas
        doReturn("y","f","y","g").whenever(commandLineService).readFromCommandLine()
        reconcileCommandLineRunner.run("reconcile")

        //THEN: We only save A and B and not C
        argumentCaptor<List<List<String>>>().apply {
            verify(googleSheetClient, times(2)).writeAllValuesToTab(eq(TabName.CATEGORIZED_TRANSACTIONS), capture())
            val first = firstValue.flatten()
            assertEquals(10, first.size)
            assertTrue(first.contains("H-E-B ONLINE"))

            val second = secondValue.flatten()
            assertEquals(10, second.size)
            assertTrue(second.contains("Chevron"))
        }
    }

    @Test
    @DisabledIfSystemProperty(named = "testEnvironment", matches = "ci")
    fun `System will use rules to categorize mint transactions that are not yet reconciled`() {
        //GIVEN: Mint website requires login and has three transactions to report
        stubMintLogin(wireMock)
        stubDownloadMintTransactionFileResponse(wireMock, "ReconcileAndCategorizeTransactionsTest")

        //AND: The Google sheets categorized transactions tab already has one transaction categorized
        whenever(googleSheetClient.listValuesInTab(TabName.CATEGORIZED_TRANSACTIONS)).thenReturn(categorizedTransactionsFromGoogleSheet)

        //AND: Accounts exist for these transactions as well as categories
        whenever(googleSheetClient.listValuesInTab(TabName.ACCOUNTS)).thenReturn(accounts)
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORIES)).thenReturn(budgetCategories)

        //AND: There are matching rules for each of the two remaining transactions
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORY_RULES)).thenReturn(twoMatchingBudgetCategoryRules)

        //WHEN: We ask to reconcile and categorize transactions
        reconcileCommandLineRunner.run("reconcile")

        //THEN: We only save A and B and not C
        argumentCaptor<List<List<String>>>().apply {
            verify(googleSheetClient, times(2)).writeAllValuesToTab(eq(TabName.CATEGORIZED_TRANSACTIONS), capture())
            val first = firstValue.flatten()
            assertEquals(10, first.size)
            assertTrue(first.contains("H-E-B ONLINE"))

            val second = secondValue.flatten()
            assertEquals(10, second.size)
            assertTrue(second.contains("Chevron"))
        }

        //AND: We never used the command line
        verifyZeroInteractions(commandLineService)
    }
}

private val thirdDate = LocalDate.of(2020, 7, 3)
private val googleSheetFormatter: DateTimeFormatter = DateTimeFormatterFactory("yyyy-MM-dd").createDateTimeFormatter()
private const val CATEGORIZED_AMOUNT = "128824"
private const val CHECKING_ACCOUNT_ID = "1"
private const val CREDIT_CARD_ACCOUNT_ID = "2"
private const val GLOBAL_EXTERNAL_ACCOUNT_ID = "3"
private const val INCOME_BUDGET_ID = "1"
private const val FOOD_BUDGET_ID = "2"
private const val GAS_BUDGET_ID = "3"

private val categorizedTransactionsFromGoogleSheet = CsvReader().readAll("""
        "Id","Date","Budget Category Id","Description","Bank Payee","Credit Account Id","Debit Account Id","Authorized Amount","Settled Amount","Is Reconcilable?"
        "${UUID.randomUUID()}","${thirdDate.format(googleSheetFormatter)}","$INCOME_BUDGET_ID","Employer PPD ID: 1224445555","Employer Payroll","$CHECKING_ACCOUNT_ID","$GLOBAL_EXTERNAL_ACCOUNT_ID","$CATEGORIZED_AMOUNT","$CATEGORIZED_AMOUNT","true"
        """.trimIndent()
).toMutableList()

private val accounts = CsvReader().readAll("""
    "Id","Name","Description","Mint Name","Mint Name Alt","Can Manually Credit","Can Manually Debit"
    "$CHECKING_ACCOUNT_ID","Checking Account","Checking Account","Checking Account","","true","true"
    "$CREDIT_CARD_ACCOUNT_ID","Credit Card(1234)","Credit Card(1234)","Credit Card(1234)","","true","true"
    "$GLOBAL_EXTERNAL_ACCOUNT_ID","$GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME","$GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME","$GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME","","true","true"
""".trimIndent())

private val budgetCategories = CsvReader().readAll("""
    "Id","Name","Description"
    "$INCOME_BUDGET_ID","Income","Income"
    "$FOOD_BUDGET_ID","Food","Food"
    "$GAS_BUDGET_ID","Gas","Gas"
""".trimIndent())

private val emptyBudgetCategoryRules = CsvReader().readAll("""
    "Description to Match","Account Id To Credit","Account Id To Debit","Budget Category Id"
""".trimIndent())

private val twoMatchingBudgetCategoryRules = CsvReader().readAll("""
    "Description to Match","Account Id To Credit","Account Id To Debit","Budget Category Id"
    "A","","$GLOBAL_EXTERNAL_ACCOUNT_ID","$FOOD_BUDGET_ID"
    "B","","$GLOBAL_EXTERNAL_ACCOUNT_ID","$GAS_BUDGET_ID"
    
""".trimIndent())