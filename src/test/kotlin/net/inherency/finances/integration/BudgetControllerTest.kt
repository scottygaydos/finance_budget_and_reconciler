package net.inherency.finances.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.config.ConfigKey
import net.inherency.finances.config.ConfigurationService
import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@SpringBootTest //This is necessary to auto-inject all classes and properties from test application.yml
@AutoConfigureMockMvc //This is necessary to inject MockMvc
class BudgetControllerTest {

    @MockBean
    private lateinit var googleSheetClient: GoogleSheetClient

    @SpyBean
    private lateinit var configurationService: ConfigurationService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    @DisabledIfSystemProperty(named = "testEnvironment", matches = "ci")
    fun `Creating a budget for month and year from template successfully copies budget template data to the target month and year`() {
        //GIVEN: Google sheets integration works, allowing us to read and write the sheets
        //This is accomplished by the MockBean annotation on googleSheetClient

        //AND: Google sheets responds with a list of good budget template values when asked
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_TEMPLATE)).thenReturn(goodBudgetTemplateData)

        //AND: Google sheets responds with a good list of existing budget values for June, 2020 when asked
        whenever(googleSheetClient.listValuesInTab(TabName.BUDGETS)).thenReturn(goodExistingBudgetDataForJune2020)

        //WHEN: We try to create a new budget for July, 2020
        val jsonBody = objectMapper.writeValueAsString(CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JULY))
        mockMvc.perform(MockMvcRequestBuilders
                .post("/ws/budget/create_for_month" )
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk)

        //THEN: Google sheets is told to store all the good budget template values for July, 2020
        verify(googleSheetClient).writeAllValuesToTab(TabName.BUDGETS, goodBudgetDataToBeWrittenForJuly2020)
    }
}

val goodBudgetTemplateData = listOf(
        listOf("Budget Category Id", "Amount", "Ordering"),
        listOf("1", "10000", "a"),
        listOf("2", "20000", "b"),
        listOf("3", "40000", "d"),
        listOf("4", "30000", "c")
)

val goodExistingBudgetDataForJune2020 = listOf(
        listOf("Budget Month", "Budget Year", "Budget Category Id", "Amount"),
        listOf("6", "2020", "1", "10000"),
        listOf("6", "2020", "2", "20000"),
        listOf("6", "2020", "3", "40000"),
        listOf("6", "2020", "4", "30000")
)

val goodBudgetDataToBeWrittenForJuly2020 = listOf(
        listOf("7", "2020", "1", "10000"),
        listOf("7", "2020", "2", "20000"),
        listOf("7", "2020", "3", "40000"),
        listOf("7", "2020", "4", "30000")
)