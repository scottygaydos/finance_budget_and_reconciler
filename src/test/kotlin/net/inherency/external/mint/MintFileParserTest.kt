package net.inherency.external.mint

import net.inherency.external.parseMintFileAmount
import net.inherency.external.parseMintFileLocalDate
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MintFileParserTest {

    private val mintTransactionFactory = MintTransactionFactory()
    private val mintFileParser = MintFileParser(mintTransactionFactory)

    @Test
    fun `Parser should successfully transform each line of the CSV file into a MintTransaction while ignoring the header`() {
        val mintFileContent = """
        "Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes"
        "7/17/2020","H-E-B ONLINE","H-E-B ONLINE","128.25","debit","Groceries","Checking Account","",""
        "7/13/2020","Apple","APPLE.COM/BILL","0.99","debit","Electronics & Software","Credit Card(1234)","",""
        "7/03/2020","Employer PPD ID: 1224445555","Employer Payroll","1288.24","credit","Paycheck","Checking Account","",""
        """.trimIndent()

        val transactions = mintFileParser.parseFile(
                mintFileContent,
                { parseMintFileLocalDate(it) },
                { parseMintFileAmount(it) })

        assertEquals(3, transactions.size)

        val firstTransaction = transactions[0]
        assertEquals("Checking Account", firstTransaction.accountName)
        assertEquals(12825, firstTransaction.amount)
        assertEquals("Groceries", firstTransaction.category)
        assertEquals(CreditOrDebit.DEBIT, firstTransaction.creditOrDebit)
        assertEquals(LocalDate.of(2020, 7, 17), firstTransaction.date)
        assertEquals("H-E-B ONLINE", firstTransaction.description)
        assertEquals("H-E-B ONLINE", firstTransaction.originalDescription)

        val secondTransaction = transactions[1]
        assertEquals("Credit Card(1234)", secondTransaction.accountName)
        assertEquals(99, secondTransaction.amount)
        assertEquals("Electronics & Software", secondTransaction.category)
        assertEquals(CreditOrDebit.DEBIT, secondTransaction.creditOrDebit)
        assertEquals(LocalDate.of(2020, 7, 13), secondTransaction.date)
        assertEquals("Apple", secondTransaction.description)
        assertEquals("APPLE.COM/BILL", secondTransaction.originalDescription)

        val thirdTransaction = transactions[2]
        assertEquals("Checking Account", thirdTransaction.accountName)
        assertEquals(128824, thirdTransaction.amount)
        assertEquals("Paycheck", thirdTransaction.category)
        assertEquals(CreditOrDebit.CREDIT, thirdTransaction.creditOrDebit)
        assertEquals(LocalDate.of(2020, 7, 3), thirdTransaction.date)
        assertEquals("Employer PPD ID: 1224445555", thirdTransaction.description)
        assertEquals("Employer Payroll", thirdTransaction.originalDescription)
    }

    @Test
    fun `Mint Transaction Parser should fail if file does not follow expected format`() {
        val mintFileContentSemiColon = """
        "Date";"Description";"Original Description";"Amount";"Transaction Type";"Category";"Account Name";"Labels";"Notes"
        "7/17/2020";"H-E-B ONLINE";"H-E-B ONLINE";"128.25";"debit";"Groceries";"Checking Account";"";""
        "7/13/2020";"Apple";"APPLE.COM/BILL";"0.99";"debit";"Electronics & Software";"Credit Card(1234)";"";""
        "7/03/2020";"Employer PPD ID: 1224445555";"Employer Payroll";"1288.24";"credit";"Paycheck";"Checking Account";"";""
        """.trimIndent()

        assertFailsWith(Exception::class) {
            mintFileParser.parseFile(
                    mintFileContentSemiColon,
                    { parseMintFileLocalDate(it) },
                    { parseMintFileAmount(it) })
        }
    }

    @Test
    fun `Mint Transaction Parser should return empty list if file is completely empty`() {
        val mintFileContentEmpty = """
        """.trimIndent()

        val mintTransactions = mintFileParser.parseFile(
                mintFileContentEmpty,
                { parseMintFileLocalDate(it) },
                { parseMintFileAmount(it) })

        assertTrue(mintTransactions.isEmpty())
    }

    @Test
    fun `Mint Transaction Parser should return empty list if file only contains header`() {
        val mintFileContent = """
        "Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes"
        """.trimIndent()

        val mintTransactions = mintFileParser.parseFile(
                mintFileContent,
                { parseMintFileLocalDate(it) },
                { parseMintFileAmount(it) })

        assertTrue(mintTransactions.isEmpty())
    }

    @Test
    fun `Mint Transaction Parser should fail is header is not present`() {
        val mintFileContent = """
        "7/17/2020","H-E-B ONLINE","H-E-B ONLINE","128.25","debit","Groceries","Checking Account","",""
        "7/13/2020","Apple","APPLE.COM/BILL","0.99","debit","Electronics & Software","Credit Card(1234)","",""
        "7/03/2020","Employer PPD ID: 1224445555","Employer Payroll","1288.24","credit","Paycheck","Checking Account","",""
        """.trimIndent()

        assertFailsWith(IllegalArgumentException::class) {
            mintFileParser.parseFile(
                    mintFileContent,
                    { parseMintFileLocalDate(it) },
                    { parseMintFileAmount(it) })
        }
    }
}