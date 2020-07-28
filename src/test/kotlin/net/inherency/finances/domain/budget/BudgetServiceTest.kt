package net.inherency.finances.domain.budget

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.domain.budget.template.BudgetTemplateData
import net.inherency.finances.domain.budget.template.BudgetTemplateService
import org.junit.jupiter.api.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.Month
import java.util.*

class BudgetServiceTest {

    private lateinit var budgetService: BudgetService

    @Mock
    private lateinit var budgetTemplateService: BudgetTemplateService

    @Mock
    private lateinit var budgetRepository: BudgetRepository

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        budgetService = BudgetService(budgetTemplateService, budgetRepository)
    }

    @Test
    fun `Successfully create a new budget from template when no other budget records exist`() {
        //GIVEN: A valid request year/month
        val input = CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JULY)

        //AND: We have no existing budget records
        whenever(budgetRepository.readAll()).thenReturn(emptyList())

        //AND: We have a valid set of budget template data
        val budgetTemplateValues = listOf(
                BudgetTemplateData(1, 10, "a"),
                BudgetTemplateData(2, 20, "b")
        )
        whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

        //WHEN: We try to create a new set of budget records with the input
        budgetService.createBudgetForMonthAndYearFromTemplate(input)

        //THEN: We successfully ask the repository to create the new records
        val expectedEntries = listOf(
                BudgetData(Month.JULY.value, 2020, 1, 10),
                BudgetData(Month.JULY.value, 2020, 2, 20)
        )
        verify(budgetRepository).writeNewBudgetEntries(expectedEntries)
    }

    @Test
    fun `Successfully create a new budget from template when other budget records exist`() {
        //GIVEN: A valid request year/month
        val input = CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JULY)

        //AND: We have existing valid budget records
        whenever(budgetRepository.readAll()).thenReturn(listOf(
                BudgetData(Month.JUNE.value, 2020, 1, 11),
                BudgetData(Month.JUNE.value, 2020, 2, 22),
                BudgetData(Month.JUNE.value, 2020, 3, 33)
        ))

        //AND: We have a valid set of budget template data
        val budgetTemplateValues = listOf(
                BudgetTemplateData(1, 10, "a"),
                BudgetTemplateData(2, 20, "b")
        )
        whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

        //WHEN: We try to create a new set of budget records with the input
        budgetService.createBudgetForMonthAndYearFromTemplate(input)

        //THEN: We successfully ask the repository to create the new records
        val expectedEntries = listOf(
                BudgetData(Month.JULY.value, 2020, 1, 10),
                BudgetData(Month.JULY.value, 2020, 2, 20)
        )
        verify(budgetRepository).writeNewBudgetEntries(expectedEntries)
    }

    @TestFactory
    fun testInvalidInputs() =
            listOf(
                    Pair(2020, 13),
                    Pair(2020, -1),
                    Pair(-2020, Calendar.JULY))
                    .map { (year, month) ->
                        DynamicTest.dynamicTest("Fail to create a new budget from template due to invalid input $year and $month") {
                            //GIVEN: An invalid request due to month
                            val input = CreateBudgetForMonthAndYearFromTemplateCmd(year, month)

                            //AND: We have no existing budget records
                            whenever(budgetRepository.readAll()).thenReturn(emptyList())

                            //AND: We have a valid set of budget template data
                            val budgetTemplateValues = listOf(
                                    BudgetTemplateData(1, 10, "a"),
                                    BudgetTemplateData(2, 20, "b")
                            )
                            whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

                            //WHEN: We try to create a new set of budget records with the input
                            //THEN: We get an exception
                            Assertions.assertThrows(IllegalArgumentException::class.java) {
                                budgetService.createBudgetForMonthAndYearFromTemplate(input)
                            }

                            //AND: We do not attempt to write any new entries
                            verify(budgetRepository, times(0)).writeNewBudgetEntries(any())
                        }
                    }

    @Test
    fun `Fail to create a new budget from template when existing budget values have errors`() {
        //GIVEN: A valid request year/month
        val input = CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JULY)

        //AND: We have existing invalid budget records (duplicate categories)
        whenever(budgetRepository.readAll()).thenReturn(listOf(
                BudgetData(Month.JUNE.value, 2020, 1, 11),
                BudgetData(Month.JUNE.value, 2020, 1, 22),
                BudgetData(Month.JUNE.value, 2020, 3, 33)
        ))

        //AND: We have a valid set of budget template data
        val budgetTemplateValues = listOf(
                BudgetTemplateData(1, 10, "a"),
                BudgetTemplateData(2, 20, "b")
        )
        whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

        //WHEN: We try to create a new set of budget records with the input
        //THEN: We get an exception
        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetService.createBudgetForMonthAndYearFromTemplate(input)
        }

        //AND: We do not attempt to write any new entries
        verify(budgetRepository, times(0)).writeNewBudgetEntries(any())
    }

    @Test
    fun `Fail to create a new budget from template when template values have errors`() {
        //GIVEN: A valid request year/month
        val input = CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JULY)

        //AND: We have existing valid budget records
        whenever(budgetRepository.readAll()).thenReturn(listOf(
                BudgetData(Month.JUNE.value, 2020, 1, 11),
                BudgetData(Month.JUNE.value, 2020, 2, 22),
                BudgetData(Month.JUNE.value, 2020, 3, 33)
        ))

        //AND: We have an invalid set of budget template data (duplicate categories)
        val budgetTemplateValues = listOf(
                BudgetTemplateData(1, 10, "a"),
                BudgetTemplateData(1, 20, "b")
        )
        whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

        //WHEN: We try to create a new set of budget records with the input
        //THEN: We get an exception
        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetService.createBudgetForMonthAndYearFromTemplate(input)
        }

        //AND: We do not attempt to write any new entries
        verify(budgetRepository, times(0)).writeNewBudgetEntries(any())
    }

    @Test
    fun `Fail to create a new budget from template when new values would duplicate existing budget values`() {
        //GIVEN: A valid request year/month
        val input = CreateBudgetForMonthAndYearFromTemplateCmd(2020, Calendar.JUNE)

        //AND: We have existing valid budget records
        whenever(budgetRepository.readAll()).thenReturn(listOf(
                BudgetData(Month.JUNE.value, 2020, 1, 11),
                BudgetData(Month.JUNE.value, 2020, 2, 22),
                BudgetData(Month.JUNE.value, 2020, 3, 33)
        ))

        //AND: We have a valid set of budget template data (duplicate categories)
        val budgetTemplateValues = listOf(
                BudgetTemplateData(1, 10, "a"),
                BudgetTemplateData(2, 20, "b")
        )
        whenever(budgetTemplateService.readAllBudgetTemplateValues()).thenReturn(budgetTemplateValues)

        //WHEN: We try to create a new set of budget records with the input
        //THEN: We get an exception
        Assertions.assertThrows(IllegalStateException::class.java) {
            budgetService.createBudgetForMonthAndYearFromTemplate(input)
        }

        //AND: We do not attempt to write any new entries
        verify(budgetRepository, times(0)).writeNewBudgetEntries(any())
    }
}