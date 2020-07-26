package net.inherency.finances.external

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseMintFileLocalDate(stringDate: String): LocalDate {
    return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("M/d/y"))
}

fun parseGoogleSheetLocalDate(stringDate: String): LocalDate {
    return LocalDate.parse(stringDate)
}

fun parseMintFileAmount(stringAmount: String): Int {
    return stringAmount.toBigDecimal().multiply(BigDecimal("100")).toBigInteger().intValueExact()
}

fun parseGoogleSheetAmount(stringAmount: String): Int {
    return stringAmount.toInt()
}