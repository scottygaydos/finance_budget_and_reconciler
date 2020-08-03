package net.inherency.finances.external.google

import net.inherency.finances.domain.transaction.CategorizedTransaction
import net.inherency.finances.external.parseGoogleSheetAmount
import net.inherency.finances.external.parseGoogleSheetLocalDate
import org.springframework.stereotype.Service
import java.util.*

@Service
class CategorizedTransactionFactory {

    fun listOfListOfStringsToCategorizedTransactions(input: List<List<String>>): List<CategorizedTransaction> {
        return input.map {
            CategorizedTransaction(
                    UUID.fromString(it[0]),
                    parseGoogleSheetLocalDate(it[1]),
                    it[2].toInt(),
                    it[3],
                    it[4],
                    it[5].toInt(),
                    it[6].toInt(),
                    parseGoogleSheetAmount(it[7]),
                    parseGoogleSheetAmount(it[8]),
                    it[9].toBoolean()
            )
        }
    }
}