package net.inherency.finances.domain.budget

import net.inherency.finances.controller.dto.BudgetReportDTO
import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.controller.dto.TransactionTypeReportDTO
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.budget.template.BudgetTemplateData
import net.inherency.finances.domain.budget.template.BudgetTemplateService
import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.util.*

@Service
class BudgetService(
        private val budgetTemplateService: BudgetTemplateService,
        private val budgetRepository: BudgetRepository,
        private val transactionService: TransactionService,
        private val budgetCategoryService: BudgetCategoryService) {

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

        return Pair(cmd.year, Month.of(cmd.month + 1))
    }

    fun generateMonthlyBudgetReport(budgetMonth: Int?, budgetYear: Int?): BudgetReportDTO {
        val budgetMonthLocal = budgetMonth ?: LocalDate.now().monthValue
        val budgetYearLocal = budgetYear ?: LocalDate.now().year
        val allBudgetCategories = budgetCategoryService.readAll()
        val budgetTemplates = budgetTemplateService.readAllBudgetTemplateValues()
        val txsByBudgetCategoryId = transactionService.listAllCategorizedTransactions()
                .filter { YearMonth.from(it.date) == YearMonth.of(budgetYearLocal, budgetMonthLocal) }
                .groupBy { it.budgetCategoryId }
                .mapValues { mapEntry -> mapEntry.value.map { it.settledAmount }.sum() }
        val budgetCategoryReports = budgetRepository.readAll()
                .filter { it.month == budgetMonthLocal && it.year == budgetYearLocal }
                .map { budget ->
                    val category = allBudgetCategories.first{ budget.budgetCategoryId == it.id }
                    TransactionTypeReportDTO(
                            transactionTypeId = budget.budgetCategoryId,
                            transactionTypeName = category.name,
                            budgetAmount = budget.amount,
                            remainingAmount = budget.amount - (txsByBudgetCategoryId[budget.budgetCategoryId] ?: 0),
                            sortOrderValue = budgetTemplates.first { it.budgetCategoryId == category.id }.ordering)
                }

        return BudgetReportDTO(
                transactionTypeReports = budgetCategoryReports.associateBy { it.transactionTypeId },
                totalBudget = budgetCategoryReports.map { it.budgetAmount }.sum(),
                totalRemainingBudget = budgetCategoryReports.map { it.remainingAmount }.sum(),
                paychecksPreviousMonthArray = emptyList(), //TODO: Fixme
                totalPaychecksPreviousMonth = 0, //TODO: Fixme
                discrepancyBetweenTotalBudgetAndPaychecks = 0 //TODO: Fixme
        )

    }

    private fun validateYearAndMonthParameters(year: Int, month: Int) {
        if (year < 0) {
            throw IllegalArgumentException()
        }
        val zeroIndexBasedMonth = month - 1
        if (zeroIndexBasedMonth < Calendar.JANUARY) {
            throw IllegalArgumentException()
        }
        if (zeroIndexBasedMonth > Calendar.DECEMBER) {
            throw IllegalArgumentException()
        }
    }

}