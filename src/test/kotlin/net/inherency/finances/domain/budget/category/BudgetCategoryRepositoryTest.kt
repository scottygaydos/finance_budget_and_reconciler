package net.inherency.finances.domain.budget.category

import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BudgetCategoryRepositoryTest {

    private lateinit var budgetCategoryRepository: BudgetCategoryRepository

    @Mock
    private lateinit var googleSheetClient: GoogleSheetClient

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetCategoryRepository = BudgetCategoryRepository(googleSheetClient)
    }

    @Test
    fun `Repository should successfully map rows, ignoring the first row because it is a header`() {
        val input = listOf(
                listOf("Id", "Name", "Description", "Destination Id When Moving Budget Forward To Next Month"),
                listOf("1", "Food", "Food Budget", "1"),
                listOf("16", "Non Budget", "A transaction that does not impact the budget", "16")
        )

        whenever(googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORIES)).thenReturn(input)

        val result = budgetCategoryRepository.readAll()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(BudgetCategoryData(1, "Food", "Food Budget", 1), result[0])
        Assertions.assertEquals(BudgetCategoryData(16, "Non Budget", "A transaction that does not impact the budget", 1), result[1])

    }

}