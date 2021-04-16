package net.inherency.finances.domain.transaction

import java.time.LocalDate

data class MintTransaction (
        val date: LocalDate,
        val description: String,
        val originalDescription: String,
        val amount: Int,
        val creditOrDebit: CreditOrDebit,
        val category: String,
        val accountName: String
) : Transaction {

    companion object {
        const val UNKNOWN_ACCOUNT_NAME = "unknown"
    }

    override fun getIdAsString(): String {
        return "$date$description$originalDescription$amount$creditOrDebit$category$accountName"
                .replace(" ", "")
                .replace("/", "")
    }

    fun getCreditAccountName(): String {
        return if (creditOrDebit === CreditOrDebit.CREDIT) {
            accountName
        } else {
            UNKNOWN_ACCOUNT_NAME
        }
    }

    fun getDebitAccountName(): String {
        return if (creditOrDebit === CreditOrDebit.DEBIT) {
            accountName
        } else {
            UNKNOWN_ACCOUNT_NAME
        }
    }

    override fun toGoogleSheetRowList(): List<String> {
        return listOf(
                date.toString(),
                description,
                originalDescription,
                amount.toString(),
                creditOrDebit.toString(),
                category,
                accountName
        )
    }

    override fun toString(): String {
        val newLine = System.lineSeparator()
        return "MintTransaction " + newLine +
                "   date=" + date + newLine +
                "   description=" + description + newLine +
                "   originalDescription=" + originalDescription + newLine +
                "   amount=" + amount + newLine +
                "   creditOrDebit=" + creditOrDebit + newLine +
                "   category=" + category + newLine +
                "   account=" + accountName
    }


}