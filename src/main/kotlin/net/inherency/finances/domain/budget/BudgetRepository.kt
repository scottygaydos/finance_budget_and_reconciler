package net.inherency.finances.domain.budget

import net.inherency.finances.controller.dto.FixBudgetToMatchDepositsCmd

interface BudgetRepository {
    fun readAll(): List<BudgetData>
    fun writeNewBudgetEntries(entries: List<BudgetData>)
    fun updateBudgetEntryForYearAndMonth(cmd: FixBudgetToMatchDepositsCmd, newAmount: Int): BudgetData
}