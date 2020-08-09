package net.inherency.finances.domain.reconcile.rule

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class BudgetCategoryRuleRepository(private val googleSheetClient: GoogleSheetClient) {

    private lateinit var rules: List<BudgetCategoryRuleData>
    private var cached = false

    @Synchronized
    fun readAll(ignoreCache: Boolean = false): List<BudgetCategoryRuleData> {
        return if (cached && !ignoreCache) {
            this.rules
        } else {
            val rows = googleSheetClient.listValuesInTab(TabName.BUDGET_CATEGORY_RULES)
            if (rows.isEmpty()) {
                return emptyList()
            }
            val foundRules = rows
                    .subList(1, rows.size) //remove header
                    .map { row ->
                        BudgetCategoryRuleData(
                                row[0],
                                row[1].toIntOrNull(),
                                row[2].toIntOrNull(),
                                row[3].toInt()
                        )
                    }
            this.rules = foundRules
            rules
        }
    }
}