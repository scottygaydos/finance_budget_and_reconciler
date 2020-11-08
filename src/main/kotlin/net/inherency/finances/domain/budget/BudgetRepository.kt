package net.inherency.finances.domain.budget

import net.inherency.finances.controller.dto.FixBudgetToMatchDepositsCmd
import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class BudgetRepository(private val googleSheetClient: GoogleSheetClient) {

    private val log = LoggerFactory.getLogger(BudgetRepository::class.java)

    fun readAll(): List<BudgetData> {
        return transformAllDataRowsToBudgetData(readAllRows())
    }

    fun writeNewBudgetEntries(entries: List<BudgetData>) {
        googleSheetClient.writeAllValuesToTab(TabName.BUDGETS, entriesToListOfListOfStrings(entries))
    }

    fun updateBudgetEntryForYearAndMonth(cmd: FixBudgetToMatchDepositsCmd, newAmount: Int): BudgetData {
        val allRows = readAllRows()
        val headerRow = allRows.first()
        val allExisting = transformAllDataRowsToBudgetData(allRows)
        val existingEntry = readAll().first {
            it.month == cmd.budgetMonth &&
                    it.year == cmd.budgetYear &&
                    it.budgetCategoryId == cmd.transactionTypeId
        }
        val updatedEntry =
                existingEntry.let { BudgetData(it.month, it.year, it.budgetCategoryId, newAmount) }

        val newList = allExisting.asSequence().minus(existingEntry).plus(updatedEntry)
                .sortedWith(compareBy(
                        {it.year},
                        {it.month},
                        {it.budgetCategoryId}
                ))
                .toList()

        googleSheetClient.clearAllDataInTab(TabName.BUDGETS)
        googleSheetClient.writeAllValuesToTab(TabName.BUDGETS, listOf(headerRow))
        writeNewBudgetEntries(newList)
        while (readAll() != newList) {
            log.info("Waiting for new budget data to update")
            Thread.sleep(1000)
        }
        return updatedEntry
    }

    private fun readAllRows(): List<List<String>> {
        return googleSheetClient.listValuesInTab(TabName.BUDGETS)
    }

    private fun transformAllDataRowsToBudgetData(rows: List<List<String>>): List<BudgetData> {
        return rows
                .subList(1, rows.size) //remove header
                .map { row ->
                    BudgetData(
                            row[0].toInt(),
                            row[1].toInt(),
                            row[2].toInt(),
                            row[3].toInt()
                    )
                }
    }

    private fun entriesToListOfListOfStrings(entries: List<BudgetData>): List<List<String>> {
        return entries.map { entry ->
            listOf(
                    entry.month.toString(),
                    entry.year.toString(),
                    entry.budgetCategoryId.toString(),
                    entry.amount.toString()
            )
        }
    }
}