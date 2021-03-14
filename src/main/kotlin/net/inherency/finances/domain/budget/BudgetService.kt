package net.inherency.finances.domain.budget

import net.inherency.finances.controller.dto.*
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.budget.template.BudgetTemplateData
import net.inherency.finances.domain.budget.template.BudgetTemplateService
import net.inherency.finances.domain.transaction.CategorizedTransaction
import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.objectIsUnique
import net.inherency.finances.util.DateTimeService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.util.*
import kotlin.math.min

@Service
class BudgetService(
        private val budgetTemplateService: BudgetTemplateService,
        private val budgetRepository: BudgetRepository,
        private val transactionService: TransactionService,
        private val budgetCategoryService: BudgetCategoryService,
        private val dateTimeService: DateTimeService) {

    fun createBudgetForMonthAndYearFromTemplate(cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        val (year, month) = validateAndTransformYearAndMonthParameters(cmd)
        val existingBudgetEntries = budgetRepository.readAll()
        validateBudgetEntries(existingBudgetEntries, false)
        val allBudgetTemplateValues = budgetTemplateService.readAllBudgetTemplateValues()
        val newBudgetRecords = generateNewBudgetRecords(allBudgetTemplateValues, month, year)
        validateBudgetEntries(existingBudgetEntries.plus(newBudgetRecords), true)
        budgetRepository.writeNewBudgetEntries(newBudgetRecords)
    }

    fun fixMonthToMatchDeposits(cmd: FixBudgetToMatchDepositsCmd) {
        validateYearAndMonthParameters(cmd.budgetYear, cmd.budgetMonth)
        val budgetReportDTO = generateMonthlyBudgetReport(cmd.budgetMonth, cmd.budgetYear)
        check(budgetReportDTO.discrepancyBetweenTotalBudgetAndPaychecks > 0)
            { "Paycheck total is not greater than the amount budgeted for the month." }
        val existingBudgetAmount = budgetReportDTO.transactionTypeReports[cmd.transactionTypeId]?.budgetAmount
        requireNotNull(existingBudgetAmount)
        val newBudgetAmount = existingBudgetAmount + budgetReportDTO.discrepancyBetweenTotalBudgetAndPaychecks
        budgetRepository.updateBudgetEntryForYearAndMonth(cmd, newBudgetAmount)
    }

    fun moveRemainderToNextMonth(cmd: MoveRemainderToNextMonthCmd) {
        validateYearAndMonthParameters(cmd.budgetYear, cmd.budgetMonth)
        val budgetReportDTO = generateMonthlyBudgetReport(cmd.budgetMonth, cmd.budgetYear)
        check(budgetReportDTO.totalRemainingBudget > 0)
            {"Cannot move negative or zero budget amount forward"}

        var remainingBudget = budgetReportDTO.totalRemainingBudget
        budgetCategoryService.readAll().sortedByDescending { it.moveBudgetForwardDestinationCategoryId }.forEach { budgetCategory ->
            val amountToMove: Int = min(
                budgetReportDTO.transactionTypeReports[budgetCategory.id]?.remainingAmount ?: 0,
                remainingBudget)
            if (amountToMove > 0 && remainingBudget > 0) {
                val existingYearMonth: LocalDate = dateTimeService.fromYearAndMonth(cmd.budgetYear, cmd.budgetMonth)
                val nextYearMonth = existingYearMonth.plusMonths(1)
                val debitCheckingForOldMonthCmd = CategorizedTransaction(
                    UUID.randomUUID(),
                    existingYearMonth,
                    budgetCategory.id,
                    "Move Budget To Next Month",
                    "Move Budget To Next Month",
                    2, //ToDo: Add feature to look this up; this is 'global external debit account'
                    1, //ToDo: Use ^ feature; this is 'scotty checking'
                    amountToMove,
                    amountToMove,
                    false
                )
                val creditCheckingForNextMonthCmd = CategorizedTransaction(
                    UUID.randomUUID(),
                    nextYearMonth,
                    budgetCategory.moveBudgetForwardDestinationCategoryId,
                    "Move Budget From Previous Month",
                    "Move Budget From Previous Month",
                    1, //ToDo: Use v feature; this is 'scotty checking'
                    2, //ToDo: Add feature to look this up; this is 'global external debit account'
                    amountToMove,
                    amountToMove,
                    false
                )
                transactionService.create(debitCheckingForOldMonthCmd)
                transactionService.create(creditCheckingForNextMonthCmd)
                remainingBudget -= amountToMove
            }
        }
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
        val allTransactions = transactionService.listAllCategorizedTransactions()
        val txsByBudgetCategoryId = allTransactions
                .filter { YearMonth.from(it.date) == YearMonth.of(budgetYearLocal, budgetMonthLocal) }
                .filterNot { it.creditAccountId == 100200 && it.debitAccountId == 2} //TODO: Handle this with configuration somehow
                .filterNot { it.debitAccountId == 100200 && it.creditAccountId == 2} //TODO: Handle this with configuration somehow
                .groupBy { it.budgetCategoryId }
                .mapValues { mapEntry -> mapEntry.value.map { calculateAmount(it) }.sum() }
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

        val paychecksPreviousMonth: List<PaycheckDTO> = allTransactions
                .filter { YearMonth.from(it.date) == YearMonth.of(budgetYearLocal, budgetMonthLocal).minusMonths(1) }
                .filter { it.creditAccountId == 1 } //TODO: Fix this to look up checking acct
                .filter { it.bankPayee.toLowerCase().contains("onprem") || it.description.toLowerCase().contains("onprem") }
                .map { PaycheckDTO(it.date, it.settledAmount) }

        val totalBudget = budgetCategoryReports.map { it.budgetAmount }.sum()
        val totalPaychecksPreviousMonth = paychecksPreviousMonth.map { it.amount }.sum()

        return BudgetReportDTO(
                transactionTypeReports = budgetCategoryReports.associateBy { it.transactionTypeId },
                totalBudget = totalBudget,
                totalRemainingBudget = budgetCategoryReports.map { it.remainingAmount }.sum(),
                paychecksPreviousMonthArray = paychecksPreviousMonth,
                totalPaychecksPreviousMonth = totalPaychecksPreviousMonth,
                discrepancyBetweenTotalBudgetAndPaychecks = totalPaychecksPreviousMonth - totalBudget
        )

    }

    //TODO: Add property to account for this
    private fun calculateAmount(it: CategorizedTransaction): Int {
        val creditAccountsToReverse = listOf(1, 3, 4, 25, 28, 29, 30, 100000, 100100, 100200)
        val amount =  if (isSharedAccountEntry(it)) it.settledAmount / 2 else it.settledAmount
        return if (creditAccountsToReverse.contains(it.creditAccountId)) -amount else amount
    }


    //TODO: Add property to account for this
    private fun isSharedAccountEntry(entry: CategorizedTransaction): Boolean {
        if (entry.debitAccountId == 100000) {
            return true
        } else if (entry.creditAccountId == 100000) {
            return true
        }
        return false
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