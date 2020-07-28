package net.inherency.finances.domain.budget.template

import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BudgetTemplateRepositoryTest {

    private lateinit var budgetTemplateRepository: BudgetTemplateRepository

    @Mock
    private lateinit var googleSheetClient: GoogleSheetClient

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetTemplateRepository = BudgetTemplateRepository(googleSheetClient)
    }

    @Test
    fun `Repository should successfully map rows, ignoring the first row because it is a header`() {
        val input = listOf(
                listOf("Budget Category Id", "Amount", "Ordering"),
                listOf("1", "12312", "a"),
                listOf("2", "4445", "b")
        )

        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_TEMPLATE)).thenReturn(input)

        val result = budgetTemplateRepository.readAll()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(BudgetTemplateData(1, 12312, "a"), result[0])
        Assertions.assertEquals(BudgetTemplateData(2, 4445, "b"), result[1])
    }

}