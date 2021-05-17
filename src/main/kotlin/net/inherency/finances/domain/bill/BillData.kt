package net.inherency.finances.domain.bill

data class BillData (
        val accountId: Int,
        val name: String,
        val description: String,
        val dueDayOfMonth: Int,
        val autoPayEnabled: Boolean,
        val showInUIReports: Boolean,
        val budgetCategoryId: Int
)
