package net.inherency.finances.domain.budget.template

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class BudgetTemplateRepository(private val googleSheetClient: GoogleSheetClient) {

    fun readAll(): List<BudgetTemplateData> {
        val rows = googleSheetClient.listValuesInTab(TabName.BUDGET_TEMPLATE)
        return rows
                .subList(1, rows.size) //remove header
                .map { row ->
            BudgetTemplateData(
                    row[0].toInt(),
                    row[1].toInt(),
                    row[2]
            )
        }
    }

}