package net.inherency.integration

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import org.springframework.format.datetime.standard.DateTimeFormatterFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val firstDate = LocalDate.of(2020, 7, 17)
private val secondDate = LocalDate.of(2020, 7, 13)
private val thirdDate = LocalDate.of(2020, 7, 3)

private val mintFileFormatter: DateTimeFormatter = DateTimeFormatterFactory("MM/dd/yyyy").createDateTimeFormatter()
private val googleSheetFormatter: DateTimeFormatter = DateTimeFormatterFactory("yyyy-MM-dd").createDateTimeFormatter()

private const val firstAmount = "128.25"
private const val secondAmount = "0.99"
private const val thirdAmount = "1288.24"

private fun toCents(i: String): String = BigDecimal(i).multiply(BigDecimal("100")).toInt().toString()


val mintFileContent = """
        "Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes"
        "${firstDate.format(mintFileFormatter)}","H-E-B ONLINE","H-E-B ONLINE","$firstAmount","debit","Groceries","Checking Account","",""
        "${secondDate.format(mintFileFormatter)}","Apple","APPLE.COM/BILL","$secondAmount","debit","Electronics & Software","Credit Card(1234)","",""
        "${thirdDate.format(mintFileFormatter)}","Employer PPD ID: 1224445555","Employer Payroll","$thirdAmount","credit","Paycheck","Checking Account","",""
        """.trimIndent()

val testDataAsListOfListOfStrings = CsvReader().readAll("""
        "Date","Description","Original Description","Amount","Transaction Type","Category","Account Name","Labels","Notes"
        "${firstDate.format(googleSheetFormatter)}","H-E-B ONLINE","H-E-B ONLINE","${toCents(firstAmount)}","debit","Groceries","Checking Account","",""
        "${secondDate.format(googleSheetFormatter)}","Apple","APPLE.COM/BILL","${toCents(secondAmount)}","debit","Electronics & Software","Credit Card(1234)","",""
        "${thirdDate.format(googleSheetFormatter)}","Employer PPD ID: 1224445555","Employer Payroll","${toCents(thirdAmount)}","credit","Paycheck","Checking Account","",""
        """.trimIndent()
).toMutableList()