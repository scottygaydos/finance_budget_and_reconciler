package net.inherency.finances.domain.budget

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class BudgetRepository(private val googleSheetClient: GoogleSheetClient) {

    fun readAll(): List<BudgetData> {
        val rows = googleSheetClient.listValuesInTab(TabName.BUDGETS)
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

    fun writeNewBudgetEntries(entries: List<BudgetData>) {
        googleSheetClient.writeAllValuesToTab(TabName.BUDGETS, entriesToListOfListOfStrings(entries))
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