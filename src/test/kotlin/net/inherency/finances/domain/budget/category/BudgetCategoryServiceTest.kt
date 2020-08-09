package net.inherency.finances.domain.budget.category

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BudgetCategoryServiceTest {

    private lateinit var budgetCategoryService: BudgetCategoryService

    @Mock
    private lateinit var budgetCategoryRepository: BudgetCategoryRepository

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetCategoryService = BudgetCategoryService(budgetCategoryRepository)
    }

    @Test
    fun `A good set of category data reports successfully`() {
        val foodCategory = BudgetCategoryData(1, "Food", "Food Budget")
        val gasCategory = BudgetCategoryData(2, "Gas", "Gas Budget")
        val input = listOf(foodCategory, gasCategory)

        whenever(budgetCategoryRepository.readAll()).thenReturn(input)

        val result = budgetCategoryService.readAll()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(foodCategory, result[0])
        Assertions.assertEquals(gasCategory, result[1])
    }

    @Test
    fun `The application throws an exception if no budget category data is found`() {
        val input = emptyList<BudgetCategoryData>()

        whenever(budgetCategoryRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetCategoryService.readAll()
        }
    }

    @Test
    fun `The application throws an exception if the data has more than one entry with the same id`() {
        val foodCategory = BudgetCategoryData(1, "Food", "Food Budget")
        val gasCategory = BudgetCategoryData(1, "Gas", "Gas Budget")
        val input = listOf(foodCategory, gasCategory)

        whenever(budgetCategoryRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetCategoryService.readAll()
        }
    }

    @Test
    fun `The application throws an exception if the data has more than one entry with the same name`() {
        val foodCategory = BudgetCategoryData(1, "Food", "Food Budget")
        val gasCategory = BudgetCategoryData(2, "Food", "Gas Budget")
        val input = listOf(foodCategory, gasCategory)

        whenever(budgetCategoryRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetCategoryService.readAll()
        }
    }
}