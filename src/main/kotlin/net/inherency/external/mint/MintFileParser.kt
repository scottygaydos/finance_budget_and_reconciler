package net.inherency.external.mint

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import net.inherency.vo.CreditOrDebit
import net.inherency.vo.MintTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MintFileParser {

    private val log = LoggerFactory.getLogger(MintFileParser::class.java)

    fun parseFile(file: File): List<MintTransaction> {
        val listOfRows = CsvReader().readAll(file).toMutableList()
        listOfRows.removeAt(0) //First row is headers
        log.info("Read {} rows from mint file.  Parsing now.", listOfRows.size)
        return transformListsIntoMintTransactions(listOfRows)
    }

    private fun transformListsIntoMintTransactions(listOfRows: List<List<String>>): List<MintTransaction> {
        return listOfRows.map { row ->
            MintTransaction(
                    parseMintTransactionDate(row[0]),
                    row[1],
                    row[2],
                    row[3].toBigDecimal().multiply(BigDecimal("100")).toBigInteger().intValueExact(),
                    CreditOrDebit.parse(row[4]),
                    row[5],
                    row[6]
            )
        }
    }

    private fun parseMintTransactionDate(stringDate: String): LocalDate {
        return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("M/d/y"))
    }

}