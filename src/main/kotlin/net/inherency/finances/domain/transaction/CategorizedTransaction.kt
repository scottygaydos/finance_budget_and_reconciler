package net.inherency.finances.domain.transaction

import java.time.LocalDate
import java.util.*

data class CategorizedTransaction (
        val id: UUID,
        val date: LocalDate,
        val budgetCategoryId: Int,
        val description: String,
        val bankPayee: String,
        val creditAccountId: Int,
        val debitAccountId: Int,
        val authorizedAmount: Int,
        val settledAmount: Int,
        val reconcilable: Boolean
): Transaction {

    override fun getIdAsString(): String {
        return this.id.toString()
    }

    override fun toGoogleSheetRowList(): List<String> {
        return listOf(
                id.toString(),
                date.toString(),
                budgetCategoryId.toString(),
                description,
                bankPayee,
                creditAccountId.toString(),
                debitAccountId.toString(),
                authorizedAmount.toString(),
                settledAmount.toString(),
                if (reconcilable) "1" else "0"
        )
    }
}