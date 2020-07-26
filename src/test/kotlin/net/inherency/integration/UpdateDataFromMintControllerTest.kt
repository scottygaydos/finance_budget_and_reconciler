package net.inherency.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.config.ConfigKey
import net.inherency.config.ConfigurationService
import net.inherency.external.google.GoogleSheetClient
import net.inherency.external.google.TabName
import net.inherency.vo.CreditOrDebit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariables
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertEquals



@SpringBootTest //This is necessary to auto-inject all classes and properties from test application.yml
@AutoConfigureMockMvc //This is necessary to inject MockMvc
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
class UpdateDataFromMintControllerTest {

    private val log = LoggerFactory.getLogger(UpdateDataFromMintControllerTest::class.java)

    @Suppress("SpringJavaInjectionPointsAutowiringInspection", "ThisIsAddedToAutowireInWireMockContextInitializer")
    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @MockBean
    private lateinit var googleSheetClient: GoogleSheetClient

    @SpyBean
    private lateinit var configurationService: ConfigurationService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @BeforeEach
    fun beforeAll() {
        //This allows us to continue setting non-sensitive variables in application.yml, but programmatically set
        //sensitive environment variables so we don't have to document/teach testers how to configure their environment
        //to run this test.
        doReturn("12345@!@$%").whenever(configurationService).getString(ConfigKey.GOOGLE_SHEET_ID)
        doReturn("finances").whenever(configurationService).getString(ConfigKey.GOOGLE_APP_NAME)
        doReturn("{key:val}").whenever(configurationService).getString(ConfigKey.GOOGLE_AUTH_JSON)
        doReturn("fake@fake.com").whenever(configurationService).getString(ConfigKey.MINT_LOGIN_EMAIL)
        doReturn("very_secure_password").whenever(configurationService).getString(ConfigKey.MINT_LOGIN_PASSWORD)
    }

    @Test
    @DisabledIfEnvironmentVariables(
            DisabledIfEnvironmentVariable(named = "test-environment", matches = "ci"),
            DisabledIfEnvironmentVariable(named = "test-environment", matches = "production")
    )
    fun `UpdateDataFromMintController successfully logs into mint website and writes data to google sheet`() {
        //GIVEN: Mint website has three transactions to report
        //See stubbing method in WireMockContextInitializer

        //AND: Google sheets integration works, allowing us to write the three transactions to a sheet
        //This is accomplished by the MockBean annotation on googleSheetClient

        //AND: Google sheets responds with those three transactions when we ask to list transactions
        whenever(googleSheetClient.listValuesInTab(TabName.TRANSACTIONS)).thenReturn(testDataAsListOfListOfStrings)


        //WHEN: We try to download mint transactions and update our sheet
        val responseString = mockMvc.perform(post("/ws/updateData/update")).andReturn().response.contentAsString
        val response = jacksonObjectMapper().readTree(responseString)


        //THEN: We get three transactions
        log.info("Response: ${response.size()}")
        response.forEach { println(it) }
        assertEquals(3, response.size())

        val firstResponse = response[0]
        assertEquals("2020-07-17", firstResponse["date"].asText())
        assertEquals("H-E-B ONLINE", firstResponse["description"].asText())
        assertEquals("H-E-B ONLINE", firstResponse["originalDescription"].asText())
        assertEquals("12825", firstResponse["amount"].asText())
        assertEquals(CreditOrDebit.DEBIT.toString(), firstResponse["creditOrDebit"].asText())
        assertEquals("Groceries", firstResponse["category"].asText())
        assertEquals("Checking Account", firstResponse["accountName"].asText())

        val secondResponse = response[1]
        assertEquals("2020-07-13", secondResponse["date"].asText())
        assertEquals("Apple", secondResponse["description"].asText())
        assertEquals("APPLE.COM/BILL", secondResponse["originalDescription"].asText())
        assertEquals("99", secondResponse["amount"].asText())
        assertEquals(CreditOrDebit.DEBIT.toString(), secondResponse["creditOrDebit"].asText())
        assertEquals("Electronics & Software", secondResponse["category"].asText())
        assertEquals("Credit Card(1234)", secondResponse["accountName"].asText())

        val thirdResponse = response[2]
        assertEquals("2020-07-03", thirdResponse["date"].asText())
        assertEquals("Employer PPD ID: 1224445555", thirdResponse["description"].asText())
        assertEquals("Employer Payroll", thirdResponse["originalDescription"].asText())
        assertEquals("128824", thirdResponse["amount"].asText())
        assertEquals(CreditOrDebit.CREDIT.toString(), thirdResponse["creditOrDebit"].asText())
        assertEquals("Paycheck", thirdResponse["category"].asText())
        assertEquals("Checking Account", thirdResponse["accountName"].asText())
    }


}