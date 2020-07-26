package net.inherency.external.mint

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import net.inherency.vo.MintTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.LocalDate

@Service
class MintFileParser(private val mintTransactionFactory: MintTransactionFactory) {

    companion object {
        val HEADER_LINE_VALUES =
                listOf("Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes")
    }

    private val log = LoggerFactory.getLogger(MintFileParser::class.java)


    fun parseFile(
            fileContents: String,
            dateParsingStrategy: (String) -> LocalDate,
            amountParsingStrategy: (String) -> Int): List<MintTransaction> {
        val listOfRows = CsvReader().readAll(fileContents).toMutableList()
        if (listOfRows.size < 2) {
            return emptyList()
        }
        val header = listOfRows.removeAt(0)
        if (header != HEADER_LINE_VALUES) {
            throw IllegalArgumentException("File does not follow expected format")
        }
        log.info("Read {} rows from mint file.  Parsing now.", listOfRows.size)
        return transformListsIntoMintTransactions(listOfRows, dateParsingStrategy, amountParsingStrategy)
    }

    private fun transformListsIntoMintTransactions(
            listOfRows: List<List<String>>,
            dateParsingStrategy: (String) -> LocalDate,
            amountParsingStrategy: (String) -> Int): List<MintTransaction> {
        return mintTransactionFactory.listOfListOfStringsToListOfMintTransactions(
                listOfRows, dateParsingStrategy, amountParsingStrategy)
    }
}