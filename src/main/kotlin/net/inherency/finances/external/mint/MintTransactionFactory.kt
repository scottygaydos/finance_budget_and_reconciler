package net.inherency.finances.external.mint

import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MintTransactionFactory {

    fun listOfListOfStringsToListOfMintTransactions(
            listOfRows: List<List<String>>,
            dateParsingStrategy: (String) -> LocalDate,
            amountParsingStrategy: (String) -> Int): List<MintTransaction> {
        return listOfRows.map { row ->
            createMintTransactionFromListOfStrings(row, dateParsingStrategy, amountParsingStrategy)
        }
    }

    private fun createMintTransactionFromListOfStrings(
            row: List<String>, dateParsingFunction: (String) -> LocalDate, amountParsingFunction: (String) -> Int)
            : MintTransaction {
        return MintTransaction(
                dateParsingFunction(row[0]),
                row[1],
                row[2],
                amountParsingFunction(row[3]),
                CreditOrDebit.parse(row[4]),
                row[5],
                row[6])
    }
}