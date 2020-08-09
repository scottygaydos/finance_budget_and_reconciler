package net.inherency.finances.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.config.ConfigKey
import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.external.google.GoogleSheetClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.assertEquals

@SpringBootTest //This is necessary to auto-inject all classes and properties from test application.yml
@AutoConfigureMockMvc //This is necessary to inject MockMvc
class UpdateDataFromMintControllerTest {

    private val log = LoggerFactory.getLogger(UpdateDataFromMintControllerTest::class.java)

    @Suppress("unused")
    @MockBean
    private lateinit var googleSheetClient: GoogleSheetClient

    @SpyBean
    private lateinit var configurationService: ConfigurationService

    private lateinit var wireMock: WireMockServer

    @Autowired
    private lateinit var mockMvc: MockMvc

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
    }

    @Test
    @DisabledIfSystemProperty(named = "testEnvironment", matches = "ci")
    fun `UpdateDataFromMintController successfully logs into mint website and writes data to google sheet`() {
        //GIVEN: Mint website requires login and has three transactions to report
        stubMintLogin(wireMock)
        stubDownloadMintTransactionFileResponse(wireMock, "UpdateDataFromMintControllerTest")

        //AND: Google sheets integration works, allowing us to write the three transactions to a sheet
        //This is accomplished by the MockBean annotation on googleSheetClient

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