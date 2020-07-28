package net.inherency.finances.domain.budget.category

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class BudgetCategoryRepository(private val googleSheetClient: GoogleSheetClient) {

    fun readAll(): List<BudgetCategoryData> {
        val rows = googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORIES)
        return rows
                .subList(1, rows.size) //remove header
                .map { row ->
            BudgetCategoryData(
                    row[0].toInt(),
                    row[1],
                    row[2])
        }
    }
}