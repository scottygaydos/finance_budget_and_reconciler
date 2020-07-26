package net.inherency.external.mint

import net.inherency.vo.CreditOrDebit
import net.inherency.vo.MintTransaction
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
            row: List<String>, dateParsingStrategy: (String) -> LocalDate, amountParsingStrategy: (String) -> Int): MintTransaction {
        return MintTransaction(
                dateParsingStrategy(row[0]),
                row[1],
                row[2],
                amountParsingStrategy(row[3]),
                CreditOrDebit.parse(row[4]),
                row[5],
                row[6])
    }
}