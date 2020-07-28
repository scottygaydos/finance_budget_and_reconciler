package net.inherency.finances.domain.budget

import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.domain.budget.template.BudgetTemplateData
import net.inherency.finances.domain.budget.template.BudgetTemplateService
import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service
import java.time.Month
import java.util.Calendar

@Service
class BudgetService(
        private val budgetTemplateService: BudgetTemplateService,
        private val budgetRepository: BudgetRepository) {

    fun createBudgetForMonthAndYearFromTemplate(cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        val (year, month) = validateAndTransformYearAndMonthParameters(cmd)
        val existingBudgetEntries = budgetRepository.readAll()
        validateBudgetEntries(existingBudgetEntries, false)
        val allBudgetTemplateValues = budgetTemplateService.readAllBudgetTemplateValues()
        val newBudgetRecords = generateNewBudgetRecords(allBudgetTemplateValues, month, year)
        validateBudgetEntries(existingBudgetEntries.plus(newBudgetRecords), true)
        budgetRepository.writeNewBudgetEntries(newBudgetRecords)
    }

    private fun generateNewBudgetRecords(allBudgetTemplateValues: List<BudgetTemplateData>, month: Month, year: Int)
            : List<BudgetData> {
        return allBudgetTemplateValues.map { budgetTemplateData ->
            BudgetData(
                    month.value,
                    year,
                    budgetTemplateData.budgetCategoryId,
                    budgetTemplateData.amount
            )
        }
    }

    private fun validateBudgetEntries(entries: List<BudgetData>, includesNewRecords: Boolean) {
        if (entries.isNotEmpty()) {
            if  (!objectIsUnique(entries) { entry ->
                        Triple(entry.budgetCategoryId, entry.year, entry.month) })  {
                if (includesNewRecords) {
                    throw IllegalStateException("Invalid input -- input would duplicate existing budget information")
                } else {
                    throw IllegalStateException("Problem with existing budget entries -- Each budget entry must have a unique combination of category, year, and month")
                }

            }
        }
    }

    private fun validateAndTransformYearAndMonthParameters(cmd: CreateBudgetForMonthAndYearFromTemplateCmd)
            : Pair<Int, Month> {
        if (cmd.year < 0) {
            throw IllegalArgumentException("Year must be greater than 0")
        }

        if (cmd.month < Calendar.JANUARY) {
            throw IllegalArgumentException("Month must be 0 (January) or greater and is: ${cmd.month}")
        }
        if (cmd.month > Calendar.DECEMBER) {
            throw IllegalArgumentException("Month must be 11 (December) or less and is: ${cmd.month}")
        }

        return Pair(cmd.year, Month.of(cmd.month+1))
    }

}