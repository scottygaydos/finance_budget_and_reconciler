package net.inherency.finances.domain.budget.template

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BudgetTemplateServiceTest {

    private lateinit var budgetTemplateService: BudgetTemplateService

    @Mock
    private lateinit var budgetTemplateRepository: BudgetTemplateRepository

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetTemplateService = BudgetTemplateService(budgetTemplateRepository)
    }

    @Test
    fun `A good set of template data reports successfully`() {
        val templateValueOne = BudgetTemplateData(1, 123, "a")
        val templateValueTwo = BudgetTemplateData(2, 345, "b")
        val input = listOf(templateValueOne, templateValueTwo)

        whenever(budgetTemplateRepository.readAll()).thenReturn(input)

        val result = budgetTemplateService.readAllBudgetTemplateValues()

        Assertions.assertEquals(2, result.size)
        Assertions.assertEquals(templateValueOne, result[0])
        Assertions.assertEquals(templateValueTwo, result[1])
    }

    @Test
    fun `The application throws an exception if no budget template data is found`() {
        val input = emptyList<BudgetTemplateData>()

        whenever(budgetTemplateRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetTemplateService.readAllBudgetTemplateValues()
        }
    }

    @Test
    fun `The application throws an exception if the data has more than one entry with the same category id`() {
        val templateValueOne = BudgetTemplateData(1, 123, "a")
        val templateValueTwo = BudgetTemplateData(1, 345, "b")
        val input = listOf(templateValueOne, templateValueTwo)

        whenever(budgetTemplateRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetTemplateService.readAllBudgetTemplateValues()
        }
    }

    @Test
    fun `The application throws an exception if the data has a negative value`() {
        val templateValueOne = BudgetTemplateData(1, 123, "a")
        val templateValueTwo = BudgetTemplateData(2, -345, "b")
        val input = listOf(templateValueOne, templateValueTwo)

        whenever(budgetTemplateRepository.readAll()).thenReturn(input)

        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetTemplateService.readAllBudgetTemplateValues()
        }
    }
}