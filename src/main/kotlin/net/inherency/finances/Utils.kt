package net.inherency.finances

import java.math.BigDecimal
import java.text.NumberFormat

fun <O, T> objectIsUnique(objects: List<O>, objectToValidate: (O) -> T): Boolean {
    return objects.map { objectToValidate(it) }.toSet().size == objects.size
}

fun bigDecimalCentsToCurrency(bigDecimalInCents: BigDecimal): String {
    return NumberFormat.getCurrencyInstance().format(bigDecimalInCents.divide(BigDecimal("100")))
}

fun intCentsToCurrency(intInCents: Int): String {
    return bigDecimalCentsToCurrency(intInCents.toBigDecimal())
}