package net.inherency.finances.domain.budget

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class BudgetRepositoryTest {

    private lateinit var budgetRepository: BudgetRepository

    @Mock
    private lateinit var googleSheetClient: GoogleSheetClient

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetRepository = BudgetRepository(googleSheetClient)
    }

    @Test
    fun `Repository should successfully map rows, ignoring the first row because it is a header`() {
        val input = listOf(
                listOf("Budget Month", "Budget Year", "Budget Category Id", "Amount"),
                listOf("1", "2020", "2", "25000"),
                listOf("2", "2019", "4", "123")
        )

        whenever(googleSheetClient.listValuesInTab(TabName.BUDGETS)).thenReturn(input)

        val result = budgetRepository.readAll()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(BudgetData(1, 2020, 2, 25000), result[0])
        Assertions.assertEquals(BudgetData(2, 2019, 4, 123), result[1])
    }

    @Test
    fun `Repository should map data to string list for writing to google sheet`() {
        val input = listOf(
                BudgetData(1, 2020, 1, 123),
                BudgetData(1, 2020, 2, 456)
        )

        budgetRepository.writeNewBudgetEntries(input)

        val expectedResult = listOf(
                listOf("1", "2020", "1", "123"),
                listOf("1", "2020", "2", "456")
        )

        argumentCaptor<List<List<String>>>().apply {
            verify(googleSheetClient).writeAllValuesToTab(eq(TabName.BUDGETS), capture())
            assertEquals(expectedResult, firstValue)
        }
    }

}