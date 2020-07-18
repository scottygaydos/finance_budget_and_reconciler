package net.inherency.external.mint

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import net.inherency.vo.CreditOrDebit
import net.inherency.vo.MintTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MintFileParser {

    private val log = LoggerFactory.getLogger(MintFileParser::class.java)
    private val expectedHeaderLineValues =
        listOf("Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes")


    fun parseFile(fileContents: String): List<MintTransaction> {
        val listOfRows = CsvReader().readAll(fileContents).toMutableList()
        if (listOfRows.size < 2) {
            return emptyList()
        }
        val header = listOfRows.removeAt(0)
        if (header != expectedHeaderLineValues) {
            throw IllegalArgumentException("File does not follow expected format")
        }
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